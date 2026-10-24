#import "MoxoModule.h"

#import <MEPSDK/MEPSDK.h>

#if __has_include(<react_native_moxo_module/react_native_moxo_module-Swift.h>)
#import <react_native_moxo_module/react_native_moxo_module-Swift.h>
#else
#import "react_native_moxo_module-Swift.h"
#endif

@implementation MoxoModule {
  MoxoModuleImpl *_impl;
}

RCT_EXPORT_MODULE(MoxoModule)

- (instancetype)init
{
  if (self = [super init]) {
    _impl = [MoxoModuleImpl new];
    __weak MoxoModule *weakSelf = self;
    _impl.onUnreadMessageCountUpdated = ^(NSNumber *count) {
      [weakSelf emitOnUnreadMessageCountUpdated:count];
    };
    _impl.onTapWindowClose = ^{
      [weakSelf emitOnTapWindowClose];
    };
  }
  return self;
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

- (void)setup:(NSString *)domain options:(NSDictionary *)options
{
  [_impl setupWithDomain:domain options:options];
}

- (void)link:(NSString *)token resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
  [_impl linkWithToken:token resolve:resolve reject:reject];
}

- (void)unlink:(BOOL)keepNotification
{
  [_impl unlinkWithKeepNotification:keepNotification];
}

- (void)showMEPWindow
{
  [_impl showMEPWindow];
}

- (void)hideMEPWindow
{
  [_impl hideMEPWindow];
}

- (void)changeLanguage:(NSString *)language
{
  [_impl changeLanguage:language];
}

- (void)openChat:(NSString *)chatId
    feedSequence:(NSNumber *)feedSequence
         resolve:(RCTPromiseResolveBlock)resolve
          reject:(RCTPromiseRejectBlock)reject
{
  [_impl openChatWithChatId:chatId feedSequence:feedSequence resolve:resolve reject:reject];
}

- (void)openRelation:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
  [_impl openRelationWithUser:options resolve:resolve reject:reject];
}

- (void)getUnreadMessageCount:(NSDictionary *)options
                      resolve:(RCTPromiseResolveBlock)resolve
                       reject:(RCTPromiseRejectBlock)reject
{
  [_impl getUnreadMessageCountWithResolve:resolve];
}

- (void)registerNotification:(NSString *)deviceToken
                     resolve:(RCTPromiseResolveBlock)resolve
                      reject:(RCTPromiseRejectBlock)reject
{
  [_impl registerNotificationWithDeviceToken:deviceToken resolve:resolve reject:reject];
}

- (void)parseNotification:(NSDictionary *)payload resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
  [_impl parseNotificationWithPayload:payload resolve:resolve reject:reject];
}

- (void)showMeetRinger:(NSString *)sessionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject
{
  reject(@"-1", @"showMeetRinger is only supported on Android", nil);
}

- (void)startMeet:(NSString *)topic
          options:(NSDictionary *)options
          resolve:(RCTPromiseResolveBlock)resolve
           reject:(RCTPromiseRejectBlock)reject
{
  [_impl startMeetWithTopic:topic options:options resolve:resolve reject:reject];
}

// iOS always reports close taps through MEPClientDelegate.
- (void)enableTapWindowCloseEvent
{
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
  return std::make_shared<facebook::react::NativeMoxoModuleSpecJSI>(params);
}

@end
