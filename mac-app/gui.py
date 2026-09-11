import sys
import os
import threading
import asyncio
from PyQt6.QtWidgets import QApplication, QMainWindow, QLabel, QPushButton, QVBoxLayout, QHBoxLayout, QWidget, QCheckBox, QLineEdit, QMessageBox, QComboBox
from PyQt6.QtCore import Qt, pyqtSignal, QObject, QSettings
from pyngrok import ngrok, conf
from server import RemoteServer, PORT

TEXTS = {
    "tr": {
        "title": "Mac Remote",
        "server_stopped": "Sunucu: Durduruldu",
        "server_running": "Sunucu: Çalışıyor",
        "start_server": "Sunucuyu Başlat",
        "stop_server": "Sunucuyu Durdur",
        "ngrok_enable": "İnternet Erişimini Aç (Ngrok)",
        "local_only": "Yalnızca Yerel Ağ",
        "starting_ngrok": "Ngrok Başlatılıyor...",
        "perm_granted": "✓ Erişilebilirlik İzni: Verildi",
        "perm_required": "⚠️ Erişilebilirlik İzni Gerekli (Tıkla)",
    },
    "en": {
        "title": "Mac Remote",
        "server_stopped": "Server: Stopped",
        "server_running": "Server: Running",
        "start_server": "Start Server",
        "stop_server": "Stop Server",
        "ngrok_enable": "Enable Internet Access (Ngrok)",
        "local_only": "Local Network Only",
        "starting_ngrok": "Starting Ngrok...",
        "perm_granted": "✓ Accessibility: Granted",
        "perm_required": "⚠️ Accessibility Permission Required (Click)",
    }
}

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
    ngrok_status_signal = pyqtSignal(str)

    def __init__(self):
        super().__init__()
        self.setWindowTitle("Mac Remote")
        self.setFixedSize(350, 360)
        self.settings = QSettings("MacRemote", "ServerApp")
        self.server_thread = None
        self.public_url = None
        self.ngrok_status_signal.connect(self.on_ngrok_status)
        
        layout = QVBoxLayout()
        
        # Language Selector Bar
        top_bar = QHBoxLayout()
        top_bar.addStretch()
        self.lang_combo = QComboBox()
        self.lang_combo.setCursor(Qt.CursorShape.PointingHandCursor)
        self.lang_combo.addItem("🇹🇷 Türkçe", "tr")
        self.lang_combo.addItem("🇬🇧 English", "en")
        saved_lang = self.settings.value("language", "tr")
        if saved_lang == "en":
            self.lang_combo.setCurrentIndex(1)
        else:
            self.lang_combo.setCurrentIndex(0)
        self.lang_combo.currentIndexChanged.connect(self.on_language_changed)
        top_bar.addWidget(self.lang_combo)
        layout.addLayout(top_bar)
        
        self.status_label = QLabel()
        self.status_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.status_label.setStyleSheet("font-size: 16px; font-weight: bold; color: gray;")
        layout.addWidget(self.status_label)
        
        self.pin_label = QLabel("----")
        self.pin_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.pin_label.setStyleSheet("font-size: 32px; font-weight: bold; margin-top: 6px; margin-bottom: 6px;")
        layout.addWidget(self.pin_label)

        self.ngrok_checkbox = QCheckBox()
        layout.addWidget(self.ngrok_checkbox)

        self.url_label = QLabel()
        self.url_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.url_label.setStyleSheet("color: #4a90e2; font-size: 12px;")
        self.url_label.setTextInteractionFlags(Qt.TextInteractionFlag.TextSelectableByMouse)
        self.url_label.setWordWrap(True)
        layout.addWidget(self.url_label)
        
        import input_controller
        self.perm_label = QLabel()
        self.perm_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.perm_label.mousePressEvent = lambda e: input_controller.prompt_accessibility_permission()
        layout.addWidget(self.perm_label)
        
        layout.addStretch()
        
        self.start_btn = QPushButton()
        self.start_btn.setStyleSheet("font-size: 16px; padding: 10px;")
        self.start_btn.clicked.connect(self.toggle_server)
        layout.addWidget(self.start_btn)
        
        container = QWidget()
        container.setLayout(layout)
        self.setCentralWidget(container)

        self.update_ui_texts()

    def tr_text(self, key):
        lang = self.lang_combo.currentData() if hasattr(self, 'lang_combo') else "tr"
        return TEXTS.get(lang, TEXTS["tr"]).get(key, "")

    def on_language_changed(self):
        lang = self.lang_combo.currentData()
        self.settings.setValue("language", lang)
        self.update_ui_texts()

    def update_ui_texts(self):
        is_running = self.server_thread is not None
        self.status_label.setText(self.tr_text("server_running") if is_running else self.tr_text("server_stopped"))
        self.start_btn.setText(self.tr_text("stop_server") if is_running else self.tr_text("start_server"))
        self.ngrok_checkbox.setText(self.tr_text("ngrok_enable"))
        
        if self.public_url:
            self.url_label.setText(f"{self.public_url.replace('http://', 'ws://').replace('https://', 'wss://')}")
        elif self.ngrok_checkbox.isChecked() and is_running:
            self.url_label.setText(self.tr_text("starting_ngrok"))
        else:
            self.url_label.setText(self.tr_text("local_only"))
            
        import input_controller
        if input_controller.is_accessibility_trusted():
            self.perm_label.setText(self.tr_text("perm_granted"))
            self.perm_label.setStyleSheet("color: #2ecc71; font-size: 11px;")
        else:
            self.perm_label.setText(self.tr_text("perm_required"))
            self.perm_label.setStyleSheet("color: #e74c3c; font-size: 11px; font-weight: bold;")

    def toggle_server(self):
        if self.server_thread is None:
            # Start
            self.server_thread = ServerThread()
            self.server_thread.start()
            self.pin_label.setText(f"{self.server_thread.server.pin}")
            self.status_label.setStyleSheet("font-size: 16px; font-weight: bold; color: #2ecc71;")
            self.ngrok_checkbox.setEnabled(False)
            
            if self.ngrok_checkbox.isChecked():
                self.url_label.setText(self.tr_text("starting_ngrok"))
                threading.Thread(target=self.start_ngrok, daemon=True).start()
            else:
                self.url_label.setText(self.tr_text("local_only"))
        else:
            # Stop
            self.server_thread.stop()
            self.server_thread = None
            self.pin_label.setText("----")
            self.status_label.setStyleSheet("font-size: 16px; font-weight: bold; color: gray;")
            self.ngrok_checkbox.setEnabled(True)
            self.url_label.setText(self.tr_text("local_only"))
            if self.public_url:
                try:
                    ngrok.disconnect(self.public_url)
                    ngrok.kill()
                except:
                    pass
                self.public_url = None
        self.update_ui_texts()

    def on_ngrok_status(self, text):
        self.url_label.setText(text)

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
            ws_url = url.replace('http://', 'ws://').replace('https://', 'wss://')
            self.ngrok_status_signal.emit(ws_url)
        except Exception as e:
            self.ngrok_status_signal.emit(f"Ngrok Error: {str(e)}")

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
