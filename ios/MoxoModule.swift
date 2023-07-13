@objc(MoxoModule)
class MoxoModule: RCTEventEmitter, MEPClientDelegate {
    let default_error_code = 999
    let default_error_message = "invalid parameters"
    let unread_message_event = "onUnreadMessageCountUpdated"
    private var onUnreadMessageCountUpdated:RCTResponseSenderBlock? = nil;
    
    override class func moduleName() -> String! {
        return "MoxoModule"
    }
    
    @objc(setup:options:)
    func setup(domain: String, options: Dictionary<String, Any>) {
        MEPClient.sharedInstance().setup(withDomain: domain, linkConfig: nil)
        MEPClient.sharedInstance().delegate = self
    }

    @objc(link:withResolver:withRejecter:)
    func link(token: String,resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) {
        MEPClient.sharedInstance().linkUser(withAccessToken: token) { error in
            if let err = error as? NSError {
                reject("\(err.code)", err.localizedDescription, error)
            } else {
                resolve(true)
            }
        }
    }
    
    @objc(unlink:)
    func unlink(keepNotification: Bool) {
        DispatchQueue.main.async {
            if (keepNotification) {
                MEPClient.sharedInstance().localUnlink()
            } else {
                MEPClient.sharedInstance().unlink()
            }
        }
    }

    @objc(showMEPWindow)
    func showMEPWindow() {
        DispatchQueue.main.async {
            MEPClient.sharedInstance().showMEPWindow()
        }
    }
    
    @objc(hideMEPWindow)
    func hideMEPWindow() {
        DispatchQueue.main.async {
            MEPClient.sharedInstance().hideMEPWindow()
        }
    }
    
    @objc(changeLanguage:)
    func changeLanguage(language:String) {
        DispatchQueue.main.async {
            MEPClient.sharedInstance().changeLanguage(language)
        }
    }
    
    @objc(openChat:feedSequence:withResolver:withRejecter:)
    func openChat(chatId:String,feedSequence:NSNumber,resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
            MEPClient.sharedInstance().openChat(chatId, withFeedSequence: feedSequence == 0 ? nil : feedSequence) { error in
                if let err = error as? NSError {
                    reject("\(err.code)", err.localizedDescription, error)
                } else {
                    resolve(true)
                }
            }
        }
    }
    
    @objc(openRelation:withResolver:withRejecter:)
    func openRelation(user:Dictionary<String, String>,resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
            if let uniqueId = user["uniqueId"] {
                MEPClient.sharedInstance().openRelationChat(withUniqueID: uniqueId) { error in
                    if let err = error as? NSError {
                        reject("\(err.code)", err.localizedDescription, error)
                    } else {
                        resolve(true)
                    }
                }
            } else if let email = user["email"] {
                MEPClient.sharedInstance().openRelationChat(withEmail: email) { error in
                    if let err = error as? NSError {
                        reject("\(err.code)", err.localizedDescription, error)
                    } else {
                        resolve(true)
                    }
                }
            } else {
                let err = NSError(domain:MEPSDKErrorDomain, code:self.default_error_code, userInfo:[ NSLocalizedDescriptionKey: "No uniqueId or email found"])
                reject("\(self.default_error_code)", self.default_error_message, err)
            }
        }
    }
    
    @objc(getUnreadMessageCount:withResolver:withRejecter:)
    func getUnreadMessageCount(options:Dictionary<String,String>, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) {
        let ret = MEPClient.sharedInstance().getUnreadMessageCount()
        resolve(ret)
    }
    
    //MARK: Notification
    @objc(registerNotification:withResolver:withRejecter:)
    func registerNotification(deviceToken:String,resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) {
        if let data = Data(hex: deviceToken) {
            MEPClient.sharedInstance().registerNotification(withDeviceToken: data) { error in
                if let err = error as? NSError {
                    reject("\(err.code)", err.localizedDescription, error)
                } else {
                    resolve(true)
                }
            }
        }
    }
    
    @objc(parseNotification:withResolver:withRejecter:)
    func parseNotification(payload:Dictionary<String, Any>,resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) {
        MEPClient.sharedInstance().parseRemoteNotification(payload) { error, ret in
            if let err = error as? NSError {
                reject("\(err.code)", err.localizedDescription, error)
            } else {
                resolve(ret)
            }
        }
    }
    
    //MARK: MEPClientDelegate
    func client(_ client: MEPClient, didUpdateUnreadCount unreadCount: UInt) {
        sendEvent(withName: unread_message_event, body: unreadCount)
    }
    
    //MARK: RCTBridgeModule
    override func supportedEvents() -> [String]! {
        return [unread_message_event]
    }
}

extension Data {
    init?(hex: String) {
        guard hex.count.isMultiple(of: 2) else {
            return nil
        }
        
        let chars = hex.map { $0 }
        let bytes = stride(from: 0, to: chars.count, by: 2)
            .map { String(chars[$0]) + String(chars[$0 + 1]) }
            .compactMap { UInt8($0, radix: 16) }
        
        guard hex.count / bytes.count == 2 else { return nil }
        self.init(bytes)
    }
}
