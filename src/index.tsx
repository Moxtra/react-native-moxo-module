import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
const LINKING_ERROR =
  `The package 'react-native-moxo-module' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';
const UNREAD_MSG_EVENT = "onUnreadMessageCountUpdated";

const MoxoModule = NativeModules.MoxoModule
  ? NativeModules.MoxoModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    )
const MoxoEventEmitter = new NativeEventEmitter(MoxoModule);
/**
 * Setup domain
 * Notice: This API MUST be invoked first before 'link'
 *
 * @param {string} baseDomain        - Your server domain
 * @param {object} options           - SSL cert options, sample:
 * {
 *    //SSL cert organization name. Optional, default is null.
 *    "certOrgName" : "Moxo, Inc." 
 *    
 *    //SSL cert public key.
 *    "certPublicKey" : "-----BEGIN PUBLIC KEY-----\nYOUR PUBLIC KEY\n-----END PUBLIC KEY-----\n" 
 *    
 *    //Ignore bad SSL cert or not. Default is true.
 *    ignoreBadCert : false
 * }
 */
export function setup(domain: string, options?:object) {
  return MoxoModule.setup(domain,options);
}

/**
 * Link user with access token.
 * @param {string} token             - User access token
*/
export function link(token: string): Promise<boolean> {
  return MoxoModule.link(token);
}

/**
 *  Unlink user.
 * @param {boolean} keepNotification             - Keep notification after user unlink
*/
export function unlink(keepNotification: boolean = false) {
  return MoxoModule.unlink(keepNotification);
}

/**
 * Show mep main window.
*/
export function showMEPWindow() {
  return MoxoModule.showMEPWindow();
}

/**
 * Hide mep main window.
*/
export function hideMEPWindow() {
  return MoxoModule.hideMEPWindow();
}

/**
 * Change language, language code should follow ISO 639-1 standard.
*/
export function changeLanguage(language: string) {
  return MoxoModule.changeLanguage(language);
}

/**
 * Open chat with chat Id and scroll to the specified feed
 * @param {string} chatId             - Id of chat.
 * @param {number} feedSequence       - Sequence of feed.
*/
export function openChat(chatId: string, feedSequence:number): Promise<boolean> {
  return MoxoModule.openChat(chatId,feedSequence);
}

/**
 * Open relation chat with user
 * @param {object} options             - User object, includes either uniqueId or email of target relation user, sample: 
 * {
 *    "uniqueId" : "user_unique_id_1" 
 *    //or 
 *    "email" : "user_email "
 * }
*/
export function openRelation(options: object): Promise<boolean> {
  return MoxoModule.openRelation(options);
}

/**
 *  Get unread message count of current user.
 * @param {object} options             - Reserved option.
*/
export function getUnreadMessageCount(options?:object): Promise<number> {
  return MoxoModule.getUnreadMessageCount(options);
}

/**
 * Set the callback when unread messages count updated
 *
 * @param {function} callback   - Would be invoked when user unread messages count updated, with an integer parameter which represents unread messages count post update
*/
export function onUnreadMessageCountUpdated(callback:(count:number) => void) {
  MoxoEventEmitter.addListener(UNREAD_MSG_EVENT,callback);
}

/**
 * Register Moxo notification with device token 
 *
 * @param {string} deviceToken   - Device token
*/
export function registerNotification(deviceToken:string) {
  return MoxoModule.registerNotification(deviceToken);
}

/**
 * Parse the notification to extract related info.
 * When parse success, it will return with object like below:
 *  {
 *      //For chat:
 *      "chat_id": "CBPErkesrtOeFfURA6gusJAD"
 *      "feed_sequence": 191
 *
 *      //For meet:
 *      "session_id": "255576178"
 *  }
 *
 * @param {object} payload   - Notification payload
*/
export function parseNotification(payload:object): Promise<object>  {
  return MoxoModule.parseNotification(payload);
}

