# SKYNET Mobile

SKYNET combines a native Android assistant shell with a static JARVIS-inspired web interface.

## Website
The GitHub Pages site in `docs/` includes a multi-chat interface, browser speech input/output, local conversation history, APK downloads, and optional Google Gemini API integration. A Gemini API key is created by the user and stored only in browser local storage. Google free-tier limits and availability may change. Without a key, the website runs in a limited local mode; it is not a full language model.

## Android
The Android app includes native search, app discovery, manual speech input, text-to-speech, local AI modules, device/network status, and optional local audit logs. No always-on microphone or hidden device control.

## Build
GitHub Actions builds and verifies a debug APK and publishes it to GitHub Releases. Debug APKs are for testing, not production signing.

## Privacy
The static website does not send chat history to a SKYNET backend. If Gemini is configured, the current conversation context is sent directly from the browser to Google's API. Do not paste secrets into public chats or commit them to Git.
