
import java.io.*;
import java.net.*;
import java.util.*;

class JoinProcessService implements Runnable {

    private Socket client;
    private int nodeID;
    private List<Integer> successorNodeIDList;
    private int clientNodeID;

    public JoinProcessService(Socket socket, int nodeID,
                             int successorNodeIDList, int clientNodeID) {
        this.client = socket;
        this.nodeID = nodeID;
        this.successorNodeIDList = successorNodeIDList;
        this.clientNodeID = clientNodeID;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(this.client.getOutputStream());
            int firstSuccessorNodeID = this.successorNodeIDList.get(0);
            int secondSuccessorNodeID = this.successorNodeIDList.get(1);

            //  REPONSE/JOIN:APPROVED:4:5
            String r1 = "RESPONSE/JOIN:APPROVED:" + this.successorNodeIDList.get(0)
                                            + ":" + this.successorNodeIDList.get(1);
            //  REPONSE/JOIN:DELEGATE:4
            String r2 = "RESPONSE/JOIN:DELEGATE:" + this.successorNodeIDList.get(0);

            if (this.clientNodeID > this.successorNodeID ||
                    this.clientNodeID < this.nodeID) {
                out.writeUTF(r2);
            } else {
                out.writeUTF(r1);
                //Process new successor

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
