
import java.io.*;
import java.net.*;
import java.util.*;

public class UserRequestProcessService implements Runnable {

    private Scanner sc;
    private NodeStatus nodeStatus;

    public UserRequestProcessService(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
        this.sc = new Scanner(System.in);
    }

    @Override
    public void run() {
        while(true) {
            if (sc.hasNext()) {
                String input = sc.nextLine();
                String[] parsedInput = input.split(" ");

                if (parsedInput[0].equals("Quit")) {
                    this.nodeStatus.setNodeStayAlive(false);
                    System.out.println("Qutting DHT Process...");
                }
            }
        }
    }
}
