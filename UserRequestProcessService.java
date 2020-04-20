
import java.io.*;
import java.net.*;
import java.util.*;

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

                if (parsedInput[0].equals("Quit")) {
                    this.nodeStatus.setNodeStayAlive(false);
                    System.out.println("Qutting DHT Process...");
                } else if (parsedInput[0].equals("Store")) {
                    try {
                        int requestedFile = Integer.parseInt(parsedInput[1]);
                        File file = new File("./");
                        FilenameFilter filter = new FilenameFilter() {
                            public boolean accept (File file, String fileName) {
                                return fileName.startsWith(requestedFile + "");
                            }
                        };
                        String[] foundFiles = file.list(filter);
                        if (foundFiles != null && foundFiles.length > 0) {
                            this.fileProcessor.insertFile(requestedFile);
                        } else {
                            System.out.println("Such file does not exist in the given directory.");
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
