
public class P2P {

    public static final int PORT_OFFSET = 12000;

    public static void printInstruction() {
        System.out.println(":: P2P DHT Simulator");
        System.out.println(":: On initial startup: ");
        System.out.println(":: @param: <TYPE> <ID> <FIRST_SUCCESSOR> <SECOND_SUCCESSOR> <PING_INTERVAL>");
        System.out.println(":: Joining existing network: ");
        System.out.println(":: @param: <TYPE> <ID> <KNOWN_PEER> <PING_INTERVAL>");
        System.out.println("::     TYPE:  'init' for inital startup, 'join' to join an existing network");
        System.out.println("::     ID: id number of this node");
        System.out.println("::     FIRST_SUCCESSOR: id number of the first successor");
        System.out.println("::     SECOND_SUCCESSOR: id number of the second successor");
        System.out.println("::     KNOWN_PEER: id number of a visible node in the network");
        System.out.println("::     PING_INTERVAL: time between each ping in seconds");
    }

    public static void main(String[] args) {
        if (args.length != 4 && args.length != 5) {
            printInstruction();
            System.out.println(args.length);
            return;
        }

        try {
            String type = args[0].trim().toLowerCase();
            int nodeID = Integer.parseInt(args[1]);

            if (type.equals("init")) {
                int firstSuccessorNodeID = Integer.parseInt(args[2]);
                int secondSuccessorNodeID = Integer.parseInt(args[3]);
                int pingInterval = Integer.parseInt(args[4]);

                PeerNode node = new PeerNode(nodeID, firstSuccessorNodeID,
                                        secondSuccessorNodeID, pingInterval,
                                        PORT_OFFSET);
                System.out.println("System starting...");
                node.begin();

            } else if (type.equals("join")) {
                int knownPeerID = Integer.parseInt(args[2]);
                int pingInterval = Integer.parseInt(args[3]);

                //TODO: Implement JOIN
            }

        } catch (IllegalArgumentException e) {
            printInstruction();
            System.out.println("ARgument");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
}
