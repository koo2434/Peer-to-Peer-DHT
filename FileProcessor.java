
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileProcessor provides tools to perform any file IO processes.
 */
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

    /**
     * Checks if the @param exists in the './Files' directory.
     * @param fileName four-digit presumed file name in the directory.
     * @return if the file exists in the './Files' directory or not.
     */
    public boolean hasFileInDirectory(int fileName) {
        try {
            File file = new File("./Files");
            String[] foundFiles = file.list();
            for (String f : foundFiles) {

                if (f.split("\\.")[0].equals(fileName+"")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Checks if this node holds the @param or not.
     * The difference from hasFileInDirectory method is
     * hasFileInDirectory checks if the physical file exists in the './Files',
     * while hasFile checks if the @param is held by this node or not.
     * @param fileName four-digit presumed file name in the directory.
     * @return if the node holds @param or not.
     */
    public boolean hasFile(int fileName) {
        return this.nodeStatus.hasFile(fileName);
    }
    /**
     * Stores the given file if this node is the appropriate node
     * based on the nodeID and the calculated hash value,
     * OR @param forceAdd is true
     * OR hash value > nodeID && nodeID > successorID.
     * If not, it forwards the Store request to is successor.
     * If one of the following conditions are met, the Store request is forwarded
     * with forceAdd being true.
     *  1. Hash value < nodeID && successorID < nodeID
     *  2. Hash value > nodeID AND hash value < successorID.
     * @param fileName four-digit given file name.
     * @param forceAdd given file should be stored in this node if this is true.
     */
    public synchronized void insertFile(int fileName, boolean forcedAdd) {
        try{
            int hash = fileName % 256;
            int successorID = this.successorNodeIDList.get(0);
            String requestMsg = "";

            if (forcedAdd || hash == nodeID) {
                boolean isDup = !this.nodeStatus.putNewFile(fileName);
                if (isDup) {
                    System.out.println("This node already holds file " + fileName);
                } else {
                    System.out.println("Store " + fileName + " request accepted");
                }
            } else {
                if (hash < this.nodeID) {
                    if (successorID < this.nodeID) {
                        requestMsg = "REQUEST/DATA_INSERTION:"+fileName+":TRUE";
                    } else {
                        requestMsg = "REQUEST/DATA_INSERTION:"+fileName+":FALSE";
                    }
                } else if (hash > this.nodeID){
                    if (hash < successorID) {
                        requestMsg = "REQUEST/DATA_INSERTION:"+fileName+":TRUE";
                    } else if (successorID < this.nodeID) {
                        boolean isDup = !this.nodeStatus.putNewFile(fileName);
                        if (isDup) {
                            System.out.println("This node already holds file " + fileName);
                        } else {
                            System.out.println("Store " + fileName + " request accepted");
                        }
                        return;
                    } else {
                        requestMsg = "REQUEST/DATA_INSERTION:"+fileName+":FALSE";
                    }
                }
                Socket socket = new Socket(InetAddress.getByName("127.0.0.1"),
                                    this.PORT_OFFSET + successorID);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(requestMsg);
                System.out.println("Store " + fileName + " request forwarded to successor");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Requests a file from successor.
     * @param fileName four-digit name of the requested file.
     * @param originID the ID number of the node that made this request. Used by the node that
     *                  holds the requested file to transfer the file.
     */
    public synchronized void requestFile(int fileName, int originID) {
        try{
            int hash = fileName % 256;
            int successorID = this.successorNodeIDList.get(0);
            String requestMsg = "REQUEST/DATA_REQUEST:" + fileName + ":" + originID;
            Socket socket = new Socket(InetAddress.getByName("127.0.0.1"),
                                this.PORT_OFFSET + successorID);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(requestMsg);
            System.out.println("File request for " + fileName + " has been forwarded to my successor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Sends a file to a node.
     * @param fileName four-digit name of the file being sent.
     * @param clientID ID number of the node that the file is being sent to.
     */
    public boolean sendFile(int fileName, int clientID) {
        try{
            if (!hasFile(fileName)) return false;
            Socket socket = new Socket(InetAddress.getByName("127.0.0.1"),
                                this.PORT_OFFSET + clientID);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String fileDir = "./Files/";
            String fileFullName = "";
            String[] foundFiles = new File(fileDir).list();
            for (String f : foundFiles) {
                if (f.split("\\.")[0].equals(fileName + "")) {
                    fileDir += f;
                    fileFullName = f;
                    break;
                }
            }

            File fileToSend = new File(fileDir);
            //1. Notify the recipient that the file will be sent
            String fileTransferMsg = "NOTIFY/DATA_INCOMING:"+ this.nodeID + ":" + fileFullName;
            long length = fileToSend.length();
            byte[] bytes = new byte[4096];
            InputStream in = new FileInputStream(fileToSend);
            out.writeUTF(fileTransferMsg);

            //2. Send the file
            int count;
            while((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.close();
            in.close();
            socket.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
