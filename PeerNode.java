
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


class PeerNode {

    private int nodeID;
    private int firstSuccessorNodeID;
    private int secondSuccessorNodeID;

    private final int PING_INTERVAL;
    private final int PORT_OFFSET;

    private DatagramSocket socket;

    volatile static boolean isDestAlive = true;
    volatile static Map<Integer, Integer> pingCounter = new HashMap<>();

    public PeerNode (int nodeID, int firstSuccessorNodeID,
                    int secondSuccessorNodeID, int PING_INTERVAL,
                    int PORT_OFFSET) throws Exception {
        this.nodeID = nodeID;
        this.firstSuccessorNodeID = firstSuccessorNodeID;
        this.secondSuccessorNodeID = secondSuccessorNodeID;
        this.PING_INTERVAL = PING_INTERVAL;
        this.PORT_OFFSET = PORT_OFFSET;

        this.socket = new DatagramSocket(nodeID);
    }

    public void begin() throws Exception {
        ExecutorService p2pService = Executors.newCachedThreadPool();
        List<Integer> targetIDList = new ArrayList();
        targetIDList.add(firstSuccessorNodeID);
        targetIDList.add(secondSuccessorNodeID);

        PingRequestService PingRequestService = new PingRequestService(
            this.socket, this.nodeID, targetIDList, this.PING_INTERVAL, this.PORT_OFFSET
        );
        PingProcessService PingProcessService = new PingProcessService (
            this.socket, this.nodeID
        );

        p2pService.execute(PingProcessService);
        Future<Integer> pingFailedFuture = p2pService.submit(PingRequestService);

        int lostTargetID = pingFailedFuture.get();
    }

}
