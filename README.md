# turbo-nfc

React Native TurboModule for NFC functionality

## Installation

```sh
npm install turbo-nfc-dubu
```

## Requirements

- React Native >= 0.71.0
- iOS 13+ (for CoreNFC support)
- Android API level 24+ (Android 7.0+) for NFC support

## iOS Setup

1. Add the following to your Info.plist:

```xml
<key>NFCReaderUsageDescription</key>
<string>NFC 태그를 읽기 위해 NFC 접근 권한이 필요합니다</string>

<key>com.apple.developer.nfc.readersession.formats</key>
<array>
    <string>NDEF</string>
    <string>TAG</string>
</array>
```

2. Enable Near Field Communication Tag Reading capability in your Xcode project:
   - Open your project in Xcode
   - Select your target
   - Go to "Signing & Capabilities"
   - Click "+" and add "Near Field Communication Tag Reading"

Note: Make sure your Apple Developer account has NFC capabilities enabled.

## Android Setup

1. Add the following permissions and features to your `AndroidManifest.xml`:

```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.NFC" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.USE_FINGERPRINT" />

<!-- Features -->
<uses-feature android:name="android.hardware.nfc" android:required="true" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
<uses-feature android:name="android.hardware.camera.flash" android:required="false" />
<uses-feature android:name="android.hardware.microphone" android:required="false" />
```

Note: These permissions and features are required for the NFC functionality and related features to work properly on Android devices.

## Features

- Check NFC capability and status
- Read NFC tags
- Event notifications for NFC tag detection

## Usage

```typescript
import { TurboNfc } from "turbo-nfc-dubu";

// Check if NFC is supported on the device
const isSupported = await TurboNfc.isSupported();
if (!isSupported) {
  console.log("NFC is not supported on this device");
  return;
}

// Check if NFC is enabled
const isEnabled = await TurboNfc.isEnabled();
if (!isEnabled) {
  console.log("NFC is not enabled on this device");
  return;
}

// Add listener for NFC tag detection
TurboNfc.addListener("nfcTagDetected");

// Start reading NFC tags
try {
  const result = await TurboNfc.startTagReading();
  if (result.success && result.payload) {
    console.log("NFC tag content:", result.payload);
  }
} catch (error) {
  console.error("Error reading NFC tag:", error);
}

// Stop reading NFC tags
const stopped = await TurboNfc.stopTagReading();

// Remove listeners when no longer needed
TurboNfc.removeListeners(1);
```

## API Reference

### Methods

#### `isSupported(): Promise<boolean>`

Checks if the device supports NFC functionality.

#### `isEnabled(): Promise<boolean>`

Checks if NFC is currently enabled on the device.

#### `startTagReading(): Promise<{ success: boolean; payload?: string }>`

Starts the NFC tag reading session. Returns a promise that resolves with the tag data when a tag is read.

#### `stopTagReading(): Promise<boolean>`

Stops the current NFC tag reading session. Returns a promise that resolves with a boolean indicating if the session was successfully stopped.

#### `addListener(eventName: string): void`

Adds a listener for NFC events. Currently supported event: `nfcTagDetected`.

#### `removeListeners(count: number): void`

Removes listeners for NFC events. The `count` parameter specifies how many listeners to remove.

## License

MIT
