
import java.io.*;
import java.net.*;
import java.util.*;

class FileProcessor {
    private int nodeID;
    private NodeStatus nodeStatus;
    private List<Integer> successorNodeIDList;

    private int PORT_OFFSET;

    public FileProcessor(int nodeID,
                              List<Integer> successorNodeIDList,
                              int PORT_OFFSET,
                              NodeStatus nodeStatus) {
        this.nodeID = nodeID;
        this.successorNodeIDList = successorNodeIDList;
        this.PORT_OFFSET = PORT_OFFSET;
        this.nodeStatus = nodeStatus;
    }

    public synchronized void insertFile(int fileName) {
        try{
            int hash = fileName % 256;

            if (hash == nodeID) {
                boolean isDup = !this.nodeStatus.putNewFile(fileName);
                if (isDup) {
                    System.out.println("This node already holds file " + fileName);
                } else {
                    System.out.println("Store " + fileName + " request accepted");
                }
            } else {
                String requestMsg = "DATA/INSERTION:"+fileName;
                Socket socket = new Socket(InetAddress.getByName("127.0.0.1"),
                                    this.PORT_OFFSET + successorNodeIDList.get(0));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(requestMsg);
                System.out.println("Store " + fileName + " request forwarded to successor");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void requestFile(int fileName) {
        
    }
}
