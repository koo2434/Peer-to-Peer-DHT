
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingRequestService implements Callable<Integer> {

    private int nodeID;
    private volatile List<Integer> targetIDList;
    private final int PING_INTERVAL;
    private final int PORT_OFFSET;

    private NodeStatus nodeStatus;

    private DatagramSocket socket;

    /**
     * Constructor.
     *
     * @param ID id number of this node.
     * @param targetIDList List of successors / target nodes to ping to
     * @param PING_INTERVAL how long should intervals between each ping be; milliseconds
     * @param PORT_OFFSET port number = PORT_OFFSET + ID, rule applies to all nodes in network
     */
    public PingRequestService(DatagramSocket socket, int ID, List<Integer> targetIDList,
                        int PING_INTERVAL, int PORT_OFFSET, NodeStatus nodeStatus) throws Exception {
        this.nodeID = ID;
        this.targetIDList = targetIDList;
        this.PING_INTERVAL = PING_INTERVAL;
        this.PORT_OFFSET = PORT_OFFSET;
        this.nodeStatus = nodeStatus;

        this.socket = socket;
    }

    //Thread Instance sending ping to its successors
    @Override
    public Integer call() throws Exception {
        int offlineTargetID = -1;
        boolean circuitAlive = true;
        while(this.nodeStatus.isNodeStayAlive()) {
            try{
                for (int i = 0; i < this.targetIDList.size(); i++) {
                    String pingMsg = "REQUEST/PING:" + this.nodeID + ":" + i;
                    byte[] msgBytes = pingMsg.getBytes();

                    int targetID = this.targetIDList.get(i);
                    SocketAddress targetAddr = new InetSocketAddress("127.0.0.1", PORT_OFFSET + targetID);

                    DatagramPacket pingPacket = new DatagramPacket(msgBytes, msgBytes.length, targetAddr);
                    socket.send(pingPacket);
                    this.nodeStatus.incrementOutPingCount(targetID);
                    System.out.println("Ping request sent to Peer " + targetID);

                    if (this.nodeStatus.getOutPingCount(targetID) >= 3) {
                        circuitAlive = false;
                        offlineTargetID = targetID;
                        System.out.println("Offline found: " + offlineTargetID);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(this.nodeStatus.isNodeStayAlive()) Thread.sleep(PING_INTERVAL);
            if(!circuitAlive) {
                //this.nodeStatus.setSuccessorsChanging(true);
                if (offlineTargetID == this.targetIDList.get(0)) {
                    this.resetPrimarySuccessor(targetIDList.get(1), true);
                } else {
                    this.resetPrimarySuccessor(targetIDList.get(0), false);
                }
            }
            circuitAlive = true;
        }
        return 0;
    }
    private synchronized void resetPrimarySuccessor (int clientID, boolean isFirstDead) {
        try {
            Socket requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), this.PORT_OFFSET + clientID);
            DataOutputStream out = new DataOutputStream(requestSocket.getOutputStream());
            DataInputStream in = new DataInputStream(requestSocket.getInputStream());

            String requestSuccessorMsg = "REQUEST/SUCCESSORS:" + this.nodeID;
            out.writeUTF(requestSuccessorMsg);
            System.out.println("Request sent to " + clientID);

            while (!this.nodeStatus.isSecondarySuccessorReceived()) {
                try {
                    Thread.sleep(500);
                    System.out.println("Sleeping...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.nodeStatus.setSecondarySuccessorReceived(false);
            if (isFirstDead) {
                this.targetIDList.set(0, this.targetIDList.get(1));
                this.targetIDList.set(1, this.nodeStatus.getSecondarySuccessor());
            } else {
                this.targetIDList.set(1, this.nodeStatus.getSecondarySuccessor());
            }
        } catch (IOException e) {
            System.out.println("Failed to notify predecessor Error");
            e.printStackTrace();
        }
    }

}
