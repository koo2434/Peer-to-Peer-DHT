
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingRequestService implements Callable<Integer> {

    private int nodeID;
    private List<Integer> targetIDList;
    private final int PING_INTERVAL;

    private DatagramSocket socket;
    private List<SocketAddress> targetAddrList;

    /**
     * Constructor.
     *
     * @param ID id number of this node.
     * @param targetIDList List of successors / target nodes to ping to
     * @param PING_INTERVAL how long should intervals between each ping be; milliseconds
     * @param PORT_OFFSET port number = PORT_OFFSET + ID, rule applies to all nodes in network
     */
    public PingRequestService(DatagramSocket socket, int ID, List<Integer> targetIDList, int PING_INTERVAL, int PORT_OFFSET) throws Exception {
        this.nodeID = ID;
        this.targetIDList = targetIDList;
        this.PING_INTERVAL = PING_INTERVAL;

        this.socket = socket;
        this.targetAddrList = new ArrayList<>();
        for (Integer id : targetIDList) {
            PeerNode.pingCounter.put(id, 0);
            this.targetAddrList.add(new InetSocketAddress("127.0.0.1", PORT_OFFSET + id));
        }
    }

    //Thread Instance sending ping to its successors
    @Override
    public Integer call() throws Exception {
        int offlineTargetID = -1;
        while(PeerNode.isDestAlive) {
            String pingMsg = "REQUEST:" + this.nodeID;
            byte[] msgBytes = pingMsg.getBytes();

            try{
                for (int i = 0; i < this.targetAddrList.size(); i++) {
                    int targetID = this.targetIDList.get(i);
                    SocketAddress targetAddr = this.targetAddrList.get(i);

                    DatagramPacket pingPacket = new DatagramPacket(msgBytes, msgBytes.length, targetAddr);
                    socket.send(pingPacket);

                    int newCount =  PeerNode.pingCounter.get(targetID) + 1;
                    PeerNode.pingCounter.put(targetID, newCount);
                    System.out.println("Ping request sent to Peer " + targetID);

                    if (newCount >= 3) {
                        PeerNode.isDestAlive = false;
                        offlineTargetID = targetID;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(PeerNode.isDestAlive) Thread.sleep(PING_INTERVAL);
        }

        return offlineTargetID;
    }

}
