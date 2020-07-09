package p2pdops.dopsender.services;

import java.net.InetAddress;


public class WifiServiceUtils {

    public static int iNetAddressToInt(InetAddress inetAddress)
            throws IllegalArgumentException {
        byte[] address = inetAddress.getAddress();
        if (address.length != 4) {
            throw new IllegalArgumentException("Not an IPv4 address");
        }
        return ((address[3] & 0xff) << 24) | ((address[2] & 0xff) << 16) |
                ((address[1] & 0xff) << 8) | (address[0] & 0xff);
    }
}