package com.moxomodule

import android.app.Application
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.moxtra.mepsdk.MEPClient
import com.moxtra.mepsdk.MEPClientDelegate
import com.moxtra.mepsdk.data.MEPStartMeetOptions
import com.moxtra.sdk.LinkConfig
import com.moxtra.sdk.common.ApiCallback
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MoxoModuleModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val TAG: String = "MoxoModuleModule"

  private var mReactContext = reactContext

  private val UNREAD_MSG_EVENT = "onUnreadMessageCountUpdated"

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun setup(domain: String?, options: String?) {
    Log.d(TAG, "setup called with domain: $domain, options: $options")
    var linkConfig: LinkConfig? = null
    try {
      if (domain != null) {
        if (options != null && options is String) {
          linkConfig = LinkConfig()
          val jsonObject = JSONObject(options)
          val certOrgName: String? = jsonObject.getString("certOrgName")
          val certPublicKey: String? = jsonObject.getString("certPublicKey")
          val ignoreBadCert: Boolean = jsonObject.getBoolean("ignoreBadCert")
          linkConfig.setCertOrganization(certOrgName)
          linkConfig.setCertPublicKey(certPublicKey)
          linkConfig.setIgnoreBadCert(ignoreBadCert)
        }
      }
    } catch (e: JSONException) {

    }

    mReactContext.runOnUiQueueThread {
      MEPClient.initialize(mReactContext?.applicationContext as Application)
      MEPClient.setupDomain(domain, linkConfig)

      //setup callbacks
      onUnreadMessageCountUpdated()
    }
  }

  @ReactMethod
  fun link(token: String?, promise: Promise?) {
    Log.d(TAG, "link called with token: $token")
    if (token == null || token.isEmpty()) {
      promise?.reject("-1", "token is empty or null")
      return
    }
    mReactContext.runOnUiQueueThread {
      MEPClient.linkWithAccessToken(token, object : ApiCallback<Void?> {
        override fun onCompleted(rlt: Void?) {
          Log.d(
            TAG,
            "initWithAccessToken successful..."
          )
          promise?.resolve(true)
        }

        override fun onError(errorCode: Int, errorMsg: String) {
          Log.d(
            TAG,
            "initWithAccessToken failed, errCode:$errorCode, errMsg:$errorMsg"
          )
          promise?.reject(
            errorCode.toString(),
            "initWithAccessToken failed, errCode:$errorCode, errMsg:$errorMsg"
          )
        }
      })
    }
  }

  @ReactMethod
  fun unlink(keepNotification: Boolean) {
    Log.d(TAG, "unlink called with keep notification $keepNotification")
    val callback: ApiCallback<Void?> = object : ApiCallback<Void?> {
      override fun onCompleted(rult: Void?) {
        Log.d(TAG, "unlink successful...")
      }

      override fun onError(errorCode: Int, errorMsg: String) {
        Log.w(TAG, "unlink failed with error code $errorCode and error message $errorMsg ...")
      }
    }
    if (keepNotification) {
      MEPClient.localUnlink(callback)
    } else {
      MEPClient.unlink(callback)
    }
  }

  @ReactMethod
  fun getUnreadMessageCount(options: ReadableMap?, promise: Promise?) {
    var unreadCount = MEPClient.getUnreadMessageCount()
    promise?.resolve(unreadCount)
  }

  @ReactMethod
  fun showMEPWindow() {
    Log.d(TAG, "showMEPWindow called...")
    if (MEPClient.isLinked()) {
      Log.d(TAG, "MEP is linked and showMEPWindow...")
      MEPClient.showMEPWindow(mReactContext?.baseContext)
    }
  }

  @ReactMethod
  fun hideMEPWindow() {
    Log.d(TAG, "hideMEPWindow called...")
    if (MEPClient.isLinked()) {
      Log.d(TAG, "MEP is linked and hideMEPWindow...")
      MEPClient.destroyMEPWindow()
    }
  }

  @ReactMethod
  fun openChat(chatId: String?, feedSequence: Double?, promise: Promise?) {
    Log.d(TAG, "openChat called...")
    if (!MEPClient.isLinked()) {
      promise?.reject("-1", "Not linked")
      return
    }
    if (chatId?.isEmpty() == false) {
      MEPClient.openChat(chatId, feedSequence?.toLong() ?: 0, object : ApiCallback<Void?> {
        override fun onCompleted(rult: Void?) {
          Log.d(TAG, "openChat successful...")
          promise?.resolve(true)
        }

        override fun onError(errorCode: Int, errorMsg: String) {
          promise?.reject(
            errorCode.toString(),
            "openChat failed, errCode:$errorCode, errMsg:$errorMsg"
          )
        }
      })
    } else {
      promise?.reject("-1", "Invalid parameter")
    }
  }

  @ReactMethod
  fun openRelation(options: ReadableMap?, promise: Promise?) {
    Log.d(TAG, "openRelation called...")
    if (!MEPClient.isLinked()) {
      promise?.reject("-1", "Not linked")
      return
    }
    val uniqueId: String? = options?.getString("uniqueId")
    val email: String? = options?.getString("email")
    val callback: ApiCallback<Void?> = object : ApiCallback<Void?> {
      override fun onCompleted(rult: Void?) {
        Log.d(TAG, "openRelationChat successful...")
        promise?.resolve(true)
      }

      override fun onError(errorCode: Int, errorMsg: String) {
        promise?.reject(
          errorCode.toString(),
          "openRelationChat failed, errCode:$errorCode, errMsg:$errorMsg"
        )
      }
    }
    if (uniqueId?.isEmpty() == false) {
      MEPClient.openRelationChat(uniqueId, callback)
    } else if (email?.isEmpty() == false) {
      MEPClient.openRelationChatWithEmail(email, callback)
    } else {
      promise?.reject("-1", "Invalid parameter")
    }
  }

  private fun onUnreadMessageCountUpdated() {
    Log.d(TAG, "onUnreadMessageCountUpdated called...")
    val listener: MEPClientDelegate.OnUnreadMessageListener =
      MEPClientDelegate.OnUnreadMessageListener { unreadCount ->
        Log.d(TAG, "onUnreadMessageCountUpdated, count is $unreadCount")
        sendEvent(mReactContext, UNREAD_MSG_EVENT, unreadCount)
      }

    MEPClient.getClientDelegate().setOnUnreadMessageListener(listener)
  }

  private fun sendEvent(reactContext: ReactContext, eventName: String, params: Int?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  @ReactMethod
  fun addListener(eventName: String) {
    Log.d(TAG, "addListener called...")
  }

  @ReactMethod
  fun removeListeners(count: Integer) {
    Log.d(TAG, "removeListeners called...")
  }

  @ReactMethod
  fun registerNotification(deviceToken: String?) {
    Log.d(
      TAG,
      "registerNotification called and token is " + (if (deviceToken == null) "null" else "not null")
    )
    if (deviceToken != null) {
            MEPClient.registerNotification(
                deviceToken,
        null, null, null,
        object : ApiCallback<Void> {
        override fun onCompleted(rlt: Void?) {
          Log.d(TAG, "registerNotification called and completed...")
        }

        override fun onError(errorCode: Int, errorMsg: String?) {
          Log.w(TAG, "registerNotification called but failed with $errorCode and $errorMsg ...")
        }
      })
    }
  }

  @ReactMethod
  fun parseNotification(payload: ReadableMap?, promise: Promise?) {
    Log.d(
      TAG,
      "parseNotification called and payload is " + (payload ?: "null")
    )
    if (payload == null) {
      promise?.reject("-1", "payload data is null or empty")
      return
    }
    try {
      var intent = map2Intent(payload)
      if (MEPClient.isMEPNotification(intent)) {
        MEPClient.parseRemoteNotification(
          intent,
          object : ApiCallback<Map<String, String>> {
            override fun onCompleted(rlt: Map<String, String>) {
              var jsonObject = JSONObject()
              var keys = rlt.keys
              for (key in keys) {
                when (key) {
                  "chat_id", "feed_sequence", "session_id" -> jsonObject.put(key, rlt.getValue(key))
                  "meet_id" -> {
                    var meetId = rlt.getValue(key)
                    jsonObject.put("session_id", meetId)
                  }

                  else -> continue
                }
              }
              promise?.resolve(jsonObject.toString())
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
              promise?.reject(
                errorCode.toString(),
                "parseRemoteNotification failed, errCode:$errorCode, errMsg:$errorMsg"
              )
            }
          })
      } else {
        promise?.reject("-1", "It is not moxo payload!")
      }
    } catch (e: JSONException) {
      e.printStackTrace()
      promise?.reject("-1", "Invalid parameter: Not fcm payload")
    }
  }

  @ReactMethod
  fun showMeetRinger(sessionId: String, promise: Promise?) {
    Log.d(TAG, "show meet ringer called with session id $sessionId")
    if (!MEPClient.isLinked()) {
      promise?.reject("-1", "Not linked")
      return
    }
    if (sessionId.isEmpty()) {
      promise?.reject("-1", "session id is empty!")
      return
    }
    MEPClient.showMeetRinger(sessionId, object : ApiCallback<Void> {
      override fun onCompleted(rlt: Void?) {
        Log.d(TAG, "show meet ringer success ...")
        promise?.resolve("Show meet ringer success")
      }

      override fun onError(errorCode: Int, errorMsg: String?) {
        Log.w(TAG, "show meet ringer failed with $errorCode and $errorMsg ...")
        promise?.reject("-1", "Show meet ringer failed with $errorCode and $errorMsg")
      }
    })
  }

  @ReactMethod
  fun changeLanguage(lang: String) {
    Log.d(TAG, "changeLanguage called with $lang ...")
    if (lang.isNotEmpty()) {
      MEPClient.changeLanguage(lang)
    }
  }

    @ReactMethod
  fun startMeet(topic: String, options: ReadableMap?, promise: Promise?) {
    Log.d(TAG, "startMeet called with topic: $topic, options:" + options?.toString())
    if (topic.isNullOrEmpty()) {
      promise?.reject("-1", "Topic is empty!")
      return
    }

    var callback: ApiCallback<String> = object : ApiCallback<String> {
      override fun onCompleted(sessionId: String) {
        Log.d(TAG, "startMeet success ...")
        val jsonObject = JSONObject()
        with(jsonObject) {
          put("session_id", sessionId)
        }
        promise?.resolve(json2WritableNativeMap(jsonObject))
      }

      override fun onError(errorCode: Int, errorMsg: String?) {
        Log.w(TAG, "startMeet failed with $errorCode and $errorMsg ...")
        promise?.reject(errorCode.toString(), "startMeet failed with $errorCode and $errorMsg")
      }
    }

    //map to MEPStartMeetOptions
    var mepStartMeetOptions = MEPStartMeetOptions()
    mepStartMeetOptions.topic = topic

    var mapValues = options?.entryIterator;
    while (mapValues?.hasNext() == true) {
      var mapEntry = mapValues.next()
      when (mapEntry.key) {
        "chat_id" -> mepStartMeetOptions.chatID = mapEntry.value.toString()
        "auto_join_audio" -> mepStartMeetOptions.setAutoJoinAudio(mapEntry.value as Boolean)
        "auto_start_video" -> mepStartMeetOptions.setAutoStartVideo(mapEntry.value as Boolean)
        "auto_recording" -> mepStartMeetOptions.isAutoRecordingEnabled = mapEntry.value as Boolean
        "instant_call" -> mepStartMeetOptions.setInstantCall(mapEntry.value as Boolean)
        "unique_ids" -> {
          val ids = mapEntry.value
          if (ids is ReadableNativeArray) {
            ids.toArrayList().also { mepStartMeetOptions.uniqueIDs = it as ArrayList<String> }
          }
        }
      }
    }

    MEPClient.startMeet(mepStartMeetOptions, callback)
  }

  private fun json2Intent(jsonObject: JSONObject?): Intent {
    var intent = Intent()
    var keys = jsonObject?.keys();
    while (keys?.hasNext() == true) {
      var key = keys.next()
      intent.putExtra(key, jsonObject?.getString(key))
    }
    return intent
  }

  private fun map2Intent(readableMap: ReadableMap?): Intent {
    var intent = Intent()
    var mapValues = readableMap?.entryIterator;
    while (mapValues?.hasNext() == true) {
      var mapEntry = mapValues.next()
      when (mapEntry.value) {
        is Double -> intent.putExtra(mapEntry.key, mapEntry.value as Double)
        is String -> intent.putExtra(mapEntry.key, mapEntry.value as String)
        is Map<*, *> -> intent.putExtra(mapEntry.key, mapEntry.value as String)
        is ArrayList<*> -> intent.putExtra(mapEntry.key, mapEntry.value as ArrayList<*>)
      }
    }
    return intent
  }

 private fun json2WritableNativeMap(jsonObject: JSONObject?): WritableNativeMap {
    val writableMap = WritableNativeMap()
    jsonObject?.let {
      for (key in jsonObject.keys()) {
        when (val value = jsonObject.get(key)) {
          is JSONObject -> {
            writableMap.putMap(key, json2WritableNativeMap(value))
          }

          is JSONArray -> {
            writableMap.putArray(key, json2WritableNativeArray(value))
          }

          is String -> {
            writableMap.putString(key, value)
          }

          is Boolean -> {
            writableMap.putBoolean(key, value)
          }

          is Double -> {
            writableMap.putDouble(key, value)
          }

          is Int -> {
            writableMap.putInt(key, value)
          }

          is Long -> {
            writableMap.putDouble(key, value.toDouble());
          }

          else -> {
            writableMap.putString(key, value.toString())
          }
        }
      }
    }
    return writableMap
  }

  private fun json2WritableNativeArray(jsonArray: JSONArray?): WritableNativeArray {
    val writableArray = WritableNativeArray()
    jsonArray?.let {
      for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.get(i)
        if (jsonObject is JSONObject) {
          writableArray.pushMap(json2WritableNativeMap(jsonObject))
        }
      }
    }
    return writableArray
  }

  companion object {
    const val NAME = "MoxoModule"
  }
}
