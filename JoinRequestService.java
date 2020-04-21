
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * JoinRequestService process join request made by the node
 * before it initiates.
 */

class JoinRequestService {

    private int nodeID;
    private int knownPeerID;

    private final int PING_INTERVAL;
    private final int PORT_OFFSET;

    private Socket socket;

    public JoinRequestService(int nodeID, int knownPeerID,
                    int PING_INTERVAL, int PORT_OFFSET) throws Exception {
        this.nodeID = nodeID;
        this.knownPeerID = knownPeerID;
        this.PING_INTERVAL = PING_INTERVAL;
        this.PORT_OFFSET = PORT_OFFSET;

        InetAddress receiverAddress = InetAddress.getByName("127.0.0.1");
        this.socket = new Socket(receiverAddress, PORT_OFFSET + knownPeerID);
    }

    /**
     * Joins the network based on the nodeID and knownPeerID.
     * Sends a message to the knownPeerID via TCP, which will either
     * approve its join w/ successor info or delegate its request until it finds
     * the appropriate node.
     * @return two successor ID numbers.
     */
    public List<Integer> joinNetwork() {
        try{

            String response;
            boolean foundPredecessor = false;
            List<Integer> targetNodeIDs = new ArrayList<>();

            while (!foundPredecessor) {
                DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
                DataInputStream in = new DataInputStream(this.socket.getInputStream());

                String joinRequestMsg = "REQUEST/JOIN:" + this.nodeID;
                out.writeUTF(joinRequestMsg);

                response = in.readUTF();

                String responseType = response.split(":", 2)[0].trim();
                if (responseType.equals("RESPONSE/JOIN")) {
                    String approval = response.split(":")[1].trim();
                    if (approval.equals("APPROVED")) {
                        System.out.println("Join APPROVED");
                        int firstSuccessorNodeID = Integer.parseInt(response.split(":")[2]);
                        int secondSuccessorNodeID = Integer.parseInt(response.split(":")[3]);

                        System.out.println("Primary Successor: " + firstSuccessorNodeID);
                        System.out.println("Secondary Successor: " + secondSuccessorNodeID);

                        out.close();
                        in.close();
                        this.socket.close();

                        targetNodeIDs.add(firstSuccessorNodeID);
                        targetNodeIDs.add(secondSuccessorNodeID);
                        foundPredecessor = true;
                    } else if (approval.equals("DELEGATE")) {
                        System.out.println("DELEGATED");

                        int targetNodeID = Integer.parseInt(response.split(":")[2]);
                        InetAddress receiverAddress = InetAddress.getByName("127.0.0.1");
                        this.socket = new Socket(receiverAddress, PORT_OFFSET + targetNodeID);
                    }
                }
            }
            return targetNodeIDs;
        } catch (IOException e) {
            System.out.println("Error while searching for predecessor");
            e.printStackTrace();
        }
        return null;
    }
}
