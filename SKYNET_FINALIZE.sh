#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT="$HOME/SKYNET_MOBILE"
cd "$ROOT"

log() { echo; echo "===== $* ====="; }
fail() { echo "STOPPET: $*" >&2; exit 1; }

log "SKYNET FINAL INTEGRATION"

git rev-parse --is-inside-work-tree >/dev/null ||
  fail "Prosjektmappen er ikke et Git-repository."

[ "$(git branch --show-current)" = "main" ] ||
  fail "Bytt til main-branchen først."

git remote get-url origin >/dev/null ||
  fail "GitHub origin mangler."

log "BACKUP ANDROID CONFIGURATION"

if grep -q 'aapt2FromMavenOverride' gradle.properties; then
  cp -p gradle.properties \
    "gradle.properties.backup.$(date +%s)"

  sed -i \
    '/^[[:space:]]*android\.aapt2FromMavenOverride[[:space:]]*=/d' \
    gradle.properties
fi

log "ANDROID SDK"

SDK=""

if [ -f local.properties ]; then
  SDK="$(sed -n 's/^[[:space:]]*sdk\.dir[[:space:]]*=[[:space:]]*//p' \
    local.properties | tail -1)"
  SDK="$(printf '%s' "$SDK" | sed 's/\\:/:/g')"
fi

[ -n "$SDK" ] || SDK="${ANDROID_HOME:-}"
[ -n "$SDK" ] || SDK="$HOME/lib/android-sdk"

[ -r "$SDK/platforms/android-35/android.jar" ] ||
  fail "Android SDK 35 mangler eller kan ikke leses."

if [ ! -f local.properties ]; then
  printf 'sdk.dir=%s\n' "$SDK" > local.properties
fi

echo "SDK: $SDK"

log "JAVA AND GRADLE"

command -v java >/dev/null ||
  fail "Java mangler."

java -version

if [ -x ./gradlew ]; then
  GRADLE=(./gradlew)
elif command -v gradle >/dev/null; then
  GRADLE=(gradle)
else
  fail "Gradle mangler."
fi

"${GRADLE[@]}" --version

log "STOP OLD GRADLE PROCESSES"
"${GRADLE[@]}" --stop || true

log "BUILD COMPLETE SKYNET APK"

"${GRADLE[@]}" --no-daemon \
  --stacktrace \
  :app:assembleDebug

APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"

[ -s "$APK" ] ||
  fail "APK ble ikke produsert."

log "APK VALIDATION"

ls -lh "$APK"
sha256sum "$APK"

log "GITHUB SOURCE INTEGRATION"

# Bare prosjektets kildekode og dokumentasjon.
# SDK-stier, APK-er og lokale hemmeligheter blir ikke publisert.

git add -A -- \
  app/src \
  app/build.gradle \
  build.gradle \
  settings.gradle \
  gradle.properties \
  docs \
  README.md \
  .github/workflows/android.yml \
  SKYNET_FINALIZE.sh

if ! git diff --cached --quiet; then
  git config user.name >/dev/null 2>&1 ||
    git config user.name "SKYNET Termux Builder"

  git config user.email >/dev/null 2>&1 ||
    git config user.email \
      "skynet-builder@users.noreply.github.com"

  git commit -m "Finalize SKYNET mobile integration"
fi

log "SYNCHRONIZE GITHUB"

git fetch origin main
git pull --rebase origin main
git push origin main

log "FINAL VERIFICATION"

git status --short
git log -1 --oneline

echo
echo "APK:"
echo "$APK"

echo
echo "GITHUB:"
echo "https://github.com/Fevi12/Skynet.mobile"

echo
echo "ACTIONS:"
echo "https://github.com/Fevi12/Skynet.mobile/actions"

echo
echo "===== SKYNET FINALIZATION COMPLETE ====="
