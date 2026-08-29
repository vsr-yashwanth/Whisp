import time
from pymongo import MongoClient

def init_db():
    print("=== Initializing Whisp MongoDB Local Database ===")
    client = MongoClient("mongodb://localhost:27017/")
    db = client["whisp_db"]

    # 1. Seed Admins collection
    admins = db["admins"]
    admin_data = [
        {
            "username": "admin",
            "password": "whispadmin123",
            "role": "SUPER_ADMIN",
            "permissions": ["ALL"],
            "createdAt": int(time.time() * 1000)
        },
        {
            "username": "operator",
            "password": "operator123",
            "role": "NETWORK_ADMIN",
            "permissions": ["TOPOLOGY", "NODES", "ROUTING"],
            "createdAt": int(time.time() * 1000)
        }
    ]
    for adm in admin_data:
        admins.update_one({"username": adm["username"]}, {"$set": adm}, upsert=True)
    print(f"[OK] Injected {len(admin_data)} admin accounts into MongoDB whisp_db.admins")

    # 2. Seed Users collection for App Login
    users = db["users"]
    user_data = [
        {
            "username": "yashwanth",
            "password": "password123",
            "role": "USER",
            "status": "ACTIVE",
            "createdAt": int(time.time() * 1000)
        },
        {
            "username": "user",
            "password": "whisp123",
            "role": "USER",
            "status": "ACTIVE",
            "createdAt": int(time.time() * 1000)
        },
        {
            "username": "alice",
            "password": "alice123",
            "role": "USER",
            "status": "ACTIVE",
            "createdAt": int(time.time() * 1000)
        },
        {
            "username": "bob",
            "password": "bob123",
            "role": "USER",
            "status": "ACTIVE",
            "createdAt": int(time.time() * 1000)
        }
    ]
    for u in user_data:
        users.update_one({"username": u["username"]}, {"$set": u}, upsert=True)
    print(f"[OK] Injected {len(user_data)} app user accounts into MongoDB whisp_db.users")

    print("\nPre-injected credentials in your local MongoDB:")
    print("--------------------------------------------------")
    print("Web Admin Login : admin / whispadmin123")
    print("Web Operator    : operator / operator123")
    print("App User Login  : yashwanth / password123")
    print("App User Login  : user / whisp123")
    print("--------------------------------------------------")

if __name__ == "__main__":
    init_db()
