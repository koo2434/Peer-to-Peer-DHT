
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
        while(this.nodeStatus.isCircuitAlive()) {
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
                        this.nodeStatus.setCircuitAlive(false);
                        offlineTargetID = targetID;
                        System.out.println("Offline found");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(this.nodeStatus.isCircuitAlive()) Thread.sleep(PING_INTERVAL);
        }

        System.out.println("Quit here");
        return offlineTargetID;
    }

}
