# Arday & Xaadirin — Android app

An offline-first Android application for student registration and attendance across multiple classes. Built with Kotlin, Jetpack Compose and Room.

## Features
- Create multiple classes
- Register students with unique Student ID, name, optional phone number and class
- Search students by name or ID
- Record daily attendance as **Jooga** or **Maqan** for each student in each class
- Today's overview: student count, class count, present and absent counts
- Local Room database; no internet or account required

## Build and install
1. Install Android Studio (Hedgehog or newer) and Android SDK Platform 35.
2. Open this `ArdayApp` folder in Android Studio.
3. Allow Gradle sync to finish.
4. Connect an Android phone with USB debugging enabled, or start an emulator.
5. Choose **Run > Run 'app'** to install it.
6. To create an APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Android Studio will show the APK location.

Package: `com.example.ardayapp`  
Minimum Android: 6.0 (API 23)

## Notes
- Student IDs must be unique. If a duplicate ID is entered, the database rejects it; the current simple UI does not yet display a duplicate-ID warning.
- Attendance is stored locally on this device. Uninstalling the app or clearing its data removes the database. Add a backup/export feature before relying on it for long-term records.
