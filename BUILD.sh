#!/data/data/com.termux/files/usr/bin/bash

cd "$HOME/SKYNET_MOBILE"

if ! command -v gradle >/dev/null 2>&1; then
    echo "Gradle mangler."
    echo "Android-prosjektet er opprettet."
    exit 1
fi

if [ -z "$ANDROID_HOME" ] || \
   [ ! -d "$ANDROID_HOME/platforms/android-35" ]; then
    echo "Android SDK 35 mangler."
    echo "Installer SDK 35 i Android IDE."
    exit 1
fi

gradle assembleDebug

echo "APK:"
echo "$HOME/SKYNET_MOBILE/app/build/outputs/apk/debug/app-debug.apk"
