import asyncio
import websockets
import json
import random
import string
import input_controller
import mss
import io
from PIL import Image
from discovery import MacRemoteDiscovery

PORT = 8765

import time

def generate_pin():
    return ''.join(random.choices(string.digits, k=4))

class RemoteServer:
    def __init__(self):
        self.pin = generate_pin()
        self.discovery = MacRemoteDiscovery(PORT, self.pin)
        self.authenticated_clients = set()
        self.client_ready = {}
        self.client_last_sent = {}
        self.sct = mss.mss()
        
        self.show_cursor = True
        import os, sys
        self.cursor_img = None
        candidates = [
            os.path.join(os.path.dirname(__file__), "cursor.png"),
            os.path.join(os.getcwd(), "cursor.png"),
            os.path.join(getattr(sys, "_MEIPASS", ""), "cursor.png"),
            os.path.join(os.path.dirname(sys.executable), "cursor.png"),
            os.path.join(os.path.dirname(sys.executable), "..", "Resources", "cursor.png"),
        ]
        for cp in candidates:
            if cp and os.path.exists(cp):
                try:
                    c_img = Image.open(cp).convert("RGBA")
                    # Resize to crisp 26x26 for 800px preview
                    self.cursor_img = c_img.resize((26, 26), Image.Resampling.LANCZOS)
                    break
                except Exception as e:
                    print(f"Error loading cursor from {cp}: {e}")
                    
        if self.cursor_img is None:
            try:
                from PIL import ImageDraw
                c_img = Image.new("RGBA", (26, 26), (0, 0, 0, 0))
                draw = ImageDraw.Draw(c_img)
                points = [
                    (0, 0),
                    (0, 20),
                    (5, 16),
                    (9, 23),
                    (12, 21),
                    (8, 14),
                    (14, 14)
                ]
                draw.polygon(points, fill=(255, 255, 255, 255), outline=(0, 0, 0, 255))
                self.cursor_img = c_img
            except Exception as e:
                print(f"Failed to generate fallback cursor: {e}")

        self.target_width = 1600
        self.jpeg_quality = 72

    async def broadcast_screen(self):
        while True:
            if not self.authenticated_clients:
                await asyncio.sleep(0.3)
                continue

            current_time = time.time()
            # Only send to clients that acknowledged previous frame or timed out (> 0.25s)
            ready_clients = [
                c for c in self.authenticated_clients
                if self.client_ready.get(c, True) or (current_time - self.client_last_sent.get(c, 0) > 0.25)
            ]

            if not ready_clients:
                await asyncio.sleep(0.01)
                continue
                
            try:
                # Capture screen
                monitor = self.sct.monitors[1]
                sct_img = self.sct.grab(monitor)
                
                # Convert to PIL Image
                img = Image.frombytes("RGB", sct_img.size, sct_img.bgra, "raw", "BGRX")
                
                # Resize for smooth remote usage (dynamic resolution)
                orig_w = img.width
                orig_h = img.height
                target_w = self.target_width
                if orig_w > target_w:
                    ratio = float(target_w) / float(orig_w)
                    new_h = int(float(orig_h) * ratio)
                    img = img.resize((target_w, new_h), Image.Resampling.BILINEAR)
                else:
                    ratio = 1.0
                    new_h = orig_h

                # Draw crisp cursor overlay directly onto resized preview
                if self.show_cursor and self.cursor_img:
                    try:
                        mx, my = input_controller.mouse.position
                        rel_x = mx - monitor["left"]
                        rel_y = my - monitor["top"]
                        if 0 <= rel_x < monitor["width"] and 0 <= rel_y < monitor["height"]:
                            cur_x = int(rel_x * ratio)
                            cur_y = int(rel_y * ratio)
                            img.paste(self.cursor_img, (cur_x, cur_y), self.cursor_img)
                    except Exception:
                        pass
                    
                # Save to JPEG bytes with dynamic quality
                buf = io.BytesIO()
                img.save(buf, format="JPEG", quality=self.jpeg_quality)
                image_bytes = buf.getvalue()
                
                # Broadcast to ready clients only
                dead_clients = set()
                for client in ready_clients:
                    try:
                        self.client_ready[client] = False
                        self.client_last_sent[client] = current_time
                        await client.send(image_bytes)
                    except Exception:
                        dead_clients.add(client)
                        
                for client in dead_clients:
                    self.authenticated_clients.discard(client)
                    self.client_ready.pop(client, None)
                    self.client_last_sent.pop(client, None)
                    
            except Exception as e:
                print(f"Screen capture error: {e}")
                
            # Prevent busy-looping
            await asyncio.sleep(0.02)

    async def handle_client(self, websocket):
        client_address = websocket.remote_address
        print(f"Client connected: {client_address}")
        self.client_ready[websocket] = True
        self.client_last_sent[websocket] = 0
        
        try:
            async for message in websocket:
                try:
                    data = json.loads(message)
                    msg_type = data.get("type")

                    if msg_type == "screen_ack":
                        self.client_ready[websocket] = True
                        continue
                    
                    if msg_type == "auth":
                        provided_pin = data.get("pin")
                        if provided_pin == self.pin:
                            self.authenticated_clients.add(websocket)
                            self.client_ready[websocket] = True
                            await websocket.send(json.dumps({"type": "auth_response", "status": "success"}))
                            print(f"Client {client_address} authenticated.")
                        else:
                            await websocket.send(json.dumps({"type": "auth_response", "status": "error", "message": "Invalid PIN"}))
                            print(f"Client {client_address} failed authentication.")
                        continue

                    if websocket not in self.authenticated_clients:
                        await websocket.send(json.dumps({"type": "error", "message": "Not authenticated"}))
                        continue

                    # Handle relative commands (fallback)
                    if msg_type == "mouse_move":
                        input_controller.handle_mouse_move(data.get("dx", 0), data.get("dy", 0))
                    # Handle absolute commands
                    elif msg_type == "absolute_move":
                        input_controller.handle_absolute_move(data.get("x", 0.5), data.get("y", 0.5))
                    elif msg_type == "absolute_drag":
                        input_controller.handle_absolute_drag(data.get("x", 0.5), data.get("y", 0.5))
                    
                    elif msg_type == "mouse_click":
                        input_controller.handle_mouse_click(data.get("button", "left"), data.get("count", 1))

                    elif msg_type == "mouse_down":
                        input_controller.handle_mouse_down(data.get("button", "left"))

                    elif msg_type == "mouse_up":
                        input_controller.handle_mouse_up(data.get("button", "left"))

                    elif msg_type == "set_quality":
                        level = data.get("level", "high")
                        if level == "low":
                            self.target_width = 800
                            self.jpeg_quality = 32
                        elif level == "medium":
                            self.target_width = 1200
                            self.jpeg_quality = 55
                        elif level == "high":
                            self.target_width = 1600
                            self.jpeg_quality = 75
                        elif level == "ultra":
                            self.target_width = 2200
                            self.jpeg_quality = 88
                        print(f"Preview quality adjusted to '{level}': width={self.target_width}, quality={self.jpeg_quality}")
                        
                    elif msg_type == "toggle_cursor":
                        if "show" in data:
                            self.show_cursor = bool(data["show"])
                        else:
                            self.show_cursor = not self.show_cursor
                        print(f"Cursor overlay in preview set to: {self.show_cursor}")
                        
                    elif msg_type == "scroll":
                        input_controller.handle_scroll(data.get("dx", 0), data.get("dy", 0))
                        
                    elif msg_type == "key_press":
                        input_controller.handle_key_press(data.get("key", ""))
                        
                    elif msg_type == "special_key":
                        input_controller.handle_special_key(data.get("key", ""))
                        
                    elif msg_type == "media":
                        input_controller.handle_media(data.get("action", ""))
                        
                    elif msg_type == "system":
                        input_controller.handle_system(data.get("action", ""))
                        
                except json.JSONDecodeError:
                    print("Invalid JSON received.")
                except Exception as e:
                    print(f"Error handling message: {e}")

        except websockets.exceptions.ConnectionClosed:
            pass
        finally:
            print(f"Client disconnected: {client_address}")
            if websocket in self.authenticated_clients:
                self.authenticated_clients.remove(websocket)
            self.client_ready.pop(websocket, None)
            self.client_last_sent.pop(websocket, None)

    async def start(self):
        print(f"--- Mac Remote Server Started ---")
        print(f"PIN for connection: {self.pin}")
        
        # Ensure macOS accessibility permission is requested
        if not input_controller.is_accessibility_trusted():
            print("WARNING: Accessibility permissions not yet granted. Prompting user...")
            input_controller.prompt_accessibility_permission()
        else:
            print("Accessibility permissions: OK")
        
        self.discovery.start()
        
        # Start broadcaster task
        asyncio.create_task(self.broadcast_screen())
        
        async with websockets.serve(self.handle_client, "0.0.0.0", PORT):
            await asyncio.Future()

if __name__ == "__main__":
    server = RemoteServer()
    try:
        asyncio.run(server.start())
    except KeyboardInterrupt:
        print("\nShutting down server...")
        server.discovery.stop()
