
import java.io.*;
import java.net.*;
import java.util.*;

class TCPProcessService implements Runnable {

    private ServerSocket socket;
    private int nodeID;
    private volatile List<Integer> successorNodeIDList;
    private NodeStatus nodeStatus;

    public TCPProcessService (ServerSocket socket,
                              int nodeID,
                              List<Integer> successorNodeIDList,
                              NodeStatus nodeStatus) {
        this.socket = socket;
        this.nodeID = nodeID;
        this.successorNodeIDList = successorNodeIDList;
        this.nodeStatus = nodeStatus;
    }

    @Override
    public void run() {
        while (this.nodeStatus.isNodeStayAlive()) {
            try {
                Socket clientSocket = this.socket.accept();
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                String request = in.readUTF();
                System.out.println("TCP : " + request);
                String requestType = request.split(":")[0].trim();

                if (requestType.equals("REQUEST/JOIN")) {
                    System.out.println("Processing JOIN");
                    JoinProcessService joinProcessService = new JoinProcessService(
                                                    clientSocket, this.nodeID,
                                                    this.successorNodeIDList,
                                                    Integer.parseInt(request.split(":")[1]),
                                                    this.nodeStatus);
                    new Thread(joinProcessService).start();
                } else if (requestType.equals("NOTIFY/CHANGE_OF_SUCCESSOR")) {
                    System.out.println("Changing Successors");

                    int newSuccessor = Integer.parseInt(request.split(":")[1].trim());
                    successorNodeIDList.set(1, newSuccessor);
                    this.nodeStatus.setNewOutPingCount(successorNodeIDList);
                } else if (requestType.equals("NOTIFY/NODE_DEAD")) {
                    //TODO: Process dead node notification from successor
                    String[] requestArr = request.split(":");
                    int deadNodeID = Integer.parseInt(requestArr[1]);
                    int newFirstSuccessorID = Integer.parseInt(requestArr[2]);
                    int newSecondSuccessorID = Integer.parseInt(requestArr[3]);
                    System.out.println("Node dead: " + deadNodeID);

                    //1. If the dead node is the immediate successor
                    //2. If the dead node is the secondary successor
                    if (deadNodeID == this.successorNodeIDList.get(0)) {
                        this.successorNodeIDList.set(0, newFirstSuccessorID);
                        this.successorNodeIDList.set(1, newSecondSuccessorID);
                    } else if (deadNodeID == this.successorNodeIDList.get(1)){
                        this.successorNodeIDList.set(1, newFirstSuccessorID);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
