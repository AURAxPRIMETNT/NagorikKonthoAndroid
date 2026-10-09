# Android Studio ছাড়াই APK তৈরি (GitHub Actions)

1. GitHub.com-এ নিজের account খুলে sign in করো।
2. New repository তৈরি করো; নাম দাও `NagorikKonthoAndroid` এবং Private রাখলে ভালো।
3. Extract করা এই folder-এর ভেতরের সব ফাইল ও folder repository-র root-এ upload করো। `.github/workflows/build-apk.yml` ফাইলটিও থাকতে হবে।
4. GitHub repository-তে Actions tab খোলো। Workflow `Build NagorikKontho APK` দেখা গেলে সেটি নির্বাচন করে `Run workflow` চাপো। প্রথম push-এর পর workflow নিজে থেকেও চলতে পারে।
5. কাজ শেষ হলে সবুজ check-সহ run-টি খোলো। নিচে `Artifacts` থেকে `NagorikKontho-debug-apk` download করো।
6. Download করা artifact ZIP extract করলে `app-debug.apk` পাবে। সেটি Android ফোনে পাঠিয়ে install করে পরীক্ষা করতে পারো।

নোট: GitHub-এর interface বদলাতে পারে। APK-টি debug/test build; Play Store-এর জন্য release signing ও AAB আলাদাভাবে করতে হবে। Account password, signing key বা ব্যক্তিগত তথ্য repository-তে upload কোরো না।
