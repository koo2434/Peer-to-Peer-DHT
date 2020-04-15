
import java.io.*;
import java.net.*;
import java.util.*;

class TCPProcessService implements Runnable {

    private ServerSocket socket;
    private int nodeID;
    private List<Integer> successorNodeIDList;

    public TCPProcessService (ServerSocket socket,
                              int nodeID,
                              List<Integer> successorNodeIDList) {
        this.socket = socket;
        this.nodeID = nodeID;
        this.successorNodeIDList = successorNodeIDList;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                String request = in.readUTF();
                String requestType = request.split(":")[0].trim();

                if (requestType.equals("REQUEST/JOIN")) {
                    JoinProcessService joinProcessService = new JoinProcessService(
                                                    clientSocket, this.nodeID,
                                                    this.successorNodeIDList, request.split(":")[0]);
                    joinProcessService.start();
                } else {
                    //Start service that uses TCP protocol
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
