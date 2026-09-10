import os
from setuptools import setup

version = "1.0.0"
version_file = os.path.join(os.path.dirname(__file__), "..", "VERSION")
if os.path.exists(version_file):
    with open(version_file, "r") as f:
        version = f.read().strip()

APP = ['gui.py']
DATA_FILES = ['cursor.png', 'server.py', 'input_controller.py', 'discovery.py']
OPTIONS = {
    'argv_emulation': True,
    'iconfile': 'icon.icns',
    'packages': ['PyQt6', 'pyngrok', 'pynput', 'websockets', 'zeroconf', 'mss', 'PIL', 'ifaddr', 'Quartz', 'AppKit', 'objc', 'Foundation', 'CoreFoundation'],
    'includes': ['PyObjCTools', 'PyObjCTools.KeyValueCoding'],
    'plist': {
        'CFBundleName': 'Mac Remote',
        'CFBundleDisplayName': 'Mac Remote',
        'CFBundleGetInfoString': 'Mac Remote Server',
        'CFBundleIdentifier': 'com.macremote.server',
        'CFBundleVersion': version,
        'CFBundleShortVersionString': version,
        'NSRequiresAquaSystemAppearance': False,
        'NSCameraUsageDescription': 'Used for screen capturing', # Sometimes needed
        'NSAppleEventsUsageDescription': 'Used for media keys',
    }
}

setup(
    app=APP,
    data_files=DATA_FILES,
    options={'py2app': OPTIONS},
    setup_requires=['py2app'],
)
