# Workshop Billing (Android)
1. Create a new GitHub repository and upload everything in this folder (keep the .github folder).
2. Open the Actions tab -> "Build APK" -> wait for the green tick (about 5 minutes). If it did not start, click Run workflow.
3. Open the finished run -> Artifacts -> download WorkshopBilling-apk, unzip, install app-debug.apk on the phone (allow "install unknown apps").
4. On first launch pick a backup folder (e.g. Documents/WorkshopBackup). After reinstalling, pick the same folder and your data returns.
To change the app, edit www/index.html and commit; the APK rebuilds. Every build uses the same signing key, so new APKs install over the old one without losing data.
