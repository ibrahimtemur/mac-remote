import subprocess
import Quartz
import AppKit
import mss

_sct = mss.mss()

def get_mouse_position():
    pos = Quartz.CGEventGetLocation(Quartz.CGEventCreate(None))
    return pos.x, pos.y

class MousePositionProxy:
    @property
    def position(self):
        return get_mouse_position()

mouse = MousePositionProxy()

def get_screen_bounds():
    monitor = _sct.monitors[1]
    return (
        float(monitor["left"]),
        float(monitor["top"]),
        float(monitor["left"] + monitor["width"] - 1),
        float(monitor["top"] + monitor["height"] - 1)
    )

_mouse_button_state = {"left": False, "right": False}

def handle_mouse_down(button_str="left"):
    global _mouse_button_state
    pos = Quartz.CGEventGetLocation(Quartz.CGEventCreate(None))
    btn_type = Quartz.kCGEventLeftMouseDown if button_str == "left" else Quartz.kCGEventRightMouseDown
    btn = Quartz.kCGMouseButtonLeft if button_str == "left" else Quartz.kCGMouseButtonRight
    _mouse_button_state[button_str] = True
    ev = Quartz.CGEventCreateMouseEvent(None, btn_type, (pos.x, pos.y), btn)
    Quartz.CGEventPost(Quartz.kCGHIDEventTap, ev)

def handle_mouse_up(button_str="left"):
    global _mouse_button_state
    pos = Quartz.CGEventGetLocation(Quartz.CGEventCreate(None))
    btn_type = Quartz.kCGEventLeftMouseUp if button_str == "left" else Quartz.kCGEventRightMouseUp
    btn = Quartz.kCGMouseButtonLeft if button_str == "left" else Quartz.kCGMouseButtonRight
    _mouse_button_state[button_str] = False
    ev = Quartz.CGEventCreateMouseEvent(None, btn_type, (pos.x, pos.y), btn)
    Quartz.CGEventPost(Quartz.kCGHIDEventTap, ev)

def handle_mouse_move(dx, dy):
    min_x, min_y, max_x, max_y = get_screen_bounds()
    pos = Quartz.CGEventGetLocation(Quartz.CGEventCreate(None))
    new_x = max(min_x, min(max_x, pos.x + dx))
    new_y = max(min_y, min(max_y, pos.y + dy))
    
    if _mouse_button_state.get("left", False):
        ev_type = Quartz.kCGEventLeftMouseDragged
    elif _mouse_button_state.get("right", False):
        ev_type = Quartz.kCGEventRightMouseDragged
    else:
        ev_type = Quartz.kCGEventMouseMoved

    event = Quartz.CGEventCreateMouseEvent(None, ev_type, (new_x, new_y), Quartz.kCGMouseButtonLeft)
    Quartz.CGEventSetIntegerValueField(event, Quartz.kCGMouseEventDeltaX, int(dx))
    Quartz.CGEventSetIntegerValueField(event, Quartz.kCGMouseEventDeltaY, int(dy))
    Quartz.CGEventPost(Quartz.kCGHIDEventTap, event)

def handle_absolute_move(x_percent, y_percent):
    min_x, min_y, max_x, max_y = get_screen_bounds()
    monitor = _sct.monitors[1]
    target_x = max(min_x, min(max_x, monitor["left"] + monitor["width"] * x_percent))
    target_y = max(min_y, min(max_y, monitor["top"] + monitor["height"] * y_percent))
    pos = Quartz.CGEventGetLocation(Quartz.CGEventCreate(None))
    dx = int(target_x - pos.x)
    dy = int(target_y - pos.y)
    
    event = Quartz.CGEventCreateMouseEvent(None, Quartz.kCGEventMouseMoved, (target_x, target_y), Quartz.kCGMouseButtonLeft)
    Quartz.CGEventSetIntegerValueField(event, Quartz.kCGMouseEventDeltaX, dx)
    Quartz.CGEventSetIntegerValueField(event, Quartz.kCGMouseEventDeltaY, dy)
    Quartz.CGEventPost(Quartz.kCGHIDEventTap, event)

