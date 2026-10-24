import Foundation
import MEPSDK

@objc(MoxoModuleImpl)
public class MoxoModuleImpl: NSObject, MEPClientDelegate {
    public typealias Resolve = (Any?) -> Void
    public typealias Reject = (String, String, Error?) -> Void

    let default_error_code = 999
    let default_error_message = "invalid parameters"

    @objc public var onUnreadMessageCountUpdated: ((NSNumber) -> Void)?
    @objc public var onTapWindowClose: (() -> Void)?

    @objc public func setup(domain: String, options: [String: Any]?) {
        MEPClient.sharedInstance().setup(withDomain: domain, linkConfig: nil)
        MEPClient.sharedInstance().delegate = self
    }

    @objc public func link(token: String, resolve: @escaping Resolve, reject: @escaping Reject) {
        MEPClient.sharedInstance().linkUser(withAccessToken: token) { error in
            Self.complete(error, resolve: resolve, reject: reject) { true }
        }
    }

    @objc public func unlink(keepNotification: Bool) {
        DispatchQueue.main.async {
            if keepNotification {
                MEPClient.sharedInstance().localUnlink()
            } else {
                MEPClient.sharedInstance().unlink()
            }
        }
    }

    @objc public func showMEPWindow() {
        DispatchQueue.main.async {
            MEPClient.sharedInstance().showMEPWindow()
        }
    }

    @objc public func hideMEPWindow() {
        DispatchQueue.main.async {
            MEPClient.sharedInstance().hideMEPWindow()
        }
    }

    @objc public func changeLanguage(_ language: String) {
        DispatchQueue.main.async {
            MEPClient.sharedInstance().changeLanguage(language)
        }
    }

    @objc public func openChat(chatId: String, feedSequence: NSNumber?, resolve: @escaping Resolve, reject: @escaping Reject) {
        let sequence = feedSequence == 0 ? nil : feedSequence
        DispatchQueue.main.async {
            MEPClient.sharedInstance().openChat(chatId, withFeedSequence: sequence) { error in
                Self.complete(error, resolve: resolve, reject: reject) { true }
            }
        }
    }

    @objc public func openRelation(user: [String: Any], resolve: @escaping Resolve, reject: @escaping Reject) {
        DispatchQueue.main.async {
            if let uniqueId = user["uniqueId"] as? String {
                MEPClient.sharedInstance().openRelationChat(withUniqueID: uniqueId) { error in
                    Self.complete(error, resolve: resolve, reject: reject) { true }
                }
            } else if let email = user["email"] as? String {
                MEPClient.sharedInstance().openRelationChat(withEmail: email) { error in
                    Self.complete(error, resolve: resolve, reject: reject) { true }
                }
            } else {
                let err = NSError(domain: MEPSDKErrorDomain, code: self.default_error_code, userInfo: [NSLocalizedDescriptionKey: "No uniqueId or email found"])
                reject("\(self.default_error_code)", self.default_error_message, err)
            }
        }
    }

    @objc public func getUnreadMessageCount(resolve: @escaping Resolve) {
        resolve(MEPClient.sharedInstance().getUnreadMessageCount())
    }

    @objc public func startMeet(topic: String, options: [String: Any]?, resolve: @escaping Resolve, reject: @escaping Reject) {
        let options = options ?? [:]
        DispatchQueue.main.async {
            let meetOption = MEPStartMeetOptions()
            meetOption.topic = topic
            meetOption.chatID = options["chat_id"] as? String
            meetOption.uniqueIDs = options["unique_ids"] as? [String]
            meetOption.autoJoinAudio = options["auto_join_audio"] as? Bool ?? true
            meetOption.autoStartVideo = options["auto_start_video"] as? Bool ?? false
            meetOption.autoRecording = options["auto_recording"] as? Bool ?? false
            meetOption.instantCall = options["instant_call"] as? Bool ?? false
            MEPClient.sharedInstance().startMeet(withOption: meetOption) { error, meetId in
                Self.complete(error, resolve: resolve, reject: reject) { ["session_id": meetId] }
            }
        }
    }

    // MARK: Notification

    @objc public func registerNotification(deviceToken: String, resolve: @escaping Resolve, reject: @escaping Reject) {
        guard let data = Data(hex: deviceToken) else {
            reject("\(default_error_code)", "Invalid device token", nil)
            return
        }
        MEPClient.sharedInstance().registerNotification(withDeviceToken: data) { error in
            Self.complete(error, resolve: resolve, reject: reject) { true }
        }
    }

    @objc public func parseNotification(payload: [String: Any], resolve: @escaping Resolve, reject: @escaping Reject) {
        MEPClient.sharedInstance().parseRemoteNotification(payload) { error, ret in
            Self.complete(error, resolve: resolve, reject: reject) { ret }
        }
    }

    // MARK: MEPClientDelegate

    public func client(_ client: MEPClient, didUpdateUnreadCount unreadCount: UInt) {
        onUnreadMessageCountUpdated?(NSNumber(value: unreadCount))
    }

    public func client(_ client: MEPClient, didTapClose sender: Any?) {
        onTapWindowClose?()
    }

    private static func complete(_ error: Error?, resolve: Resolve, reject: Reject, result: () -> Any?) {
        if let error {
            let err = error as NSError
            reject("\(err.code)", err.localizedDescription, err)
        } else {
            resolve(result())
        }
    }
}

extension Data {
    init?(hex: String) {
        guard !hex.isEmpty, hex.count.isMultiple(of: 2) else {
            return nil
        }

        let chars = Array(hex)
        let bytes = stride(from: 0, to: chars.count, by: 2)
            .map { String(chars[$0]) + String(chars[$0 + 1]) }
            .compactMap { UInt8($0, radix: 16) }

        guard bytes.count * 2 == hex.count else { return nil }
        self.init(bytes)
    }
}
