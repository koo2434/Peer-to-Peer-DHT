
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
        while (true) {
            try {
                Socket clientSocket = this.socket.accept();
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                String request = in.readUTF();
                String requestType = request.split(":")[0].trim();

                if (requestType.equals("REQUEST/JOIN")) {
                    System.out.println("Processing JOIN");
                    JoinProcessService joinProcessService = new JoinProcessService(
                                                    clientSocket, this.nodeID,
                                                    this.successorNodeIDList,
                                                    Integer.parseInt(request.split(":")[1]),
                                                    this.nodeStatus);
                    new Thread(joinProcessService).start();
                } else if (requestType.equals("NOTIFY/CHANGE_OF_SUCCESSOR")){
                    //TODO: Implement change of successor
                    System.out.println("Changing Successors");

                    int newSuccessor = Integer.parseInt(request.split(":")[1].trim());
                    successorNodeIDList.set(1, successorNodeIDList.get(0));
                    successorNodeIDList.set(0, newSuccessor);
                    this.nodeStatus.setNewOutPingCount(successorNodeIDList);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
