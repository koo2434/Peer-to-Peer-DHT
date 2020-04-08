
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingProcessService implements Runnable {

    private DatagramSocket socket;
    private int nodeID;

    public PingProcessService (DatagramSocket socket, int ID) {
        this.socket = socket;
        this.nodeID = ID;
    }

    @Override
    public void run() {
        while (true) {
            byte[] responseBytes = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(responseBytes, responseBytes.length);

            try{
                socket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            SocketAddress clientAddr = receivePacket.getSocketAddress();
            String data = new String(receivePacket.getData()).trim();
            String type = data.split(":", 2)[0].trim();
            int clientID = Integer.parseInt(data.split(":")[1].trim());

            System.out.println(data);
            System.out.println(type);
            System.out.println(clientID);

            if (type.equals("REQUEST")) {
                //TODO: Answer Ping
                System.out.println("Ping request received from Peer " + clientID);

                String pingMsg= "RESPONSE:" + this.nodeID;
                byte[] msgBytes = pingMsg.getBytes();
                DatagramPacket pingPacket = new DatagramPacket(msgBytes, msgBytes.length, clientAddr);
                try {
                    socket.send(pingPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (type.equals("RESPONSE")) {
                System.out.println("Ping response received from Peer " + clientID);

                if (PeerNode.pingCounter.containsKey(clientID)) {
                    int count = PeerNode.pingCounter.get(clientID);
                    if (count > 0) {
                        count--;
                        PeerNode.pingCounter.put(clientID, count);
                        System.out.println("CT: " + count);
                    }
                }
            }
        }
    }


}
