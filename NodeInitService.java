
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


class NodeInitService {

    private int nodeID;
    private int firstSuccessorNodeID;
    private int secondSuccessorNodeID;

    private final int PING_INTERVAL;
    private final int PORT_OFFSET;

    private NodeStatus nodeStatus;

    private DatagramSocket udpSocket;
    private Socket tcpSocket;

    public NodeInitService (int nodeID, int firstSuccessorNodeID,
                    int secondSuccessorNodeID, int PING_INTERVAL,
                    int PORT_OFFSET) throws Exception {
        this.nodeID = nodeID;
        this.firstSuccessorNodeID = firstSuccessorNodeID;
        this.secondSuccessorNodeID = secondSuccessorNodeID;
        this.PING_INTERVAL = PING_INTERVAL;
        this.PORT_OFFSET = PORT_OFFSET;

        //Shared NodeStatus instance for node and network status management.
        this.nodeStatus = new NodeStatus();

        InetAddress addr = InetAddress.getByName("127.0.0.1");
        int port = PORT_OFFSET + nodeID;
        this.udpSocket = new DatagramSocket(port, addr);
        this.tcpSocket = new Socket(addr, port);
    }

    public void begin() throws Exception {
        ExecutorService p2pService = Executors.newCachedThreadPool();
        List<Integer> targetIDList = new ArrayList();
        targetIDList.add(firstSuccessorNodeID);
        targetIDList.add(secondSuccessorNodeID);

        PingRequestService pingRequestService = new PingRequestService(
            this.udpSocket, this.nodeID, targetIDList,
            this.PING_INTERVAL, this.PORT_OFFSET,
            this.nodeStatus
        );
        PingProcessService pingProcessService = new PingProcessService (
            this.udpSocket, this.nodeID, this.nodeStatus
        );
        JoinProcessService joinProcessService = new JoinProcessService(
            this.tcpSocket
        );

        p2pService.execute(pingProcessService);
        Future<Integer> pingFailedFuture = p2pService.submit(pingRequestService);

        int lostTargetID = pingFailedFuture.get();
        System.out.println(lostTargetID);
        p2pService.shutdownNow();
        return;

    }

}
