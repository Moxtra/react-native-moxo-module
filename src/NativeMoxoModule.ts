import { TurboModuleRegistry } from 'react-native';
import type { CodegenTypes, TurboModule } from 'react-native';

export interface Spec extends TurboModule {
  setup(domain: string, options?: CodegenTypes.UnsafeObject): void;
  link(token: string): Promise<boolean>;
  unlink(keepNotification: boolean): void;
  showMEPWindow(): void;
  hideMEPWindow(): void;
  changeLanguage(language: string): void;
  openChat(chatId: string, feedSequence?: number): Promise<boolean>;
  openRelation(options: CodegenTypes.UnsafeObject): Promise<boolean>;
  getUnreadMessageCount(options?: CodegenTypes.UnsafeObject): Promise<number>;
  registerNotification(deviceToken: string): Promise<boolean>;
  parseNotification(
    payload: CodegenTypes.UnsafeObject
  ): Promise<CodegenTypes.UnsafeObject>;
  showMeetRinger(sessionId: string): Promise<string>;
  startMeet(
    topic: string,
    options?: CodegenTypes.UnsafeObject
  ): Promise<CodegenTypes.UnsafeObject>;
  // Android only takes over the close button once JS subscribes.
  enableTapWindowCloseEvent(): void;

  readonly onUnreadMessageCountUpdated: CodegenTypes.EventEmitter<number>;
  readonly onTapWindowClose: CodegenTypes.EventEmitter<void>;
}

export default TurboModuleRegistry.get<Spec>('MoxoModule');
