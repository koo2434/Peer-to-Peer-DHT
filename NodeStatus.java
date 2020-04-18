
import java.util.*;
/**
 * Data class to indicate node and network status.
 */
class NodeStatus {

    private volatile Map<Integer, Integer> outPingCount;

    private volatile boolean notifyNewSuccessor;
    private volatile int newSuccessor;

    private volatile boolean circuitAlive;

    private volatile boolean nodeStayAlive;
    private volatile boolean quitComplete;

    public NodeStatus(int nodeID, List<Integer> successorIDList) {
        this.outPingCount = new HashMap<>();
        this.notifyNewSuccessor = false;
        this.newSuccessor = -1;
        this.circuitAlive = true;
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
    /**
     * Setter of ping count, value of outPingCount. Decrements the ping count
     * by 1 if @param exists.
     * @param id nodeID of the pinging target.
     */
    public synchronized void decrementOutPingCount (int id) {
        if (this.outPingCount.containsKey(id) && this.outPingCount.get(id) > 0) {
            this.outPingCount.put(id, this.outPingCount.get(id) - 1);
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

    public void setNewSuccessor(int clientNodeID) {
        this.notifyNewSuccessor = true;
        this.newSuccessor = clientNodeID;
    }
    public int getNewSuccessor() {
        return this.newSuccessor;
    }
    public void setNotifyNewSuccessor(boolean notifyNewSuccessor) {
        this.notifyNewSuccessor = notifyNewSuccessor;
    }
    public boolean isNotifyNewSuccessor() {
        return this.notifyNewSuccessor;
    }
    public boolean isCircuitAlive() {
        return this.circuitAlive;
    }
    public synchronized void setCircuitAlive(boolean circuitStatus) {
        this.circuitAlive = circuitStatus;
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
