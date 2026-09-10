import sys
import threading
import asyncio
from PyQt6.QtWidgets import QApplication, QMainWindow, QLabel, QPushButton, QVBoxLayout, QWidget, QCheckBox, QLineEdit, QMessageBox
from PyQt6.QtCore import Qt, pyqtSignal, QObject, QSettings
from pyngrok import ngrok, conf
from server import RemoteServer, PORT

class ServerThread(threading.Thread):
    def __init__(self):
        super().__init__()
        self.server = RemoteServer()
        self.loop = None
        self.daemon = True

    def run(self):
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        try:
            self.loop.run_until_complete(self.server.start())
        except Exception as e:
            pass

    def stop(self):
        if self.loop:
            self.loop.call_soon_threadsafe(self.loop.stop)
        self.server.discovery.stop()

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Mac Remote")
        self.setFixedSize(350, 320)
        self.settings = QSettings("MacRemote", "ServerApp")
        
        layout = QVBoxLayout()
        
        self.status_label = QLabel("Server: Stopped")
        self.status_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.status_label.setStyleSheet("font-size: 16px; font-weight: bold; color: gray;")
        layout.addWidget(self.status_label)
        
        self.pin_label = QLabel("----")
        self.pin_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.pin_label.setStyleSheet("font-size: 32px; font-weight: bold; margin-top: 10px; margin-bottom: 10px;")
        layout.addWidget(self.pin_label)

        self.ngrok_checkbox = QCheckBox("Enable Internet Access (Ngrok)")
        layout.addWidget(self.ngrok_checkbox)

        self.url_label = QLabel("Local Network Only")
        self.url_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.url_label.setStyleSheet("color: #4a90e2; font-size: 12px;")
        self.url_label.setTextInteractionFlags(Qt.TextInteractionFlag.TextSelectableByMouse)
        self.url_label.setWordWrap(True)
        layout.addWidget(self.url_label)
        
        import input_controller
        self.perm_label = QLabel()
        self.perm_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        if input_controller.is_accessibility_trusted():
            self.perm_label.setText("✓ Erişilebilirlik İzni: Verildi")
            self.perm_label.setStyleSheet("color: #2ecc71; font-size: 11px;")
        else:
            self.perm_label.setText("⚠️ Erişilebilirlik İzni Gerekli (Tıkla)")
            self.perm_label.setStyleSheet("color: #e74c3c; font-size: 11px; font-weight: bold;")
            self.perm_label.mousePressEvent = lambda e: input_controller.prompt_accessibility_permission()
        layout.addWidget(self.perm_label)
        
        layout.addStretch()
        
        self.start_btn = QPushButton("Start Server")
        self.start_btn.setStyleSheet("font-size: 16px; padding: 10px;")
        self.start_btn.clicked.connect(self.toggle_server)
        layout.addWidget(self.start_btn)
        
        container = QWidget()
        container.setLayout(layout)
        self.setCentralWidget(container)
        
        self.server_thread = None
        self.public_url = None

    def toggle_server(self):
        if self.server_thread is None:
            # Start
            self.server_thread = ServerThread()
            self.server_thread.start()
            self.pin_label.setText(f"{self.server_thread.server.pin}")
            self.status_label.setText("Server: Running")
            self.status_label.setStyleSheet("font-size: 16px; font-weight: bold; color: #2ecc71;")
            self.start_btn.setText("Stop Server")
            self.ngrok_checkbox.setEnabled(False)
            
            if self.ngrok_checkbox.isChecked():
                self.url_label.setText("Starting Ngrok...")
                threading.Thread(target=self.start_ngrok, daemon=True).start()
            else:
                self.url_label.setText("Local Network Only")
        else:
            # Stop
            self.server_thread.stop()
            self.server_thread = None
            self.pin_label.setText("----")
            self.status_label.setText("Server: Stopped")
            self.status_label.setStyleSheet("font-size: 16px; font-weight: bold; color: gray;")
            self.start_btn.setText("Start Server")
            self.ngrok_checkbox.setEnabled(True)
            self.url_label.setText("Local Network Only")
            if self.public_url:
                try:
                    ngrok.disconnect(self.public_url)
                    ngrok.kill()
                except:
                    pass
                self.public_url = None

    def start_ngrok(self):
        try:
            conf.get_default().region = "eu"
            token = os.environ.get("NGROK_AUTHTOKEN") or self.settings.value("ngrok_token", "")
            if token:
                ngrok.set_auth_token(token)
            try:
                ngrok.kill()
            except:
                pass
            tunnel = ngrok.connect(PORT, "http")
            url = tunnel.public_url
            self.public_url = url
            self.url_label.setText(f"{url.replace('http://', 'ws://').replace('https://', 'wss://')}")
        except Exception as e:
            self.url_label.setText(f"Ngrok Error: {str(e)}")

    def closeEvent(self, event):
        if self.public_url:
            try:
                ngrok.disconnect(self.public_url)
                ngrok.kill()
            except:
                pass
        if self.server_thread:
            self.server_thread.stop()
        event.accept()

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())