def handle_absolute_drag(x_percent, y_percent):
    min_x, min_y, max_x, max_y = get_screen_bounds()
    monitor = _sct.monitors[1]
    target_x = max(min_x, min(max_x, monitor["left"] + monitor["width"] * x_percent))
    target_y = max(min_y, min(max_y, monitor["top"] + monitor["height"] * y_percent))
    pos = Quartz.CGEventGetLocation(Quartz.CGEventCreate(None))
    dx = int(target_x - pos.x)
    dy = int(target_y - pos.y)
    
    event = Quartz.CGEventCreateMouseEvent(None, Quartz.kCGEventLeftMouseDragged, (target_x, target_y), Quartz.kCGMouseButtonLeft)
    Quartz.CGEventSetIntegerValueField(event, Quartz.kCGMouseEventDeltaX, dx)
    Quartz.CGEventSetIntegerValueField(event, Quartz.kCGMouseEventDeltaY, dy)
    Quartz.CGEventPost(Quartz.kCGHIDEventTap, event)

import time
import ApplicationServices

def is_accessibility_trusted():
    try:
        return ApplicationServices.AXIsProcessTrusted()
    except Exception:
        return True

def prompt_accessibility_permission():
    try:
        opts = {ApplicationServices.kAXTrustedCheckOptionPrompt: True}
        return ApplicationServices.AXIsProcessTrustedWithOptions(opts)
    except Exception:
        return False

def handle_mouse_click(button_str, click_count=1):
    pos = Quartz.CGEventGetLocation(Quartz.CGEventCreate(None))
    if button_str == "left":
        down_type = Quartz.kCGEventLeftMouseDown
        up_type = Quartz.kCGEventLeftMouseUp
        btn = Quartz.kCGMouseButtonLeft
    elif button_str == "right":
        down_type = Quartz.kCGEventRightMouseDown
        up_type = Quartz.kCGEventRightMouseUp
        btn = Quartz.kCGMouseButtonRight
    else:
        down_type = Quartz.kCGEventOtherMouseDown
        up_type = Quartz.kCGEventOtherMouseUp
        btn = Quartz.kCGMouseButtonCenter

    # Execute clicks with proper clickState (1 for single, 2 for double)
    for c in range(1, click_count + 1):
        down_event = Quartz.CGEventCreateMouseEvent(None, down_type, (pos.x, pos.y), btn)
        Quartz.CGEventSetIntegerValueField(down_event, Quartz.kCGMouseEventClickState, c)
        Quartz.CGEventPost(Quartz.kCGHIDEventTap, down_event)

        time.sleep(0.025)

        up_event = Quartz.CGEventCreateMouseEvent(None, up_type, (pos.x, pos.y), btn)
        Quartz.CGEventSetIntegerValueField(up_event, Quartz.kCGMouseEventClickState, c)
        Quartz.CGEventPost(Quartz.kCGHIDEventTap, up_event)

        if c < click_count:
            time.sleep(0.05)

def handle_scroll(dx, dy):
    # On macOS, scrollwheel event: dy > 0 is scroll up, dy < 0 is scroll down
    scroll_event = Quartz.CGEventCreateScrollWheelEvent(None, 1, 2, int(dy), int(dx))
    Quartz.CGEventPost(Quartz.kCGHIDEventTap, scroll_event)

# macOS virtual key codes mapping
KEY_CODES = {
    "enter": 36,
    "return": 36,
    "tab": 48,
    "space": 49,
    "backspace": 51,
    "esc": 53,
    "escape": 53,
    "cmd": 55,
    "command": 55,
    "shift": 56,
    "opt": 58,
    "alt": 58,
    "ctrl": 59,
    "control": 59,
    "up": 126,
    "down": 125,
    "left": 123,
    "right": 124,
    "f": 3,
    "m": 46
}

def handle_key_press(key_char):
    try:
        down_ev = Quartz.CGEventCreateKeyboardEvent(None, 0, True)
        Quartz.CGEventKeyboardSetUnicodeString(down_ev, len(key_char), key_char)
        up_ev = Quartz.CGEventCreateKeyboardEvent(None, 0, False)
        Quartz.CGEventKeyboardSetUnicodeString(up_ev, len(key_char), key_char)
        Quartz.CGEventPost(Quartz.kCGHIDEventTap, down_ev)
        time.sleep(0.015)
        Quartz.CGEventPost(Quartz.kCGHIDEventTap, up_ev)
    except Exception as e:
        print(f"Error pressing key {key_char}: {e}")

