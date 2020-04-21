
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * JoinProcessService processes incoming join requests.
 */
class JoinProcessService implements Runnable {

    private Socket client;
    private int nodeID;
    private volatile List<Integer> successorNodeIDList;
    private int clientNodeID;
    private NodeStatus nodeStatus;

    public JoinProcessService(Socket socket, int nodeID,
                             List<Integer> successorNodeIDList, int clientNodeID,
                             NodeStatus nodeStatus) {
        this.client = socket;
        this.nodeID = nodeID;
        this.successorNodeIDList = successorNodeIDList;
        this.clientNodeID = clientNodeID;
        this.nodeStatus = nodeStatus;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(this.client.getOutputStream());
            int firstSuccessorNodeID = this.successorNodeIDList.get(0);
            int secondSuccessorNodeID = this.successorNodeIDList.get(1);

            // Message for join request approval.
            //  e.g. REPONSE/JOIN:APPROVED:4:5
            String r1 = "RESPONSE/JOIN:APPROVED:" + firstSuccessorNodeID
                                            + ":" + secondSuccessorNodeID;
            //Message for join request delegation.
            //  e.g. REPONSE/JOIN:DELEGATE:4
            String r2 = "RESPONSE/JOIN:DELEGATE:" + firstSuccessorNodeID;

            if (this.clientNodeID < this.nodeID) {
                out.writeUTF(r2);
            } else if (this.clientNodeID > this.nodeID) {
                if (this.clientNodeID < firstSuccessorNodeID
                    || this.nodeID > firstSuccessorNodeID) {
                        out.writeUTF(r1);
                        //Alert its predecessor
                        this.nodeStatus.setJoinedSuccessor(clientNodeID);
                        this.nodeStatus.setNotifyJoinedSuccessor(true);
                        //Process new successor
                        successorNodeIDList.set(1, successorNodeIDList.get(0));
                        successorNodeIDList.set(0, this.clientNodeID);
                        this.nodeStatus.setNewOutPingCount (successorNodeIDList);
                } else if (this.nodeID < firstSuccessorNodeID) {
                    out.writeUTF(r2);
                } else {
                    System.out.println("ERROR CASE");
                }
            } else {
                System.out.println("ERROR CASE");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
