#!/usr/bin/env python3
"""
Roommate Expenses & Life Hub - Python Backend Server
Serves the React.js + HTML + CSS web application from /public
and provides RESTful JSON API endpoints for roommates, expenses,
chore matrix, and utility bill countdowns.

Usage:
    python server.py [port]
    (Default port: 8000 -> Open http://localhost:8000)
"""

import json
import os
import sys
import time
from http.server import SimpleHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs

DATA_FILE = os.path.join(os.path.dirname(__file__), "roommate_data.json")
PUBLIC_DIR = os.path.join(os.path.dirname(__file__), "public")

DEFAULT_DATABASE = {
    "roommates": [
        {"id": "1", "name": "Vinod Kumar", "email": "uvinodkumar614@gmail.com", "phone": "+91 98765 43210", "isAdmin": True, "pin": "1234", "color": "#4F46E5"},
        {"id": "2", "name": "Alex Morgan", "email": "alex.m@roommail.com", "phone": "+91 98765 43211", "isAdmin": True, "pin": "1234", "color": "#10B981"},
        {"id": "3", "name": "David Chen", "email": "david.c@roommail.com", "phone": "+91 98765 43212", "isAdmin": False, "pin": "1234", "color": "#F59E0B"},
        {"id": "4", "name": "Sam Wilson", "email": "sam.w@roommail.com", "phone": "+91 98765 43213", "isAdmin": False, "pin": "1234", "color": "#EC4899"}
    ],
    "deposits": [
        {"id": "dep-1", "memberId": "1", "memberName": "Vinod Kumar", "amount": 5000.0, "timestamp": int(time.time() * 1000) - 86400000 * 10, "note": "Shared wallet deposit"},
        {"id": "dep-2", "memberId": "2", "memberName": "Alex Morgan", "amount": 5000.0, "timestamp": int(time.time() * 1000) - 86400000 * 10, "note": "Shared wallet deposit"},
        {"id": "dep-3", "memberId": "3", "memberName": "David Chen", "amount": 5000.0, "timestamp": int(time.time() * 1000) - 86400000 * 10, "note": "Shared wallet deposit"},
        {"id": "dep-4", "memberId": "4", "memberName": "Sam Wilson", "amount": 5000.0, "timestamp": int(time.time() * 1000) - 86400000 * 10, "note": "Shared wallet deposit"}
    ],
    "expenses": [
        {"id": "exp-1", "title": "Monthly Room Rent", "amount": 12000.0, "paidByMemberId": "1", "paidByMemberName": "Vinod Kumar", "category": "Rent", "isRoomExpense": True, "paymentSource": "POOL", "timestamp": int(time.time() * 1000) - 86400000 * 5, "notes": "Room rent debited from wallet pool"},
        {"id": "exp-2", "title": "Weekly Grocery Run", "amount": 1500.0, "paidByMemberId": "2", "paidByMemberName": "Alex Morgan", "category": "Groceries", "isRoomExpense": True, "paymentSource": "POOL", "timestamp": int(time.time() * 1000) - 86400000 * 3, "notes": "Vegetables, milk, snacks"},
        {"id": "exp-3", "title": "WiFi Fiber Broadband", "amount": 799.0, "paidByMemberId": "4", "paidByMemberName": "Sam Wilson", "category": "Utilities", "isRoomExpense": True, "paymentSource": "POOL", "timestamp": int(time.time() * 1000) - 86400000 * 2, "notes": "Monthly high speed internet"}
    ],
    "events": [
        {"id": "ev-1", "title": "Kitchen Counter & Stove Deep Clean", "category": "CLEANING", "assignedMemberName": "Alex Morgan", "assignedMemberColor": "#10B981", "dayOfWeek": 1, "isCompleted": False, "recurrence": "WEEKLY"},
        {"id": "ev-2", "title": "Living Room Sweep & Vacuum", "category": "CLEANING", "assignedMemberName": "David Chen", "assignedMemberColor": "#F59E0B", "dayOfWeek": 2, "isCompleted": False, "recurrence": "WEEKLY"},
        {"id": "ev-3", "title": "Monday Dinner: Paneer & Roti", "category": "COOKING", "assignedMemberName": "Vinod Kumar", "assignedMemberColor": "#4F46E5", "dayOfWeek": 1, "isCompleted": False, "recurrence": "WEEKLY"},
        {"id": "ev-4", "title": "Electricity Board Power Bill", "category": "UTILITY_BILL", "assignedMemberName": "Vinod Kumar", "assignedMemberColor": "#4F46E5", "isBill": True, "billAmount": 1250.0, "daysLeft": 3, "isCompleted": False}
    ]
}

def load_data():
    if os.path.exists(DATA_FILE):
        try:
            with open(DATA_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return DEFAULT_DATABASE
    return DEFAULT_DATABASE

def save_data(data):
    with open(DATA_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

class RoommateHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=PUBLIC_DIR, **kwargs)

    def _send_json(self, status_code, data):
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()
        self.wfile.write(json.dumps(data).encode("utf-8"))

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/api/data":
            self._send_json(200, load_data())
            return
        elif parsed.path == "/api/expenses":
            data = load_data()
            self._send_json(200, data.get("expenses", []))
            return
        elif parsed.path == "/api/roommates":
            data = load_data()
            self._send_json(200, data.get("roommates", []))
            return
        elif parsed.path == "/api/events":
            data = load_data()
            self._send_json(200, data.get("events", []))
            return

        # Fallback to serving static HTML / React files from /public
        super().do_GET()

    def do_POST(self):
        parsed = urlparse(self.path)
        content_length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(content_length).decode("utf-8") if content_length > 0 else "{}"
        try:
            payload = json.loads(body)
        except Exception:
            payload = {}

        data = load_data()

        if parsed.path == "/api/expenses":
            payload["id"] = f"exp-{int(time.time() * 1000)}"
            payload["timestamp"] = int(time.time() * 1000)
            data["expenses"].insert(0, payload)
            save_data(data)
            self._send_json(201, {"status": "success", "expense": payload})
            return
        elif parsed.path == "/api/deposits":
            payload["id"] = f"dep-{int(time.time() * 1000)}"
            payload["timestamp"] = int(time.time() * 1000)
            data["deposits"].insert(0, payload)
            save_data(data)
            self._send_json(201, {"status": "success", "deposit": payload})
            return
        elif parsed.path == "/api/events":
            payload["id"] = f"ev-{int(time.time() * 1000)}"
            data["events"].append(payload)
            save_data(data)
            self._send_json(201, {"status": "success", "event": payload})
            return

        self._send_json(404, {"error": "Endpoint not found"})

def run_server(port=8000):
    server_address = ("", port)
    httpd = HTTPServer(server_address, RoommateHandler)
    print(f"🚀 Roommate Life & Expenses Web Server running at http://localhost:{port}")
    print(f"📁 Serving React.js + HTML + CSS from: {PUBLIC_DIR}")
    print("✨ REST API endpoints available at /api/data, /api/expenses, /api/events")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping server...")
        httpd.server_close()

if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8000
    run_server(port)
