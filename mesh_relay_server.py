import http.server
import socketserver
import urllib.request
import json
import threading
import time
from pymongo import MongoClient

PORT = 8088
connected_clients = set()

# Initialize MongoDB Connection
try:
    mongo_client = MongoClient("mongodb://localhost:27017/", serverSelectionTimeoutMS=2000)
    db = mongo_client["whisp_db"]
    users_col = db["users"]
    admins_col = db["admins"]
    print("[MONGODB] Connected successfully to local MongoDB at localhost:27017 (whisp_db)")
except Exception as e:
    print(f"[MONGODB WARNING] Could not connect to local MongoDB: {e}")
    users_col = None
    admins_col = None

class MeshRelayHandler(http.server.BaseHTTPRequestHandler):

    def _set_json_headers(self, status=200):
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    def do_OPTIONS(self):
        self._set_json_headers(200)

    def do_GET(self):
        if self.path == "/api/mesh/ping":
            client_ip = self.client_address[0]
            if client_ip != "127.0.0.1" and not client_ip.startswith("10.0.2"):
                connected_clients.add(f"http://{client_ip}:8080")
            self._set_json_headers(200)
            self.wfile.write(json.dumps({"id": "Laptop-WiFi-Relay", "name": "Laptop Wireless Mesh Relay & MongoDB Auth"}).encode())
        else:
            self._set_json_headers(404)
            self.wfile.write(json.dumps({"error": "Endpoint not found"}).encode())

    def do_POST(self):
        client_ip = self.client_address[0]
        if client_ip != "127.0.0.1" and not client_ip.startswith("10.0.2"):
            connected_clients.add(f"http://{client_ip}:8080")

        content_length = int(self.headers.get("Content-Length", 0))
        body_bytes = self.rfile.read(content_length)
        
        # 1. USER REGISTRATION ENDPOINT
        if self.path == "/api/auth/register":
            try:
                data = json.loads(body_bytes.decode())
                username = data.get("username", "").strip()
                password = data.get("password", "").strip()
                role = data.get("role", "USER").strip()

                if len(username) < 3 or len(password) < 4:
                    self._set_json_headers(400)
                    self.wfile.write(json.dumps({"success": False, "error": "Invalid username or password format"}).encode())
                    return

                if users_col is not None:
                    existing = users_col.find_one({"username": username})
                    if existing:
                        self._set_json_headers(400)
                        self.wfile.write(json.dumps({"success": False, "error": f"Username '{username}' already exists"}).encode())
                        return

                    new_doc = {
                        "username": username,
                        "password": password,
                        "role": role,
                        "status": "ACTIVE",
                        "createdAt": int(time.time() * 1000)
                    }
                    users_col.insert_one(new_doc)
                    print(f"[AUTH REGISTER] New user '{username}' registered in MongoDB")

                self._set_json_headers(200)
                self.wfile.write(json.dumps({
                    "success": True,
                    "username": username,
                    "role": role,
                    "token": f"whisp_usr_{int(time.time())}_{username}"
                }).encode())
            except Exception as e:
                self._set_json_headers(500)
                self.wfile.write(json.dumps({"success": False, "error": str(e)}).encode())

        # 2. USER LOGIN ENDPOINT
        elif self.path == "/api/auth/login":
            try:
                data = json.loads(body_bytes.decode())
                username = data.get("username", "").strip()
                password = data.get("password", "").strip()

                if users_col is not None:
                    user = users_col.find_one({"username": username, "password": password})
                    if user:
                        if user.get("status") == "SUSPENDED":
                            self._set_json_headers(403)
                            self.wfile.write(json.dumps({"success": False, "error": "Account suspended by administrator"}).encode())
                            return

                        self._set_json_headers(200)
                        self.wfile.write(json.dumps({
                            "success": True,
                            "username": user["username"],
                            "role": user.get("role", "USER"),
                            "token": f"whisp_usr_{int(time.time())}_{user['username']}"
                        }).encode())
                        print(f"[AUTH SUCCESS] User '{username}' logged in via MongoDB")
                        return

                # Local fallback check if MongoDB is unreachable
                if (username == "yashwanth" and password == "password123") or (username == "user" and password == "whisp123") or (username == "alice" and password == "alice123") or (username == "bob" and password == "bob123"):
                    self._set_json_headers(200)
                    self.wfile.write(json.dumps({
                        "success": True,
                        "username": username,
                        "role": "USER",
                        "token": f"whisp_usr_{int(time.time())}_{username}"
                    }).encode())
                    return

                self._set_json_headers(401)
                self.wfile.write(json.dumps({"success": False, "error": "Invalid username or password"}).encode())
            except Exception as e:
                self._set_json_headers(500)
                self.wfile.write(json.dumps({"success": False, "error": str(e)}).encode())

        # 3. ADMIN LOGIN ENDPOINT
        elif self.path == "/api/auth/admin-login":
            try:
                data = json.loads(body_bytes.decode())
                username = data.get("username", "").strip()
                password = data.get("password", "").strip()

                if admins_col is not None:
                    admin = admins_col.find_one({"username": username, "password": password})
                    if admin:
                        self._set_json_headers(200)
                        self.wfile.write(json.dumps({
                            "success": True,
                            "username": admin["username"],
                            "role": admin.get("role", "SUPER_ADMIN"),
                            "token": f"whisp_adm_{int(time.time())}_{admin['username']}"
                        }).encode())
                        print(f"[AUTH SUCCESS] Admin '{username}' authenticated via MongoDB")
                        return

                # Local fallback check
                if (username == "admin" and password == "whispadmin123") or (username == "operator" and password == "operator123"):
                    self._set_json_headers(200)
                    self.wfile.write(json.dumps({
                        "success": True,
                        "username": username,
                        "role": "SUPER_ADMIN" if username == "admin" else "NETWORK_ADMIN",
                        "token": f"whisp_adm_{int(time.time())}_{username}"
                    }).encode())
                    return

                self._set_json_headers(401)
                self.wfile.write(json.dumps({"success": False, "error": "Invalid admin credentials"}).encode())
            except Exception as e:
                self._set_json_headers(500)
                self.wfile.write(json.dumps({"success": False, "error": str(e)}).encode())

        # 3. MESH PACKET ROUTING
        elif self.path == "/api/mesh/packet":
            self._set_json_headers(200)
            self.wfile.write(b'{"success":true}')

            targets = set(connected_clients)
            targets.add("http://127.0.0.1:8081") # Forward to emulator

            def forward_all():
                for target in targets:
                    try:
                        req = urllib.request.Request(
                            f"{target}/api/mesh/packet",
                            data=body_bytes,
                            headers={"Content-Type": "application/json"}
                        )
                        urllib.request.urlopen(req, timeout=1.5)
                        print(f"[RELAY] Forwarded packet to {target}")
                    except Exception:
                        pass

            threading.Thread(target=forward_all, daemon=True).start()
        else:
            self._set_json_headers(404)
            self.wfile.write(json.dumps({"error": "Endpoint not found"}).encode())

    def log_message(self, format, *args):
        pass

def run_server():
    server = socketserver.ThreadingTCPServer(("0.0.0.0", PORT), MeshRelayHandler)
    server.allow_reuse_address = True
    print(f"=== Laptop Wireless Mesh Relay & MongoDB Auth running on port {PORT} ===")
    server.serve_forever()

if __name__ == "__main__":
    run_server()
