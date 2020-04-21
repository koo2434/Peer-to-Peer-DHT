
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * UserRequestProcessService is a Runnable that processes user input
 *  while the program runs.
 *
 */
public class UserRequestProcessService implements Runnable {

    private Scanner sc;
    private int nodeID;
    private NodeStatus nodeStatus;
    private FileProcessor fileProcessor;

    public UserRequestProcessService( int nodeID,
                                    NodeStatus nodeStatus,
                                    FileProcessor fileProcessor) {
        this.sc = new Scanner(System.in);
        this.nodeID = nodeID;
        this.nodeStatus = nodeStatus;
        this.fileProcessor = fileProcessor;
    }

    @Override
    public void run() {
        while(true) {
            if (sc.hasNext()) {
                String input = sc.nextLine();
                String[] parsedInput = input.split(" ");

                //1. Quit:
                if (parsedInput[0].equals("Quit")) {
                    this.nodeStatus.setNodeStayAlive(false);
                    System.out.println("Qutting DHT Process...");

                //2. Store <FILE_NAME>:
                } else if (parsedInput[0].equals("Store")) {
                    try {
                        int requestedFile = Integer.parseInt(parsedInput[1]);
                        boolean hasFileInDirectory = this.fileProcessor.hasFileInDirectory(requestedFile);
                        if (hasFileInDirectory) {
                            this.fileProcessor.insertFile(requestedFile, false);
                        } else {
                            System.out.println("Such file does not exist in the given directory.");
                        }
                    } catch (Exception e) {
                        System.out.println("ERROR: Wrong command format");
                        System.out.println(":: @param Store <FILE_NAME_NUMBER>");
                    }

                //3. Request <FILE_NAME>:
                } else if (parsedInput[0].equals("Request")) {
                    try {
                        int requestedFile = Integer.parseInt(parsedInput[1]);
                        boolean hasFile = this.fileProcessor.hasFile(requestedFile);
                        if (!hasFile) { //If this node does not own this file:
                            this.fileProcessor.requestFile(requestedFile, this.nodeID);
                        } else {
                            System.out.println("This node already holds this file.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: Wrong command format");
                        System.out.println(":: @param Store <FILE_NAME_NUMBER>");
                    }
                }
            }
        }
    }
}
