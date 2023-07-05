#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
@interface RCT_EXTERN_MODULE(MoxoModule, RCTEventEmitter)
RCT_EXTERN_METHOD(setup:(NSString *)domain options:(NSDictionary *)options)
RCT_EXTERN_METHOD(link:(NSString *)token
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(unlink:(BOOL)keepNotification)
RCT_EXTERN_METHOD(showMEPWindow)
RCT_EXTERN_METHOD(hideMEPWindow)
RCT_EXTERN_METHOD(changeLanguage:(NSString *)language)
RCT_EXTERN_METHOD(openChat:(NSString *)chatId
                  feedSequence:(NSNumber *__nonnull)feedSequence
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(openRelation:(NSDictionary *)user
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(getUnreadMessageCount:(NSDictionary *)options
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(onUnreadMessageCountUpdated:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(registerNotification:(NSString *)deviceToken
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(parseNotification:(NSDictionary *)payload
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

@end
