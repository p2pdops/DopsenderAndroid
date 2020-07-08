package p2pdops.dopsender.local_connection;

import android.net.wifi.p2p.WifiP2pDevice;

public class LocalP2PDevice {

    private WifiP2pDevice localDevice;

    private static final LocalP2PDevice instance = new LocalP2PDevice();

    public static LocalP2PDevice getInstance() {
        return instance;
    }

    private LocalP2PDevice() {
        localDevice = new WifiP2pDevice();
    }

    public WifiP2pDevice getLocalDevice() {
        return localDevice;
    }

    public void setLocalDevice(WifiP2pDevice localDevice) {
        this.localDevice = localDevice;
    }
}

