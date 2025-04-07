// NativeTurboNfc.ts
import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  isSupported(): Promise<boolean>;
  isEnabled(): Promise<boolean>;

  startTagReading(): Promise<{
    success: boolean;
    payload?: string;
  }>;

  addListener(eventName: string): void;
  removeListeners(count: number): void;
  stopTagReading(): Promise<boolean>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('TurboNfc');
