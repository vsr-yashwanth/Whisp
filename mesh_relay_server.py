import http.server
import socketserver
import urllib.request
import json
import threading

PORT = 8088
connected_clients = set()

class MeshRelayHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/api/mesh/ping":
            client_ip = self.client_address[0]
            if client_ip != "127.0.0.1" and not client_ip.startswith("10.0.2"):
                connected_clients.add(f"http://{client_ip}:8080")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"id": "Laptop-WiFi-Relay", "name": "Laptop Wireless Mesh Relay"}).encode())
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == "/api/mesh/packet":
            client_ip = self.client_address[0]
            if client_ip != "127.0.0.1" and not client_ip.startswith("10.0.2"):
                connected_clients.add(f"http://{client_ip}:8080")
            
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length)
            
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(b'{"success":true}')

            targets = set(connected_clients)
            targets.add("http://127.0.0.1:8081") # Forward to emulator

            def forward_all():
                for target in targets:
                    try:
                        req = urllib.request.Request(
                            f"{target}/api/mesh/packet",
                            data=body,
                            headers={"Content-Type": "application/json"}
                        )
                        urllib.request.urlopen(req, timeout=1.5)
                        print(f"[RELAY] Forwarded packet to {target}")
                    except Exception as e:
                        pass

            threading.Thread(target=forward_all, daemon=True).start()
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        pass

def run_server():
    server = socketserver.ThreadingTCPServer(("0.0.0.0", PORT), MeshRelayHandler)
    server.allow_reuse_address = True
    print(f"=== Laptop Wireless Mesh Relay running on port {PORT} ===")
    server.serve_forever()

if __name__ == "__main__":
    run_server()
