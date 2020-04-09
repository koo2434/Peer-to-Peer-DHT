
import java.io.*;
import java.net.*;
import java.util.*;

class JoinRequestService {

    private int nodeID;
    private int knownPeerID;

    private final int PING_INTERVAL;
    private final int PORT_OFFSET;

    //TCP Socket.
    private Socket socket;

    public JoinRequestService(int nodeID, int knownPeerID,
                    int PING_INTERVAL, int PORT_OFFSET) throws Exception {
        this.nodeID = nodeID;
        this.knownPeerID = knownPeerID;
        this.PING_INTERVAL = PING_INTERVAL;
        this.PORT_OFFSET = PORT_OFFSET;

        InetAddress receiverAddress = InetAddress.getByName("127.0.0.1");
        this.socket = new Socket(receiverAddress, PORT_OFFSET + nodeID);
    }

    public boolean joinNetwork() {

    }
}
