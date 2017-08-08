package srv;

import static util.Constants.HP_GET_ADDRESS;
import static util.Constants.CLIENT_REGISTR;

import static util.Constants.SERVER_PORT;
import static util.Constants.SERVER_NAME;
import static util.Logger.log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P2PServer {
    private static DatagramSocket socket = null;
    private static boolean isSocketInitialized = false;

    private static Map<String, Client> peers = new HashMap<>();

    public static void main(String[] args) {
        log.info(SERVER_NAME + " - Main executed");

        try {
            socket = new DatagramSocket(SERVER_PORT);
            isSocketInitialized = true;
            log.info(SERVER_NAME + " socket is initialized");
        } catch (Exception e) {
            log.error("Cannot initialize socket", e);
        }

        new Thread(new Listener(SERVER_NAME + "_" + "L1")).start();
        new Thread(new Listener(SERVER_NAME + "_" + "L2")).start();
    }

    private static void clientRegistration(String clientName, InetAddress address, int port) {
        Address newAddress = new Address(address, port);
        Client client = new Client();
        client.putAddress(newAddress);

        peers.put(clientName, client);

        log.info(clientName + " - registered");
    }

    private static class Listener implements Runnable {
        String name;

        Listener(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            log.info(name + " started");

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while(isSocketInitialized) {
                try {
                    log.info(name + " is READY TO GET PACKET");
                    socket.receive(packet);

                    String msg = new String(packet.getData());
                    log.info(name + " < " + packet.getAddress() + ":" + packet.getPort() + " - " + msg);

                    switch (msg.split("::")[1].split(":")[0]) {
                        case HP_GET_ADDRESS:
                            log.info(name + " GetAddress requested by " + msg.split("::")[0]);

                            String address = peers.get(msg.split("::")[1].split(":")[1]).getAddresses().get(0).getAddress().toString();
                            String port = String.valueOf(peers.get(msg.split("::")[1].split(":")[1]).getAddresses().get(0).getPort());

                            byte[] tmpBuffer = (name + "::" + HP_GET_ADDRESS + ":" + address + ":" + port + ":").getBytes();

                            DatagramPacket tmpPacket = new DatagramPacket(tmpBuffer, tmpBuffer.length);
                            tmpPacket.setSocketAddress(new InetSocketAddress(packet.getAddress(), packet.getPort()));

                            socket.send(tmpPacket);
                        break;

                        case CLIENT_REGISTR:
                            log.info(name + " Registration of " + new String(packet.getData()));
                            clientRegistration(msg.split("::")[0], packet.getAddress(), packet.getPort());
                        break;
                    }
                } catch (Exception e) {
                    log.error("packet cannot be received", e);
                }
            }
        }
    }

    private static class Client {
        private List<Address> addresses = new ArrayList<>();

        void putAddress(Address address) {
            this.addresses.add(address);
        }

        List<Address> getAddresses() {
            return addresses;
        }
    }

    static class Address {
        private InetAddress address;
        private int port;

        Address(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        InetAddress getAddress() {
            return address;
        }

        int getPort() {
            return port;
        }
    }
}
