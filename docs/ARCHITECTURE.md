# SKYNET architecture
User interface (push-to-talk/text/dashboard) -> assistant core -> policy allowlist -> local tool router -> Android TTS -> optional local audit.

Current flow: explicit user action starts speech recognition; local commands are matched against an allowlist; unknown commands are informational only; no arbitrary shell execution; audit is stored in app-private storage.

Future: opt-in wake word in foreground service; local RAG with source references; Sentinel local diagnostics with consent and retention controls; signed update manager with verification and rollback; Termux/Linux companion. Preserve existing Termux data.

Security invariants: no covert microphone, no sensitive-data exfiltration, no arbitrary shell execution, human confirmation for consequential actions, verifiable/recoverable updates.
