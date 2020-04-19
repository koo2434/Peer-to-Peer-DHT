
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingProcessService implements Runnable {

    private DatagramSocket socket;
    private int nodeID;
    private volatile List<Integer> targetIDList;
    private int PORT_OFFSET;
    private NodeStatus nodeStatus;

    public PingProcessService (DatagramSocket socket, int ID,
                                List<Integer> targetIDList,
                                int PORT_OFFSET, NodeStatus nodeStatus) {
        this.socket = socket;
        this.nodeID = ID;
        this.targetIDList = targetIDList;
        this.PORT_OFFSET = PORT_OFFSET;
        this.nodeStatus = nodeStatus;
    }

    @Override
    public void run() {
        int deadNotifiedCount = 0; //counter for predecessor notified that this node is dead
        while (deadNotifiedCount < 2) {
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

                //If JOIN occurred and the node from which we recieved a ping
                //  is affected by the JOIN operation
                //  = change of successor by immediate predecessor
                if (this.nodeStatus.isNotifyJoinedSuccessor()
                        && successorPosition == 0) {
                    this.nodeStatus.setNotifyJoinedSuccessor(false);
                    this.notifyJoinedSuccessor(clientID);
                }
                //If the node is trying to quit:
                if (!this.nodeStatus.isNodeStayAlive()) {
                    this.notifyNodeDead(clientID, successorPosition);
                    deadNotifiedCount++;
                } else {
                    String pingMsg= "RESPONSE/PING:" + this.nodeID;
                    byte[] msgBytes = pingMsg.getBytes();
                    DatagramPacket pingPacket = new DatagramPacket(msgBytes, msgBytes.length, clientAddr);
                    try {
                        socket.send(pingPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else if (type.equals("RESPONSE/PING")) {
                System.out.println("Ping response received from Peer " + clientID);
                int pingCount = this.nodeStatus.getOutPingCount(clientID);
                if (pingCount > 0) {
                    this.nodeStatus.setOutPingCount(clientID, 0);
                }
            }
        }
        this.nodeStatus.setQuitComplete(true);
    }

    private void notifyJoinedSuccessor(int clientID) {
        try {
            int joinedSuccessor = this.nodeStatus.getJoinedSuccessor();
            Socket notifyingSocket = new Socket(InetAddress.getByName("127.0.0.1"), this.PORT_OFFSET + clientID);
            DataOutputStream out = new DataOutputStream(notifyingSocket.getOutputStream());

            String changeOfSuccessorMsg = "NOTIFY/CHANGE_OF_SECONDARY_SUCCESSOR:" + joinedSuccessor;
            out.writeUTF(changeOfSuccessorMsg);
        } catch (IOException e) {
            System.out.println("Failed to notify predecessor Error");
            e.printStackTrace();
        }
    }
    private void notifyNodeDead(int clientID, int successorPosition) {
        try {
            int first = this.targetIDList.get(0);
            int second = this.targetIDList.get(1);

            String nodeDeadMsg = "NOTIFY/NODE_DEAD:" + this.nodeID + ":" + first + ":" + second;
            Socket notifyingSocket = new Socket(InetAddress.getByName("127.0.0.1"), this.PORT_OFFSET + clientID);
            DataOutputStream out = new DataOutputStream(notifyingSocket.getOutputStream());

            out.writeUTF(nodeDeadMsg);
        } catch (IOException e) {
            System.out.println("Failed to notify predecessor Error");
            e.printStackTrace();
        }
    }


}
