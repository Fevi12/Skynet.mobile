# SKYNET Mobile 1.1
Consent-first Android assistant shell with optional Termux/Linux companion.

Implemented: native Android UI; push-to-talk speech recognition (Norwegian); Android TextToSpeech; local allowlisted commands; optional app-private audit log; optional Termux dashboard at http://127.0.0.1:8766.

Privacy: no always-on microphone, no hidden wake-word, no sensor collection or telemetry upload. Unknown commands do not execute shell commands. Disabling the log stops new entries but does not erase old ones.

Roadmap: policy gate, confirmed tool permissions, local RAG with citations, opt-in Sentinel diagnostics, signed update metadata and rollback, separate Termux/Linux companion.

This is not an AOSP fork, always-listening wake-word service, autonomous self-training system, or signed OTA updater.
