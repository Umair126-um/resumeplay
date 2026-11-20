# Resume Player

A minimal, modern Android app that plays videos and audio from your phone and
always resumes each file from where you left off, even after you close the app.

## Features
- Lists videos and audio from your device (Android media library)
- Tap to play; it seeks back to your last position automatically
- Saves position on pause, every few seconds, and when the app is closed
- Clears the position when a file finishes, so it starts fresh next time
- A "Continue" card to jump back into the last thing you played
- Material 3 dark/light UI

## Getting the installable APK (no computer setup)

1. Create a free account at github.com if you do not have one.
2. Make a new repository (e.g. `resume-player`), Public or Private.
3. Upload all these files to it (keep the folder structure). The web uploader
   accepts a drag-and-drop of the whole folder.
4. Open the **Actions** tab. The "Build APK" workflow runs automatically.
5. When it finishes (about 3-5 minutes), open the run and download
   **ResumePlayer-apk** from the Artifacts section. Inside is `app-debug.apk`.
6. Copy that APK to your phone and install it (allow "install from unknown
   sources" when asked).

## Building in Android Studio instead
Open this folder in Android Studio (Koala or newer), let it sync, then
Run, or Build > Build Bundle(s) / APK(s) > Build APK(s).

## Notes
- minSdk 26 (Android 8.0) and up.
- The APK is debug-signed, which is fine for installing on your own phone.
