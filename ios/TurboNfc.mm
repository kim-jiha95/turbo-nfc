#import "TurboNfc.h"
#import <React/RCTLog.h>
#import <CoreNFC/CoreNFC.h>

@implementation TurboNfc {
    NFCTagReaderSession *nfcSession;
    bool isEmulating;
    NSString *emulationData;
}

RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents {
    return @[@"nfcStatusChange", @"onTagDiscovered"];
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

RCT_EXPORT_METHOD(isSupported:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    if (@available(iOS 13.0, *)) {
        BOOL supported = [NFCTagReaderSession readingAvailable];
        resolve(@(supported));
    } else {
        resolve(@(NO));
    }
}

RCT_EXPORT_METHOD(startTagReading:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    if (@available(iOS 13.0, *)) {
        if (![NFCTagReaderSession readingAvailable]) {
            reject(@"not_supported", @"NFC is not supported on this device", nil);
            return;
        }
        
        if (self.nfcSession) {
            resolve(@(YES));
            return;
        }
        
        self.nfcSession = [[NFCTagReaderSession alloc] 
                          initWithPollingOption:NFCPollingISO14443
                          delegate:self
                          queue:dispatch_get_main_queue()];
        
        if (!self.nfcSession) {
            reject(@"session_error", @"Failed to create NFC session", nil);
            return;
        }
        
        [self sendEventWithName:@"nfcStatusChange" body:@"started"];
        [self.nfcSession beginSession];
        resolve(@(YES));
    } else {
        reject(@"not_supported", @"NFC is not available on this iOS version", nil);
    }
}

RCT_EXPORT_METHOD(startEmulation:(NSString *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSError *error = [NSError errorWithDomain:@"com.turboNfc"
                                        code:404
                                    userInfo:@{
                                        NSLocalizedDescriptionKey: @"HCE is not supported on iOS devices",
                                        NSLocalizedFailureReasonErrorKey: @"Apple does not provide Host Card Emulation API"
                                    }];

    reject(@"unsupported_feature",
           @"HCE is not supported on iOS devices",
           error);
}

RCT_EXPORT_METHOD(stopEmulation:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    resolve(@(YES));
}

- (void)beginTagReading API_AVAILABLE(ios(13.0)) {
    NFCTagReaderSession *session = [[NFCTagReaderSession alloc] 
                                   initWithPollingOption:NFCPollingISO15693
                                   delegate:self
                                   queue:dispatch_get_main_queue()];
    self->nfcSession = session;
    [session beginSession];
}

RCT_EXPORT_METHOD(stopTagReading:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    if (self.nfcSession) {
        [self.nfcSession invalidateSession];
        self.nfcSession = nil;
        [self sendEventWithName:@"nfcStatusChange" body:@"stopped"];
        resolve(@(YES));
    } else {
        resolve(@(NO));
    }
}

#pragma mark - NFCTagReaderSessionDelegate

- (void)tagReaderSession:(NFCTagReaderSession *)session 
          didDetectTags:(NSArray<__kindof id<NFCTag>> *)tags API_AVAILABLE(ios(13.0)) {
    if (tags.count > 0) {
        id<NFCTag> tag = tags[0];
        [session connectToTag:tag completionHandler:^(NSError *error) {
            if (error) {
                [session invalidateSessionWithErrorMessage:@"Connection failed"];
                [self sendEventWithName:@"onTagDiscovered" 
                                 body:@{@"success": @(NO), 
                                      @"error": error.localizedDescription}];
                return;
            }
            
            NSString *identifier = nil;
            
            // Handle different NFC tag types
            if ([tag conformsToProtocol:@protocol(NFCISO7816Tag)]) {
                id<NFCISO7816Tag> iso7816Tag = (id<NFCISO7816Tag>)tag;
                identifier = [iso7816Tag.identifier base64EncodedStringWithOptions:0];
            } 
            else if ([tag conformsToProtocol:@protocol(NFCISO15693Tag)]) {
                id<NFCISO15693Tag> iso15693Tag = (id<NFCISO15693Tag>)tag;
                identifier = [iso15693Tag.identifier base64EncodedStringWithOptions:0];
            }
            else if ([tag conformsToProtocol:@protocol(NFCMiFareTag)]) {
                id<NFCMiFareTag> miFareTag = (id<NFCMiFareTag>)tag;
                identifier = [miFareTag.identifier base64EncodedStringWithOptions:0];
            }
            
            if (identifier) {
                [self sendEventWithName:@"onTagDiscovered" 
                                 body:@{@"success": @(YES), 
                                      @"tagId": identifier}];
            } else {
                [self sendEventWithName:@"onTagDiscovered" 
                                 body:@{@"success": @(NO), 
                                      @"error": @"Unsupported tag type"}];
            }
            
            [session invalidateSession];
        }];
    }
}

- (void)tagReaderSession:(NFCTagReaderSession *)session 
          didInvalidateWithError:(NSError *)error API_AVAILABLE(ios(13.0)) {
    self.nfcSession = nil;
    
    // 사용자가 의도적으로 취소한 경우는 에러로 처리하지 않음
    if (error && error.code != NFCReaderSessionInvalidationErrorUserCanceled) {
        [self sendEventWithName:@"nfcStatusChange" body:@"error"];
        [self sendEventWithName:@"onTagDiscovered" 
                         body:@{@"success": @(NO), 
                              @"error": error.localizedDescription}];
    } else {
        [self sendEventWithName:@"nfcStatusChange" body:@"stopped"];
    }
}

- (void)tagReaderSessionDidBecomeActive:(NFCTagReaderSession *)session API_AVAILABLE(ios(13.0)) {
    [self sendEventWithName:@"nfcStatusChange" body:@"active"];
}

@end

