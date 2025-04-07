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
