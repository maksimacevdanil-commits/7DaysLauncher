# 7DaysLauncher

Android launcher prototype for selecting and scanning a locally stored PC game folder.

## Version 0.2.0
- Native Android Kotlin application.
- Storage Access Framework folder picker with persistent read permission.
- Recursive scan for Windows EXE and common game data files.
- Local launcher.log.
- GitHub Actions debug APK build.

## Important limitation
This project does not yet run the Windows PC game. Android cannot execute a Windows x86-64 EXE directly. A compatible ARM64 Windows runtime and graphics translation layer must be integrated and tested separately. The Play button currently reports this status instead of pretending to launch the game.

## Build
Requires JDK 17, Android SDK 35, and Gradle 8.9.

Run: gradle assembleDebug

APK: app/build/outputs/apk/debug/app-debug.apk

Push to main/master or manually run "Build Android APK" from the Actions tab. Download the generated artifact from the workflow run.

Use only game files you are legally entitled to use.
