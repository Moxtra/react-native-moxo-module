![moxo](https://assets-global.website-files.com/612ecbcc615e87b0b9b38524/62037243f5ede375a8705a34_Moxo-Website-Button.svg)

[ [Introduce](#introduce) &bull; [Preparation](#preparation) &bull; [Installation](#installation) &bull; [Initialization](#initialization) &bull; [Sample Code](#sample-code) &bull; [API Doc](#api-doc)]

## Introduce

**react-native-moxo-module** is a [moxo sdk](https://www.moxo.com/platform/sdks) react-native wrapper. Provide Moxo OneStop capabilities to your mobile app built on [React Native](https://reactnative.dev/)

### Supported Platforms

* iOS 13.0+
* Android 4.4+

## Preparation

Below sdk or tools are required before start to use react-native-moxo-module.

* Node.js v14+
* Watchman

### Android

* Android Studio
* Android SDK v19+

### iOS

* Xcode v14.3+
* Cocoapod v1.11.0+

For more react-native set up details, please ref to [react native official site](https://reactnative.dev/docs/environment-setup)

## Installation

```sh
npm install @moxtradeveloper/react-native-moxo-module
```

### iOS Specific Steps

* Add moxo cocoapod repo as source into Podfile under your iOS project.
* Change pod deployment platform to iOS 13

Sample:

```ruby
require_relative '../node_modules/react-native/scripts/react_native_pods'
require_relative '../node_modules/@react-native-community/cli-platform-ios/native_modules'
#Add moxo source here
source 'https://maven.moxtra.com/repo/moxtra-specs.git'

#Change platform version if is not iOS 13
platform :ios, '13.0'
install! 'cocoapods', :deterministic_uuids => false

# Rest of the file.....
```

## Initialization

### Login

Before login, we need to get access token, by Moxo RestAPI:

```js
// Get access token
const response = await fetch('https://myenv.moxo.com/v1/core/oauth/token', {
    method: 'POST',
    headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        client_id: 'my_clientid',
        org_id: 'my_orgid',
        unique_id: 'my_uniqueid',
        client_secret: 'my_clientsecret'
    })
})
const json = await response.json();
token = json.access_token
```

Import plugin before use, then initialize moxo sdk and login with access token:

```js
import * as moxo from 'react-native-moxo-module';
// Setup domain
moxo.setup('myenv.moxo.com')
//Login and show moxo engagement platform window
moxo.link(token).then((result)=>{
if (result)
    console.log(`Link success`)
}).catch((err=>{
    console.log(`Link failed!:${err}`);
}))
```

### Show MEP window

After login successful, we can show MEP window directly.

```js
moxo.showMEPWindow();
```

## Sample Code

### Open existing chat

If user is logged in, call open chat API to open existing chat. If not logged in or chat does not exists, API will return error with error code and error message.

```js
moxo.openChat('CBSmiUUjyIJP7gR8YIpiagvH', '')
```

### Notification

To enable notification feature, you'll need to integrate a notification module first, here we take [react-native-notifications](https://www.npmjs.com/package/react-native-notifications) for example, which can help to get device token and notification payload.

#### Register notification

Through react-native-notifications registerRemoteNotifications(), post user agreement, notification will be enabled for your app.
Call registerRemoteNotificationsRegistered() to get a device token.
Then pass token to moxo function ``registerNotification()`` will register notification to Moxo server.

```js
    Notifications.registerRemoteNotifications();
    Notifications.events().registerRemoteNotificationsRegistered((event: Registered) => {
      console.log("Device Token Received", event.deviceToken);
      moxo.registerNotification(event.deviceToken).catch((err=>{
        alertErr("Register notification failed", err);
      }));
    });
    Notifications.events().registerRemoteNotificationsRegistrationFailed((event: RegistrationError) => {
        console.error(event);
    });
```

#### Handle notification

For example, once notification received, and user tapped notification, react-native-notifications function registerNotificationOpened will be triggered with notification payload data. Then pass payload to moxo function ``parseNotification()`` like below:

```js
    Notifications.events().registerNotificationOpened((notification: Notification, completion: () => void, action?: NotificationActionResponse) => {
      console.log("Notification opened by device user", notification.payload);
      if (action != undefined) {
        console.log(`Notification opened with an action identifier: ${action.identifier} and response text: ${action.text}`);
      }
      moxo.parseNotification(notification.payload).then((result=>{
        console.log(`Moxo notificaiton parse success: ${result}`);
        completion();
      })).catch(err => {
        alertErr('Notification parse failed',err)
      })
    });
```

If is a Moxo notification, then callback of the ``parseNotification()`` would be triggered with info parameter. Usually info contains 'chat_id' or 'meet_id', depends one which kind of notification you received.
To do more, you can invoke function openChat(chat_id) to open target chat directly.

#### Sample notification payload

##### iOS

```json
{
    "aps": {
        "alert": {
            "body": "cheng4: hi",
            "action_loc_key": "BCA"
        },
        "sound": "default"
    },
    "request": {
        "object": {
            "board": {
                "id": "CBPErkesrtOeFfURA6gusJAD",
                "feeds": [{
                    "sequence": 191
                }]
            }
        }
    },
    "id": "359",
    "moxtra": "",
    "category": "message",
    "board_id": "CBPErkesrtOeFfURA6gusJAD",
    "moxtra": ""
}
```

##### Android

```json
[
  {
    "count": "7",
    "sound": "default",
    "title": "rm1 Zhang",
    "message": "rm1 Zhang: 1",
    "additionalData": {
       "feed_sequence": "3234",
       "action_loc_key": "BCA",
       "board_feed_unread_count": "3",
       "moxtra": "",
       "user_id": "CUxceIGfpXcHBna163lfFMD0",
       "arg1": "rm1 Zhang",
       "arg2": "1",
       "arg3": "",
       "loc_key": "BCM",
       "request": {
         "object": {
        "board": {
           "id": "CBPErkesrtOeFfURA6gusJAD",
           "feeds": [{
              "sequence": 3234
                    }]
                 }
               }
        },
       "board_id": "CBPErkesrtOeFfURA6gusJAD",
       "coldstart": false,
       "board_name": "rm1 Zhang",
       "foreground": true
    }
}]
```

## API Doc

[API doc](https://htmlpreview.github.io/?https://github.com/Moxtra/react-native-moxo-module/blob/main/docs/index.html)
