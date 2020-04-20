
import java.io.*;
import java.net.*;
import java.util.*;

class TCPProcessService implements Runnable {

    private ServerSocket socket;
    private int nodeID;
    private volatile List<Integer> successorNodeIDList;
    private int PORT_OFFSET;
    private NodeStatus nodeStatus;
    private FileProcessor fileProcessor;

    public TCPProcessService (ServerSocket socket,
                              int nodeID,
                              List<Integer> successorNodeIDList,
                              int PORT_OFFSET,
                              NodeStatus nodeStatus,
                              FileProcessor fileProcessor) {
        this.socket = socket;
        this.nodeID = nodeID;
        this.successorNodeIDList = successorNodeIDList;
        this.PORT_OFFSET = PORT_OFFSET;
        this.nodeStatus = nodeStatus;
        this.fileProcessor = fileProcessor;
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
                } else if (requestType.equals("NOTIFY/CHANGE_OF_SECONDARY_SUCCESSOR")) {
                    System.out.println("Changing Secondary Successor");

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
                        System.out.println("New Primary Successor: " + newFirstSuccessorID);
                        System.out.println("New Secondary Successor: " + newSecondSuccessorID);
                    } else if (deadNodeID == this.successorNodeIDList.get(1)){
                        System.out.println("New Secondary Successor: " + newFirstSuccessorID);
                        this.successorNodeIDList.set(1, newFirstSuccessorID);
                    }
                } else if (requestType.equals("REQUEST/SUCCESSORS")) {
                    System.out.println("Successor requested");
                    try {
                        int clientID = Integer.parseInt(request.split(":")[1].trim());

                        Socket responseSocket = new Socket(InetAddress.getByName("127.0.0.1"), this.PORT_OFFSET + clientID);
                        DataOutputStream out = new DataOutputStream(responseSocket.getOutputStream());

                        String response = "RESPONSE/SUCCESSORS:"
                                        + this.successorNodeIDList.get(0) + ":"
                                        + this.successorNodeIDList.get(1);
                        out.writeUTF(response);
                        System.out.println("Sent: " + response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (requestType.equals("RESPONSE/SUCCESSORS")) {
                    System.out.println("Successor received");
                    int firstID = Integer.parseInt(request.split(":")[1].trim());
                    int secondID = Integer.parseInt(request.split(":")[2].trim());
                    if (firstID == successorNodeIDList.get(1)) {
                        this.nodeStatus.setSecondarySuccessor(secondID);
                    } else {
                        this.nodeStatus.setSecondarySuccessor(firstID);
                    }
                    this.nodeStatus.setSecondarySuccessorReceived(true);
                } else if (requestType.equals("DATA/INSERTION")) {
                    int requestedFile = Integer.parseInt(request.split(":")[1].trim());
                    this.fileProcessor.insertFile(requestedFile);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
