
import java.util.*;
/**
 * Data class to indicate node and network status.
 */
class NodeStatus {

    private volatile Map<Integer, Integer> outPingCount;

    private volatile boolean notifyJoinedSuccessor;
    private volatile int joinedSuccessor;

    private volatile boolean secondarySuccessorReceived;
    private volatile int secondarySuccessor;

    private volatile boolean successorsChanging;

    private volatile boolean nodeStayAlive;
    private volatile boolean quitComplete;

    public NodeStatus(int nodeID, List<Integer> successorIDList) {
        this.outPingCount = new HashMap<>();
        this.notifyJoinedSuccessor = false;
        this.joinedSuccessor = -1;
        this.secondarySuccessorReceived = false;
        this.secondarySuccessor = -1;
        this.successorsChanging = false;

        this.nodeStayAlive = true;
        this.quitComplete = false;
    }

    /**
     * Getter of ping count, value of outPingCount
     * @param id nodeID of the pinging target
     * @return ping count of the given target. Returns -1 if the @param is not in the map.
     */
    public int getOutPingCount (int id) {
        if (this.outPingCount.containsKey(id)) {
            return this.outPingCount.get(id);
        } else {
            return -1;
        }
    }

    /**
     * Setter of ping count, value of outPingCount. Increments the ping count
     * by 1 if @param exists. If @param does not exist in the map, sets
     * ping count as 1.
     * @param id nodeID of the pinging target.
     */
    public synchronized void incrementOutPingCount(int id) {
        if (this.outPingCount.containsKey(id)) {
            this.outPingCount.put(id, this.outPingCount.get(id) + 1);
        } else {
            this.outPingCount.put(id, 1);
        }
    }
    public synchronized void setOutPingCount (int id, int ct) {
        if (this.outPingCount.containsKey(id)) {
            this.outPingCount.put(id, ct);
        }
    }

    /**
     * Returns a list of nodeIDs of targeted nodes.
     * @return a list of nodeIDs
     */
    public List<Integer> getTargetIDList() {
        return new ArrayList<Integer>(this.outPingCount.keySet());
    }

    /**
     * Sets outPingCount with new sets of key values.
     * Returns a list of old targeted node ids.
     * @return a list of nodeIDs
     */
    public synchronized List<Integer> setNewOutPingCount (List<Integer> newTargetIDs) {
        List<Integer> oldIDs = this.getTargetIDList();
        this.outPingCount = new HashMap<>();

        for (int x : newTargetIDs) {
            this.outPingCount.put(x, 0);
        }

        return oldIDs;
    }

    public void setJoinedSuccessor(int clientNodeID) {
        this.notifyJoinedSuccessor = true;
        this.joinedSuccessor = clientNodeID;
    }
    public int getJoinedSuccessor() {
        return this.joinedSuccessor;
    }
    public void setNotifyJoinedSuccessor(boolean notifyJoinedSuccessor) {
        this.notifyJoinedSuccessor = notifyJoinedSuccessor;
    }
    public boolean isNotifyJoinedSuccessor() {
        return this.notifyJoinedSuccessor;
    }
    public boolean isSecondarySuccessorReceived() {
        return this.secondarySuccessorReceived;
    }
    public void setSecondarySuccessorReceived(boolean secondarySuccessorReceived) {
        this.secondarySuccessorReceived = secondarySuccessorReceived;
    }
    public int getSecondarySuccessor() {
        return this.secondarySuccessor;
    }
    public void setSecondarySuccessor(int secondarySuccessor) {
        this.secondarySuccessor = secondarySuccessor;
    }
    public boolean isSuccessorsChanging(){
        return this.successorsChanging;
    }
    public void setSuccessorsChanging(boolean successorsChanging) {
        this.successorsChanging = successorsChanging;
    }
    public boolean isNodeStayAlive() {
        return this.nodeStayAlive;
    }
    public void setNodeStayAlive(boolean nodeStayAlive) {
        this.nodeStayAlive = nodeStayAlive;
    }
    public boolean isQuitComplete() {
        return this.quitComplete;
    }
    public void setQuitComplete(boolean quitComplete) {
        this.quitComplete = quitComplete;
    }

}
