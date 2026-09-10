import socket
from zeroconf import ServiceInfo, Zeroconf
import time

class MacRemoteDiscovery:
    def __init__(self, port, pin):
        self.port = port
        self.pin = pin
        self.zeroconf = Zeroconf()
        self.service_info = None

    def get_local_ip(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            # doesn't even have to be reachable
            s.connect(('10.255.255.255', 1))
            IP = s.getsockname()[0]
        except Exception:
            IP = '127.0.0.1'
        finally:
            s.close()
        return IP

    def start(self):
        ip = self.get_local_ip()
        hostname = socket.gethostname()
        
        desc = {'pin_required': 'true'}
        
        self.service_info = ServiceInfo(
            "_macremote._tcp.local.",
            f"{hostname}._macremote._tcp.local.",
            addresses=[socket.inet_aton(ip)],
            port=self.port,
            properties=desc,
            server=f"{hostname}.local.",
        )
        
        self.zeroconf.register_service(self.service_info)
        print(f"mDNS Broadcasting as {hostname}._macremote._tcp.local. on {ip}:{self.port}")

    def stop(self):
        if self.service_info:
            self.zeroconf.unregister_service(self.service_info)
        self.zeroconf.close()

if __name__ == "__main__":
    # Test
    disc = MacRemoteDiscovery(8765, "1234")
    disc.start()
    try:
        time.sleep(10)
    except KeyboardInterrupt:
        pass
    finally:
        disc.stop()
