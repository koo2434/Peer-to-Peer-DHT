
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingRequestService implements Runnable {

    private int nodeID;
    private List<Integer> targetIDList;
    private int PING_INTERVAL;

    private DatagramSocket socket;
    private List<SocketAddress> targetAddrList;

    volatile static boolean isDestAlive = true;
    volatile static Map<Integer, Integer> pingCounter = new HashMap<>();

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
            pingCounter.put(id, 0);
            this.targetAddrList.add(new InetSocketAddress("127.0.0.1", PORT_OFFSET + id));
        }
    }

    //Thread Instance sending ping to its successors
    @Override
    public void run() {
        while(isDestAlive) {
            String pingMsg = "REQUEST:" + this.nodeID;
            byte[] msgBytes = pingMsg.getBytes();

            try{
                for (int i = 0; i < this.targetAddrList.size(); i++) {
                    int targetID = this.targetIDList.get(i);
                    SocketAddress targetAddr = this.targetAddrList.get(i);

                    DatagramPacket pingPacket = new DatagramPacket(msgBytes, msgBytes.length, targetAddr);
                    socket.send(pingPacket);
                    int newCount =  pingCounter.get(targetID) + 1;
                    if (newCount >= 3) isDestAlive = false;
                    pingCounter.put(targetID, newCount);
                    System.out.println("Ping request sent to Peer " + targetID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(isDestAlive) Thread.sleep(PING_INTERVAL);

        }

        
    }

}
