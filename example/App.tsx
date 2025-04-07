import React, { useEffect, useState } from "react";
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { TurboNfc } from "turbo-nfc-dubu";

const App = () => {
  const [isSupported, setIsSupported] = useState(false);
  const [isEnabled, setIsEnabled] = useState(false);
  const [nfcData, setNfcData] = useState("");
  const [isReading, setIsReading] = useState(false);

  useEffect(() => {
    const checkNfcSupport = async () => {
      try {
        const supported = await TurboNfc.isSupported();
        setIsSupported(supported);

        if (supported) {
          const enabled = await TurboNfc.isEnabled();
          setIsEnabled(enabled);
        }
      } catch (error) {
        console.error("NFC check error:", error);
      }
    };

    checkNfcSupport();

    // Set up NFC tag detection listener
    TurboNfc.addListener("nfcTagDetected");

    return () => {
      // Clean up listener when component unmounts
      TurboNfc.removeListeners(1);
      if (isReading) {
        TurboNfc.stopTagReading();
      }
    };
  }, [isReading]);

  const startReading = async () => {
    try {
      setIsReading(true);
      const result = await TurboNfc.startTagReading();
      if (result.success && result.payload) {
        setNfcData(result.payload);
      }
    } catch (error) {
      console.error("NFC reading error:", error);
    } finally {
      setIsReading(false);
    }
  };

  const stopReading = async () => {
    try {
      const stopped = await TurboNfc.stopTagReading();
      if (stopped) {
        setIsReading(false);
      }
    } catch (error) {
      console.error("NFC stop error:", error);
    }
  };

  if (!isSupported) {
    return (
      <SafeAreaView style={styles.container}>
        <Text style={styles.warning}>NFC is not supported on this device</Text>
      </SafeAreaView>
    );
  }

  if (!isEnabled) {
    return (
      <SafeAreaView style={styles.container}>
        <Text style={styles.warning}>NFC is not enabled on this device</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <View style={styles.content}>
        <Text style={styles.title}>NFC Tag Reader</Text>

        {nfcData ? (
          <View style={styles.resultContainer}>
            <Text style={styles.resultTitle}>Tag Data:</Text>
            <Text style={styles.resultText}>{nfcData}</Text>
          </View>
        ) : (
          <Text style={styles.instructions}>
            Tap the button below to start scanning for NFC tags
          </Text>
        )}

        <TouchableOpacity
          style={[styles.button, isReading && styles.buttonReading]}
          onPress={isReading ? stopReading : startReading}
        >
          <Text style={styles.buttonText}>
            {isReading ? "Stop Reading" : "Start Reading"}
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F5F5F5",
  },
  content: {
    flex: 1,
    padding: 20,
    alignItems: "center",
    justifyContent: "center",
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    marginBottom: 20,
    color: "#333",
  },
  instructions: {
    fontSize: 16,
    textAlign: "center",
    marginBottom: 40,
    color: "#666",
  },
  button: {
    backgroundColor: "#4285F4",
    paddingVertical: 12,
    paddingHorizontal: 30,
    borderRadius: 8,
    elevation: 3,
  },
  buttonReading: {
    backgroundColor: "#F44336",
  },
  buttonText: {
    color: "white",
    fontSize: 16,
    fontWeight: "bold",
  },
  warning: {
    fontSize: 18,
    textAlign: "center",
    color: "#F44336",
    margin: 20,
  },
  resultContainer: {
    backgroundColor: "white",
    padding: 20,
    borderRadius: 8,
    width: "100%",
    marginBottom: 40,
    elevation: 2,
  },
  resultTitle: {
    fontSize: 18,
    fontWeight: "bold",
    marginBottom: 10,
    color: "#333",
  },
  resultText: {
    fontSize: 16,
    color: "#666",
  },
});

export default App;
