package com.moxomodule

import android.app.Application
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.module.annotations.ReactModule
import com.moxtra.mepsdk.MEPClient
import com.moxtra.mepsdk.MEPClientDelegate
import com.moxtra.mepsdk.data.MEPStartMeetOptions
import com.moxtra.sdk.LinkConfig
import com.moxtra.sdk.common.ActionListener
import com.moxtra.sdk.common.ApiCallback

@ReactModule(name = MoxoModuleModule.NAME)
class MoxoModuleModule(private val reactContext: ReactApplicationContext) :
  NativeMoxoModuleSpec(reactContext) {

  @Volatile private var tapWindowCloseEnabled = false

  override fun getName(): String = NAME

  override fun setup(domain: String, options: ReadableMap?) {
    Log.d(TAG, "setup called with domain: $domain, options: $options")
    val linkConfig = if (domain.isNotEmpty() && options != null) toLinkConfig(options) else null

    runOnSdkThread {
      MEPClient.initialize(reactContext.applicationContext as Application)
      MEPClient.setupDomain(domain, linkConfig)
      registerClientListeners()
    }
  }

  override fun link(token: String, promise: Promise) {
    Log.d(TAG, "link called")
    if (token.isEmpty()) {
      promise.reject("-1", "token is empty or null")
      return
    }
    runOnSdkThread(promise) {
      MEPClient.linkWithAccessToken(token, object : ApiCallback<Void?> {
        override fun onCompleted(result: Void?) {
          Log.d(TAG, "linkWithAccessToken successful...")
          promise.resolve(true)
        }

        override fun onError(errorCode: Int, errorMsg: String) {
          Log.d(TAG, "linkWithAccessToken failed, errCode:$errorCode, errMsg:$errorMsg")
          promise.reject(
            errorCode.toString(),
            "initWithAccessToken failed, errCode:$errorCode, errMsg:$errorMsg"
          )
        }
      })
    }
  }

  override fun unlink(keepNotification: Boolean) {
    Log.d(TAG, "unlink called with keep notification $keepNotification")
    val callback = object : ApiCallback<Void?> {
      override fun onCompleted(result: Void?) {
        Log.d(TAG, "unlink successful...")
      }

      override fun onError(errorCode: Int, errorMsg: String) {
        Log.w(TAG, "unlink failed with error code $errorCode and error message $errorMsg ...")
      }
    }
    runOnSdkThread {
      if (keepNotification) {
        MEPClient.localUnlink(callback)
      } else {
        MEPClient.unlink(callback)
      }
    }
  }

  override fun getUnreadMessageCount(options: ReadableMap?, promise: Promise) {
    runOnSdkThread(promise) {
      promise.resolve(MEPClient.getUnreadMessageCount())
    }
  }

  override fun showMEPWindow() {
    Log.d(TAG, "showMEPWindow called...")
    runOnSdkThread {
      if (MEPClient.isLinked()) {
        MEPClient.showMEPWindow(reactContext.baseContext)
      }
    }
  }

  override fun hideMEPWindow() {
    Log.d(TAG, "hideMEPWindow called...")
    runOnSdkThread {
      if (MEPClient.isLinked()) {
        MEPClient.destroyMEPWindow()
      }
    }
  }

  override fun openChat(chatId: String, feedSequence: Double?, promise: Promise) {
    Log.d(TAG, "openChat called...")
    runOnSdkThread(promise) {
      if (!MEPClient.isLinked()) {
        promise.reject("-1", "Not linked")
        return@runOnSdkThread
      }
      if (chatId.isEmpty()) {
        promise.reject("-1", "Invalid parameter")
        return@runOnSdkThread
      }
      MEPClient.openChat(chatId, feedSequence?.toLong() ?: 0, object : ApiCallback<Void?> {
        override fun onCompleted(result: Void?) {
          Log.d(TAG, "openChat successful...")
          promise.resolve(true)
        }

        override fun onError(errorCode: Int, errorMsg: String) {
          promise.reject(errorCode.toString(), "openChat failed, errCode:$errorCode, errMsg:$errorMsg")
        }
      })
    }
  }

  override fun openRelation(options: ReadableMap, promise: Promise) {
    Log.d(TAG, "openRelation called...")
    val uniqueId = options.stringOrNull("uniqueId")
    val email = options.stringOrNull("email")
    val callback = object : ApiCallback<Void?> {
      override fun onCompleted(result: Void?) {
        Log.d(TAG, "openRelationChat successful...")
        promise.resolve(true)
      }

      override fun onError(errorCode: Int, errorMsg: String) {
        promise.reject(
          errorCode.toString(),
          "openRelationChat failed, errCode:$errorCode, errMsg:$errorMsg"
        )
      }
    }
    runOnSdkThread(promise) {
      if (!MEPClient.isLinked()) {
        promise.reject("-1", "Not linked")
        return@runOnSdkThread
      }
      when {
        !uniqueId.isNullOrEmpty() -> MEPClient.openRelationChat(uniqueId, callback)
        !email.isNullOrEmpty() -> MEPClient.openRelationChatWithEmail(email, callback)
        else -> promise.reject("-1", "Invalid parameter")
      }
    }
  }

  override fun registerNotification(deviceToken: String, promise: Promise) {
    Log.d(TAG, "registerNotification called and token is " + if (deviceToken.isEmpty()) "empty" else "not empty")
    if (deviceToken.isEmpty()) {
      promise.reject("-1", "device token is empty")
      return
    }
    runOnSdkThread(promise) {
      MEPClient.registerNotification(deviceToken, null, null, null, object : ApiCallback<Void> {
        override fun onCompleted(result: Void?) {
          Log.d(TAG, "registerNotification called and completed...")
          promise.resolve(true)
        }

        override fun onError(errorCode: Int, errorMsg: String?) {
          Log.w(TAG, "registerNotification called but failed with $errorCode and $errorMsg ...")
          promise.reject(errorCode.toString(), "registerNotification failed, errCode:$errorCode, errMsg:$errorMsg")
        }
      })
    }
  }

  override fun parseNotification(payload: ReadableMap, promise: Promise) {
    Log.d(TAG, "parseNotification called and payload is $payload")
    val intent = toIntent(payload)
    runOnSdkThread(promise) {
      if (!MEPClient.isMEPNotification(intent)) {
        promise.reject("-1", "It is not moxo payload!")
        return@runOnSdkThread
      }
      MEPClient.parseRemoteNotification(intent, object : ApiCallback<Map<String, String>> {
        override fun onCompleted(result: Map<String, String>) {
          val map = Arguments.createMap()
          for ((key, value) in result) {
            when (key) {
              "chat_id", "feed_sequence", "session_id" -> map.putString(key, value)
              "meet_id" -> map.putString("session_id", value)
            }
          }
          promise.resolve(map)
        }

        override fun onError(errorCode: Int, errorMsg: String?) {
          promise.reject(
            errorCode.toString(),
            "parseRemoteNotification failed, errCode:$errorCode, errMsg:$errorMsg"
          )
        }
      })
    }
  }

  override fun showMeetRinger(sessionId: String, promise: Promise) {
    Log.d(TAG, "show meet ringer called with session id $sessionId")
    runOnSdkThread(promise) {
      if (!MEPClient.isLinked()) {
        promise.reject("-1", "Not linked")
        return@runOnSdkThread
      }
      if (sessionId.isEmpty()) {
        promise.reject("-1", "session id is empty!")
        return@runOnSdkThread
      }
      MEPClient.showMeetRinger(sessionId, object : ApiCallback<Void> {
        override fun onCompleted(result: Void?) {
          Log.d(TAG, "show meet ringer success ...")
          promise.resolve("Show meet ringer success")
        }

        override fun onError(errorCode: Int, errorMsg: String?) {
          Log.w(TAG, "show meet ringer failed with $errorCode and $errorMsg ...")
          promise.reject("-1", "Show meet ringer failed with $errorCode and $errorMsg")
        }
      })
    }
  }

  override fun changeLanguage(language: String) {
    Log.d(TAG, "changeLanguage called with $language ...")
    if (language.isEmpty()) {
      return
    }
    runOnSdkThread {
      MEPClient.changeLanguage(language)
    }
  }

  override fun startMeet(topic: String, options: ReadableMap?, promise: Promise) {
    Log.d(TAG, "startMeet called with topic: $topic, options: $options")
    if (topic.isEmpty()) {
      promise.reject("-1", "Topic is empty!")
      return
    }

    val meetOptions = MEPStartMeetOptions().apply { this.topic = topic }
    if (options != null) {
      options.stringOrNull("chat_id")?.let { meetOptions.chatID = it }
      options.booleanOrNull("auto_join_audio")?.let { meetOptions.setAutoJoinAudio(it) }
      options.booleanOrNull("auto_start_video")?.let { meetOptions.setAutoStartVideo(it) }
      options.booleanOrNull("auto_recording")?.let { meetOptions.isAutoRecordingEnabled = it }
      options.booleanOrNull("instant_call")?.let { meetOptions.setInstantCall(it) }
      if (options.hasKey("unique_ids") && options.getType("unique_ids") == ReadableType.Array) {
        val ids = options.getArray("unique_ids")
        if (ids != null) {
          meetOptions.uniqueIDs = ArrayList((0 until ids.size()).mapNotNull { ids.getString(it) })
        }
      }
    }

    runOnSdkThread(promise) {
      MEPClient.startMeet(meetOptions, object : ApiCallback<String> {
        override fun onCompleted(sessionId: String) {
          Log.d(TAG, "startMeet success ...")
          promise.resolve(Arguments.createMap().apply { putString("session_id", sessionId) })
        }

        override fun onError(errorCode: Int, errorMsg: String?) {
          Log.w(TAG, "startMeet failed with $errorCode and $errorMsg ...")
          promise.reject(errorCode.toString(), "startMeet failed with $errorCode and $errorMsg")
        }
      })
    }
  }

  override fun enableTapWindowCloseEvent() {
    Log.d(TAG, "enableTapWindowCloseEvent called...")
    tapWindowCloseEnabled = true
    runOnSdkThread {
      registerCloseButtonListener()
    }
  }

  // SDK calls share the queue that setup() initializes MEPClient on, so they
  // always run after it. SDK exceptions are reported instead of crashing the app.
  private fun runOnSdkThread(promise: Promise? = null, block: () -> Unit) {
    reactContext.runOnUiQueueThread {
      try {
        block()
      } catch (e: RuntimeException) {
        Log.w(TAG, "MEPClient call failed", e)
        promise?.reject("-1", e.message, e)
      }
    }
  }

  private fun registerClientListeners() {
    MEPClient.getClientDelegate().setOnUnreadMessageListener(
      MEPClientDelegate.OnUnreadMessageListener { unreadCount ->
        Log.d(TAG, "onUnreadMessageCountUpdated, count is $unreadCount")
        emitOnUnreadMessageCountUpdated(unreadCount.toDouble())
      }
    )
    if (tapWindowCloseEnabled) {
      registerCloseButtonListener()
    }
  }

  // Setting this listener replaces the SDK's default close behavior, so it is
  // only installed once JS subscribes to onTapWindowClose.
  private fun registerCloseButtonListener() {
    MEPClient.getClientDelegate().setOnCloseButtonListener(ActionListener<Void> { _, _ ->
      Log.d(TAG, "onTapWindowClose button clicked...")
      emitOnTapWindowClose()
    })
  }

  private fun toLinkConfig(options: ReadableMap): LinkConfig = LinkConfig().apply {
    options.stringOrNull("certOrgName")?.let { certOrganization = it }
    options.stringOrNull("certPublicKey")?.let { certPublicKey = it }
    options.booleanOrNull("ignoreBadCert")?.let { isIgnoreBadCert = it }
  }

  // Only top-level strings and numbers are passed on to the SDK.
  private fun toIntent(payload: ReadableMap): Intent {
    val intent = Intent()
    for ((key, value) in payload.toHashMap()) {
      when (value) {
        is String -> intent.putExtra(key, value)
        is Double -> intent.putExtra(key, value)
      }
    }
    return intent
  }

  private fun ReadableMap.stringOrNull(key: String): String? =
    if (hasKey(key) && getType(key) == ReadableType.String) getString(key) else null

  private fun ReadableMap.booleanOrNull(key: String): Boolean? =
    if (hasKey(key) && getType(key) == ReadableType.Boolean) getBoolean(key) else null

  companion object {
    const val NAME = NativeMoxoModuleSpec.NAME
    private const val TAG = "MoxoModuleModule"
  }
}
