#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <CoreNFC/CoreNFC.h>

API_AVAILABLE(ios(13.0))
@interface TurboNfc : RCTEventEmitter <RCTBridgeModule, NFCTagReaderSessionDelegate>
@property (nonatomic, strong) NFCTagReaderSession *nfcSession;
@end
