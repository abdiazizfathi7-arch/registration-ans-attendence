# Build the APK without installing Android Studio

This project includes a GitHub Actions workflow that builds the Android APK in GitHub's cloud.

1. Create a free account at https://github.com if you do not already have one.
2. Create a new repository and upload the contents of this project ZIP (the `ArdayApp` folder contents).
3. Open the repository's **Actions** tab. If prompted, enable GitHub Actions.
4. Select **Build Android APK** and press **Run workflow** (or push to `main`/`master`).
5. Wait for the workflow to finish successfully.
6. Open the completed workflow run and download the **ArdayApp-debug-apk** artifact.
7. Extract the downloaded artifact on your phone or computer. Install `app-debug.apk` on your Android phone. If Android asks, allow installation from that source.

The APK is a debug build for testing, not a Play Store release. App data is stored locally on the phone using the app's Room database. Uninstalling the app may delete its local data, so export/backup support should be added before relying on it for important records.
