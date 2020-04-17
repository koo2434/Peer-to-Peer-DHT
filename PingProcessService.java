
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingProcessService implements Runnable {

    private DatagramSocket socket;
    private int nodeID;
    private int PORT_OFFSET;
    private NodeStatus nodeStatus;

    public PingProcessService (DatagramSocket socket, int ID, int PORT_OFFSET, NodeStatus nodeStatus) {
        this.socket = socket;
        this.nodeID = ID;
        this.PORT_OFFSET = PORT_OFFSET;
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
            String[] data = new String(receivePacket.getData()).trim().split(":");

            String type = data[0].trim();
            int clientID = Integer.parseInt(data[1].trim());

            if (type.equals("REQUEST/PING")) {
                System.out.println("Ping request received from Peer " + clientID);

                int successorPosition = Integer.parseInt(data[2].trim());
                System.out.println(successorPosition);

                //If JOIN occurred and the node from which we recieved a ping
                //  is affected by the JOIN operation
                //  = change of successor by immediate predecessor
                if (this.nodeStatus.isNotifyPredecessor()
                        && successorPosition == 0) {
                    System.out.println("Yep, notify here!");
                    try {
                        int newSuccessor = this.nodeStatus.getPredecessorNewSuccessor();
                        Socket notifyingSocket = new Socket(InetAddress.getByName("127.0.0.1"), this.PORT_OFFSET + clientID);
                        DataOutputStream out = new DataOutputStream(notifyingSocket.getOutputStream());

                        String changeOfSuccessorMsg = "NOTIFY/CHANGE_OF_SUCCESSOR:" + newSuccessor;
                        out.writeUTF(changeOfSuccessorMsg);
                    } catch (IOException e) {
                        System.out.println("Failed to notify predecessor Error");
                        e.printStackTrace();
                    }
                }

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
                int pingCount = this.nodeStatus.getOutPingCount(clientID);
                if (pingCount > 0) {
                    this.nodeStatus.decrementOutPingCount(clientID);
                }
            }
        }
    }


}
