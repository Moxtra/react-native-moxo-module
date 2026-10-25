Moxo is an AI workflow automation platform for business operations.

# 🚀 Moxo React Native Module (`react-native-moxo-module`)

[![React Native](https://img.shields.io/badge/React%20Native-0.74%2B%20%7C%20TurboModule-61DAFB.svg?style=flat-square&logo=react)](https://reactnative.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6.svg?style=flat-square&logo=typescript)](https://www.typescriptlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2010.13.1-3DDC84.svg?style=flat-square&logo=android)](https://developer.android.com)
[![iOS](https://img.shields.io/badge/iOS-MEPSDK%2010.13.1-000000.svg?style=flat-square&logo=apple)](https://developer.apple.com/ios)
[![NPM](https://img.shields.io/badge/npm-react--native--moxo--module-CB3837.svg?style=flat-square&logo=npm)](https://www.npmjs.com/package/react-native-moxo-module)

Welcome to the **Moxo React Native Module Repository (`react-native-moxo-module`)**! This package provides the modern TurboModule wrapper for the Moxo Mobile SDK, enabling React Native mobile applications to launch high-touch client collaboration portals, real-time messaging, document collaboration, and WebRTC video meetings.

---

## 📖 1. What is this Project?

### The Core Concept
The **React Native Moxo Module** bridges Moxo's native mobile frameworks (`MEPSDK.xcframework` on iOS and `moxosdk.aar` on Android) into React Native applications using React Native's modern New Architecture (**TurboModules**).

It provides typed TypeScript APIs, promise-based asynchronous execution, and zero-overhead native bridging to launch conversation binders, conduct meetings, and manage user identity.

### Key Capabilities
* **Modern TurboModule Architecture:** Fully migrated to React Native TurboModule codegen for synchronous C++ call performance and typed specifications.
* **Full Collaboration Suite:** Launch client portals, view timeline binders, open interactive PDF documents, and conduct video calls.
* **Push Notification Gateway:** Seamlessly relays APNs and FCM device tokens to Moxo servers for background notifications.

### Role in the Moxo Ecosystem
* **Core SDK Source:** Links against  and .
* **Sample Reference:** Demonstrated in  (`v10reactnativeSDKApp`).

---

## 📂 2. Repository Anatomy

```
react-native-moxo/
├── src/                        # TypeScript source & TurboModule codegen specs
│   ├── index.ts                # Primary exported JavaScript/TypeScript API
│   └── NativeMoxoModule.ts     # TurboModule Spec interface
├── android/                    # Android Kotlin/Java TurboModule implementation
├── ios/                        # iOS Objective-C / Swift TurboModule implementation
├── package.json                # NPM package manifest (version 10.13.1)
└── react-native-moxo-module.podspec # CocoaPods specification for iOS
```

| Module / Directory Path | Role in the Ecosystem | Description |
| :--- | :--- | :--- |
| 📁 [src](./src) | **TypeScript Source** | Strongly-typed API interfaces and TurboModule NativeModule declarations. |
| 📁 [android](./android) | **Android TurboModule** | Native Android implementation linking Moxo Android SDK AAR. |
| 📁 [ios](./ios) | **iOS TurboModule** | Native iOS implementation linking `MEPSDK.xcframework`. |
| 📄 [package.json](./package.json) | **NPM Manifest** | Codegen configuration, dependencies, and script runners. |

---

## ⚙️ 3. Prerequisites & Environment Setup

* **React Native:** React Native **0.72+** (supports both Old and New Architecture).
* **Node.js:** **v22.14.0 (LTS)** (minimum **v22.13.0 LTS** required by `package.json` engines; manage with `nvm install 22.14.0 && nvm use 22.14.0`).
* **iOS Environment:** Xcode 15+ and CocoaPods 1.13+.
* **Android Environment:** Android Studio with Android NDK 26+ and Java 17.

---

## 🛠️ 4. Quick Start: Installation & Usage

### 1️⃣ Installation
```bash
# Using Yarn
yarn add react-native-moxo-module

# Using NPM
npm install react-native-moxo-module
```

### 2️⃣ iOS CocoaPods Setup
```bash
cd ios && pod install && cd ..
```

### 3️⃣ Initialize Moxo SDK in React Native
```typescript
import Moxo from 'react-native-moxo-module';

// Initialize the SDK with your tenant domain
await Moxo.init({
    domain: "your-subdomain.moxo.com"
});
```

### 4️⃣ Launch the Collaboration Portal
```typescript
// Open Moxo with authenticated access token
try {
    await Moxo.showMEP({
        accessToken: "USER_ACCESS_TOKEN"
    });
} catch (error) {
    console.error("Failed to launch Moxo:", error);
}
```

---

## 📦 5. Building & Publishing

To compile TypeScript and codegen artifacts:
```bash
yarn prepare
npm pack
```

---

## ⚡ 6. Technical Architecture & TurboModule Bridge

```
┌────────────────────────────────────────────────────────┐
│               React Native Application (TSX)           │
└──────────────────────────┬─────────────────────────────┘
                           │ JSI (JavaScript Interface)
┌──────────────────────────▼─────────────────────────────┐
│                 NativeMoxoModule Spec                  │
│       Codegen-generated C++ JSI / TurboModule Stubs    │
└────────────┬─────────────────────────────┬─────────────┘
             │ JNI                         │ ObjC / Swift Runtime
┌────────────▼─────────────┐ ┌─────────────▼─────────────┐
│   Android Native Bridge  │ │     iOS Native Bridge     │
│   (moxosdk.aar)          │ │   (MEPSDK.xcframework)    │
└──────────────────────────┘ └───────────────────────────┘
```

---

## 🌿 7. Versioning & Git Strategy

* **Main Branch:** `main` — Canonical branch for React Native module development.
* **Platform Alignment:** Release 10.13.1 aligns with Moxo SDK 10.13.1.
* **GitHub URL:** `https://github.com/Moxtra/react-native-moxo-module.git`
