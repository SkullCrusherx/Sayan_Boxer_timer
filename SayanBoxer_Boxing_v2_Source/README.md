# Sayan Boxer — Boxing Number Coach 2.0

Android source project, not an APK. This environment does not have Android SDK/Gradle packages to build an APK locally.

## Features
- Original offline number-only human-style synthetic voice samples (1–6), 1.2x speed, bundled under app/src/main/res/raw
- User-specified gap AFTER the last spoken number of a combination: 1000ms, 2000ms, 3000ms or custom 0–60000ms
- User-made punch combinations, random or saved order
- Real boxing-ring-style synthesized start and end bell sounds
- Distinct ten-second warning alert on each training round
- Voice on/off timer-only mode; button to mute/unmute in an ongoing session
- Round duration, rest time and round count; foreground Android background workout service
- No beginner/intermediate/advanced training categories
- Offline stats and guide; glove-style boxing app icon

## Create APK without installing Android Studio
Upload everything including hidden `.github` folder to GitHub. In Actions select `Build Sayan Boxer APK` and choose `Run workflow`. After a successful run, download `SayanBoxer-boxing-debug-apk` artifact, unzip and install `app-debug.apk` on your Android phone.

## Local requirements
Android Studio with JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Android API 35, Android Build Tools 35.0.0. Select Build APK(s). First sync requires internet.

The source is not compiled or device-tested yet. Do not redistribute as a production app without testing on actual hardware. Sound assets are original synthetic audio. 
