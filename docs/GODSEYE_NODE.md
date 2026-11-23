# SKYNET GOD'S EYE NODE

Local companion service:
127.0.0.1:8765

GET /health
GET /godseye
GET /devices
GET /memory

POST /chat
POST /pair
POST /unpair

Features:
- Local system diagnostics
- Existing network neighbor visibility
- Authorized device registry
- Local conversation memory
- Optional OpenAI Responses API bridge
- Offline diagnostics

The service binds to localhost only.
Device registration does not itself pair Bluetooth.
Actual device pairing requires platform approval.
