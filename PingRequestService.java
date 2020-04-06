
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class PingRequestService implements Callable<Integer> {

    private int nodeID;
    private int destID;
    private int PING_INTERVAL;

    private DatagramSocket socket;
    private SocketAddress destAddr;

    public PingRequestService(int ID, int port, int destID, SocketAddress destAddr, int pingInterval) throws Exception {
        this.nodeID = ID;
        this.destID = destID;
        this.PING_INTERVAL = pingInterval;

        this.socket = new DatagramSocket(port);
        this.destAddr = destAddr;
    }

    //Thread Instance sending ping to its successors
    @Override
    public Integer call() throws Exception {
        boolean isDestAlive = true;

        while(isDestAlive) {
            String pingMsg = "Ping: " + this.nodeID;
            byte[] msgBytes = pingMsg.getBytes();
            DatagramPacket pingPacket = new DatagramPacket(msgBytes, msgBytes.length, this.destAddr);

            try{
                socket.send(pingPacket);
                System.out.println("Ping request sent to Peer " + destID);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] responseBytes = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

            socket.setSoTimeout(PING_INTERVAL);
            try {
                socket.receive(responsePacket);
                System.out.println(new String(responsePacket.getData()));
                System.out.println("Ping response received from Peer " + destID);
            } catch (SocketTimeoutException e) {
                isDestAlive = false;
            }
        }

        return destID;
    }

}
