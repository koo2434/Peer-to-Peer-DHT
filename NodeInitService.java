
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


class NodeInitService {

    private int nodeID;
    private volatile List<Integer> successorNodeIDList;

    private final int PING_INTERVAL;
    private final int PORT_OFFSET;

    private NodeStatus nodeStatus;

    private DatagramSocket udpSocket;
    private ServerSocket tcpSocket;

    public NodeInitService (int nodeID, int firstSuccessorNodeID,
                    int secondSuccessorNodeID, int PING_INTERVAL,
                    int PORT_OFFSET) throws Exception {
        this.nodeID = nodeID;
        this.successorNodeIDList = new ArrayList<>();
        this.successorNodeIDList.add(firstSuccessorNodeID);
        this.successorNodeIDList.add(secondSuccessorNodeID);
        this.PING_INTERVAL = PING_INTERVAL;
        this.PORT_OFFSET = PORT_OFFSET;

        //Shared NodeStatus instance for node and network status management.
        this.nodeStatus = new NodeStatus();

        InetAddress addr = InetAddress.getByName("127.0.0.1");
        int port = PORT_OFFSET + nodeID;
        this.udpSocket = new DatagramSocket(port, addr);
        this.tcpSocket = new ServerSocket(port);
    }

    public void begin() throws Exception {
        ExecutorService p2pService = Executors.newCachedThreadPool();

        PingRequestService pingRequestService = new PingRequestService(
            this.udpSocket, this.nodeID, this.successorNodeIDList,
            this.PING_INTERVAL, this.PORT_OFFSET,
            this.nodeStatus
        );
        PingProcessService pingProcessService = new PingProcessService (
            this.udpSocket, this.nodeID, this.nodeStatus
        );

        TCPProcessService tcpProcessService = new TCPProcessService(
            this.tcpSocket, this.nodeID, this.successorNodeIDList
        );

        p2pService.execute(pingProcessService);
        Future<Integer> pingFailedFuture = p2pService.submit(pingRequestService);
        p2pService.execute(tcpProcessService);

        int lostTargetID = pingFailedFuture.get();
        System.out.println(lostTargetID);
        p2pService.shutdownNow();
        return;

    }

}
