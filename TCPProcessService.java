
import java.io.*;
import java.net.*;
import java.nio.file.*;
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
                String requestType = request.split(":")[0].trim();
                System.out.println(request);

                if (requestType.contains("REQUEST/JOIN")) {
                    System.out.println("Processing JOIN");
                    JoinProcessService joinProcessService = new JoinProcessService(
                                                    clientSocket, this.nodeID,
                                                    this.successorNodeIDList,
                                                    Integer.parseInt(request.split(":")[1]),
                                                    this.nodeStatus);
                    new Thread(joinProcessService).start();
                } else if (requestType.contains("NOTIFY/CHANGE_OF_SECONDARY_SUCCESSOR")) {
                    System.out.println("Changing Secondary Successor");

                    int newSuccessor = Integer.parseInt(request.split(":")[1].trim());
                    successorNodeIDList.set(1, newSuccessor);
                    this.nodeStatus.setNewOutPingCount(successorNodeIDList);
                } else if (requestType.contains("NOTIFY/NODE_DEAD")) {
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
                } else if (requestType.contains("REQUEST/SUCCESSORS")) {
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
                } else if (requestType.contains("RESPONSE/SUCCESSORS")) {
                    System.out.println("Successor received");
                    int firstID = Integer.parseInt(request.split(":")[1].trim());
                    int secondID = Integer.parseInt(request.split(":")[2].trim());
                    if (firstID == successorNodeIDList.get(1)) {
                        this.nodeStatus.setSecondarySuccessor(secondID);
                    } else {
                        this.nodeStatus.setSecondarySuccessor(firstID);
                    }
                    this.nodeStatus.setSecondarySuccessorReceived(true);
                } else if (requestType.contains("REQUEST/DATA_INSERTION")) {
                    int requestedFile = Integer.parseInt(request.split(":")[1].trim());
                    boolean forcedAdd = request.split(":")[2].trim().equals("TRUE");
                    this.fileProcessor.insertFile(requestedFile, forcedAdd);
                } else if (requestType.contains("REQUEST/DATA_REQUEST")) {
                    int requestedFile = Integer.parseInt(request.split(":")[1].trim());
                    int clientID = Integer.parseInt(request.split(":")[2].trim());
                    if (clientID == this.nodeID) {
                        System.out.println("The requested file does not exist in the network.");
                    } else if (this.fileProcessor.hasFile(requestedFile)) {
                        System.out.println("File " + requestedFile + " stored here");
                        System.out.println("Sending file " + requestedFile + " to Peer " + clientID);
                        boolean sent = this.fileProcessor.sendFile(requestedFile, clientID);
                        if (sent) {
                            System.out.println("The file has been sent");
                        } else {
                            System.out.println("The file was not sent due to an error.");
                        }
                    } else {
                        System.out.println("Request for File " + requestedFile + " has been received, but the file is not stored here");
                        this.fileProcessor.requestFile(requestedFile, clientID);
                    }
                } else if (requestType.equals("NOTIFY/DATA_INCOMING")) {
                    String fileName = request.split(":")[1].trim();
                    String fileDir = "./Files/received_" + fileName;
                    File temp = new File(fileDir);
                    if (temp.exists()) {
                        temp.delete();
                    }
                    byte[] bytes = new byte[4096];
                    OutputStream fileOut = new FileOutputStream(fileDir);
                    int count;
                    while((count = in.read(bytes)) > 0) {
                        fileOut.write(bytes, 0, count);
                    }
                    fileOut.close();
                    System.out.println("File received: " + fileName);
                }
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
