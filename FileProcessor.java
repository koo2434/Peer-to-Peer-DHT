
import java.io.*;
import java.net.*;
import java.nio.file.*;
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

    public boolean hasFileInDirectory(int fileName) {
        try {
            File file = new File("./Files");
            String[] foundFiles = file.list();
            for (String f : foundFiles) {
                System.out.println(f);

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
    public boolean hasFile(int fileName) {
        return this.nodeStatus.hasFile(fileName);
    }
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
                    requestMsg = "REQUEST/DATA_INSERTION:"+fileName+":FALSE";
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
                    } else {
                        requestMsg = "REQUEST/DATA_INSERTION:"+fileName+":FALSE";
                    }
                }
                System.out.println("==== " + requestMsg);
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
    public synchronized void requestFile(int fileName, int originID) {
        try{
            int hash = fileName % 256;
            int successorID = this.successorNodeIDList.get(0);
            String requestMsg = "REQUEST/DATA_REQUEST:" + fileName + ":" + originID;
            Socket socket = new Socket(InetAddress.getByName("127.0.0.1"),
                                this.PORT_OFFSET + successorID);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(requestMsg);
            System.out.println("File request for " + fileName + " has been sent to my successor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
            String fileTransferMsg = "NOTIFY/DATA_INCOMING:" + fileFullName;
            long length = fileToSend.length();
            byte[] bytes = new byte[4096];
            InputStream in = new FileInputStream(fileToSend);

            out.writeUTF(fileTransferMsg);
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
