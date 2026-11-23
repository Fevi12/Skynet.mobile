import os, json, time, socket, subprocess
import urllib.request, urllib.error
from pathlib import Path
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

BASE = Path.home() / ".skynet-ultimate"
BASE.mkdir(exist_ok=True)

DEVICES = BASE / "devices.json"
MEMORY = BASE / "memory.jsonl"
PORT = int(os.environ.get("SKYNET_PORT", "8765"))
MODEL = os.environ.get("SKYNET_MODEL", "gpt-6.2-astra")

def read_json(path, default):
    try:
        return json.loads(path.read_text())
    except Exception:
        return default

def command(args):
    try:
        p = subprocess.run(args, capture_output=True,
                           text=True, timeout=3)
        return p.stdout.strip() if p.returncode == 0 else None
    except Exception:
        return None

def godseye():
    try:
        stat = os.statvfs(str(Path.home()))
        disk = {
            "free_bytes": stat.f_bavail * stat.f_frsize,
            "total_bytes": stat.f_blocks * stat.f_frsize
        }
    except Exception:
        disk = {}

    battery = command(["termux-battery-status"])
    wifi = command(["termux-wifi-connectioninfo"])
    bluetooth = command(["termux-bluetooth-info"])

    return {
        "system": "SKYNET GOD'S EYE",
        "status": "ONLINE",
        "time": time.strftime("%Y-%m-%d %H:%M:%S"),
        "hostname": socket.gethostname(),
        "platform": os.uname()._asdict(),
        "disk": disk,
        "network_interfaces": command(["ip", "-brief", "addr"]),
        "known_network_neighbors": command(["ip", "neigh", "show"]),
        "wifi": json.loads(wifi) if wifi else "API unavailable",
        "bluetooth": json.loads(bluetooth) if bluetooth else "API unavailable",
        "battery": json.loads(battery) if battery else "API unavailable"
    }

def remember(role, content):
    item = {
        "time": time.time(),
        "role": role,
        "content": content
    }
    with MEMORY.open("a") as f:
        f.write(json.dumps(item, ensure_ascii=False) + "\n")

def ai_response(message):
    key = os.environ.get("OPENAI_API_KEY")

    if not key:
        remember("user", message)
        return {
            "mode": "offline",
            "reply": (
                "SKYNET lokalmodus er aktiv. "
                "Meldingen er lagret lokalt. "
                "AI-modell krever konfigurert API-nøkkel."
            )
        }

    payload = json.dumps({
        "model": MODEL,
        "instructions": (
            "You are SKYNET, a helpful personal AI assistant. "
            "Respect user authorization, privacy and device security. "
            "Never claim to control devices without an authorized integration."
        ),
        "input": message
    }).encode()

    req = urllib.request.Request(
        "https://api.openai.com/v1/responses",
        data=payload,
        headers={
            "Authorization": "Bearer " + key,
            "Content-Type": "application/json"
        },
        method="POST"
    )

    try:
        with urllib.request.urlopen(req, timeout=90) as response:
            data = json.loads(response.read())

        texts = []
        for item in data.get("output", []):
            for content in item.get("content", []):
                if content.get("type") == "output_text":
                    texts.append(content.get("text", ""))

        reply = "\n".join(texts) or "No text returned."
        remember("user", message)
        remember("assistant", reply)

        return {"mode": "online", "reply": reply}

    except Exception as error:
        return {
            "mode": "error",
            "reply": "AI connection failed: " + str(error)
        }

class Handler(BaseHTTPRequestHandler):
    def send_json(self, data, status=200):
        body = json.dumps(data, ensure_ascii=False,
                          default=str).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/health":
            self.send_json({
                "name": "SKYNET",
                "status": "ONLINE",
                "version": "2.0-node"
            })

        elif self.path == "/godseye":
            self.send_json(godseye())

        elif self.path == "/devices":
            self.send_json({
                "authorized_registry": read_json(DEVICES, []),
                "known_network_neighbors": command(
                    ["ip", "neigh", "show"]
                ) or "Unavailable"
            })

        elif self.path == "/memory":
            try:
                lines = MEMORY.read_text().splitlines()[-30:]
                self.send_json({"entries": [json.loads(x) for x in lines]})
            except Exception:
                self.send_json({"entries": []})

        else:
            self.send_json({"error": "Not found"}, 404)

    def do_POST(self):
        try:
            size = int(self.headers.get("Content-Length", "0"))
            if size > 20000:
                return self.send_json({"error": "Too large"}, 413)

            data = json.loads(self.rfile.read(size) or b"{}")
        except Exception:
            return self.send_json({"error": "Invalid JSON"}, 400)

        if self.path == "/chat":
            message = str(data.get("message", "")).strip()
            if not message:
                return self.send_json({"error": "Message required"}, 400)
            self.send_json(ai_response(message))

        elif self.path == "/pair":
            name = str(data.get("name", "")).strip()[:100]
            device_id = str(data.get("id", "")).strip()[:150]
            kind = str(data.get("type", "unknown"))[:40]

            if not name or not device_id:
                return self.send_json({"error": "Name and ID required"}, 400)

            devices = read_json(DEVICES, [])
            devices = [d for d in devices if d["id"] != device_id]
            devices.append({
                "id": device_id,
                "name": name,
                "type": kind,
                "approved": True,
                "added": time.time()
            })
            DEVICES.write_text(json.dumps(devices, indent=2))
            self.send_json({"registered": True, "device": name})

        elif self.path == "/unpair":
            device_id = str(data.get("id", ""))
            devices = read_json(DEVICES, [])
            devices = [d for d in devices if d["id"] != device_id]
            DEVICES.write_text(json.dumps(devices, indent=2))
            self.send_json({"removed": True})

        else:
            self.send_json({"error": "Not found"}, 404)

    def log_message(self, *args):
        pass

print("SKYNET NODE listening on 127.0.0.1:" + str(PORT), flush=True)
ThreadingHTTPServer(("127.0.0.1", PORT), Handler).serve_forever()
