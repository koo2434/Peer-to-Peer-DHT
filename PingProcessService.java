
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingProcessService implements Runnable {

    private DatagramSocket socket;
    private int nodeID;
    private NodeStatus nodeStatus;

    public PingProcessService (DatagramSocket socket, int ID, NodeStatus nodeStatus) {
        this.socket = socket;
        this.nodeID = ID;
        this.nodeStatus = nodeStatus;
    }

    @Override
    public void run() {
        System.out.println("This works!");
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
            System.out.println(data);
            String type = data.split(":", 2)[0].trim();
            int clientID = Integer.parseInt(data.split(":")[1].trim());

            if (type.equals("REQUEST/PING")) {
                System.out.println("Ping request received from Peer " + clientID);

                String pingMsg= "RESPONSE/PING:" + this.nodeID;
                byte[] msgBytes = pingMsg.getBytes();
                DatagramPacket pingPacket = new DatagramPacket(msgBytes, msgBytes.length, clientAddr);
                try {
                    socket.send(pingPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (type.equals("RESPONSE/PING")) {
                System.out.println("Ping response received from Peer " + clientID);
                int pingCount = this.nodeStatus.getPingCount(clientID);
                if (pingCount > 0) {
                    this.nodeStatus.decrementPingCount(clientID);
                }
            }
        }
    }


}