def handle_special_key(key_str):
    code = KEY_CODES.get(key_str.lower())
    if code is not None:
        down_ev = Quartz.CGEventCreateKeyboardEvent(None, code, True)
        up_ev = Quartz.CGEventCreateKeyboardEvent(None, code, False)
        Quartz.CGEventPost(Quartz.kCGHIDEventTap, down_ev)
        time.sleep(0.02)
        Quartz.CGEventPost(Quartz.kCGHIDEventTap, up_ev)
    else:
        print(f"Special key not found: {key_str}")

def _post_system_media_key(key):
    # key: 16 = Play/Pause, 19 = Next, 20 = Prev, 0 = Vol Up, 1 = Vol Down
    try:
        for state in (0xa, 0xb):
            flags = state << 8
            data1 = (key << 16) | flags
            ev = AppKit.NSEvent.otherEventWithType_location_modifierFlags_timestamp_windowNumber_context_subtype_data1_data2_(
                14, (0, 0), flags, 0, 0, None, 8, data1, -1
            )
            Quartz.CGEventPost(Quartz.kCGHIDEventTap, ev.CGEvent())
    except Exception as e:
        print(f"Error posting media key: {e}")

def handle_media(action):
    if action == "vol_up":
        try:
            subprocess.run(["osascript", "-e", "set volume output volume ((output volume of (get volume settings)) + 6)"], check=False)
        except Exception:
            _post_system_media_key(0)
    elif action == "vol_down":
        try:
            subprocess.run(["osascript", "-e", "set volume output volume ((output volume of (get volume settings)) - 6)"], check=False)
        except Exception:
            _post_system_media_key(1)
    elif action == "mute":
        try:
            subprocess.run(["osascript", "-e", "set volume output muted not (output muted of (get volume settings))"], check=False)
        except Exception:
            handle_special_key("m")
    elif action == "play_pause":
        handled = False
        try:
            scpt = '''
            if application "Spotify" is running then
                tell application "Spotify" to playpause
                return "spotify"
            else if application "Music" is running then
                tell application "Music" to playpause
                return "music"
            else
                return "none"
            end if
            '''
            res = subprocess.run(["osascript", "-e", scpt], capture_output=True, text=True).stdout.strip()
            if res in ("spotify", "music"):
                handled = True
        except Exception:
            pass

        _post_system_media_key(16)
        if not handled:
            handle_special_key("space")

    elif action in ("next", "forward"):
        handled = False
        if action == "next":
            try:
                scpt = '''
                if application "Spotify" is running then
                    tell application "Spotify" to next track
                    return "spotify"
                else if application "Music" is running then
                    tell application "Music" to next track
                    return "music"
                else
                    return "none"
                end if
                '''
                res = subprocess.run(["osascript", "-e", scpt], capture_output=True, text=True).stdout.strip()
                if res in ("spotify", "music"):
                    handled = True
            except Exception:
                pass
            _post_system_media_key(19)

        if not handled or action == "forward":
            # For YouTube, Netflix, QuickTime, VLC: right arrow skips 5s forward
            handle_special_key("right")

    elif action in ("prev", "rewind"):
        handled = False
        if action == "prev":
            try:
                scpt = '''
                if application "Spotify" is running then
                    tell application "Spotify" to previous track
                    return "spotify"
                else if application "Music" is running then
                    tell application "Music" to previous track
                    return "music"
                else
                    return "none"
                end if
                '''
                res = subprocess.run(["osascript", "-e", scpt], capture_output=True, text=True).stdout.strip()
                if res in ("spotify", "music"):
                    handled = True
            except Exception:
                pass
            _post_system_media_key(20)

        if not handled or action == "rewind":
            # For YouTube, Netflix, QuickTime, VLC: left arrow skips 5s backward
            handle_special_key("left")

def handle_system(action):
    if action == "sleep":
        subprocess.run(["pmset", "displaysleepnow"])

