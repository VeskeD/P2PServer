package util;

import java.net.InetAddress;

public class Constants {
    public static final String HP_GET_ADDRESS = "HOLE_PUNCH_GET_ADDRESS";
    public static final String CLIENT_REGISTR = "CLIENT_REGISTRATION_REQUEST";

    public static final int SERVER_PORT = 12345;
    public static final String SERVER_NAME;

    static {
        String tmpName = "MY_SERVER";
        try {
            tmpName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}

        SERVER_NAME = tmpName;
    }
}
