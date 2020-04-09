
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

    public List<Integer> joinNetwork() {
        try{
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());

            String response;
            boolean foundPredecessor = false;
            List<Integer> targetNodeIDs = new ArrayList();

            while (!foundPredecessor) {
                String joinRequestMsg = "REQUEST/JOIN:" + this.nodeID;
                out.writeUTF(joinRequestMsg);

                response = in.readUTF();
                System.out.println(response);

                String responseType = response.split(":", 2)[0].trim();
                if (responseType.equals("RESPONSE/JOIN")) {
                    String approval = response.split(":")[1].trim();
                    if (approval.equals("APPROVED")) {
                        int firstSuccessorNodeID = Integer.parseInt(response.split(":")[2]);
                        int secondSuccessorNodeID = Integer.parseInt(response.split(":")[3]);

                        out.close();
                        in.close();
                        this.socket.close();

                        targetNodeIDs.add(firstSuccessorNodeID);
                        targetNodeIDs.add(secondSuccessorNodeID);
                        foundPredecessor = true;
                    } else if (approval.equals("DELEGATE")) {
                        int targetNodeID = Integer.parseInt(response.split(":")[2]);
                        InetAddress receiverAddress = InetAddress.getByName("127.0.0.1");
                        this.socket.close();
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
