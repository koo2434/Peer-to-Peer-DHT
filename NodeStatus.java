
import java.util.*;
/**
 * Data class to indicate node and network status.
 */
class NodeStatus {

    private volatile boolean circuitAlive;
    private volatile Map<Integer, Integer> pingCounter;

    public NodeStatus() {
        this.circuitAlive = true;
        this.pingCounter = new HashMap();
    }

    public boolean isCircuitAlive() {
        return this.circuitAlive;
    }
    public synchronized void setCircuitAlive(boolean circuitStatus) {
        this.circuitAlive = circuitStatus;
    }

    /**
     * Getter of ping count, value of pingCounter
     * @param id nodeID of the pinging target
     * @return ping count of the given target. Returns -1 if the @param is not in the map.
     */
    public int getPingCount (int id) {
        if (this.pingCounter.containsKey(id)) {
            return this.pingCounter.get(id);
        } else {
            return -1;
        }
    }

    /**
     * Setter of ping count, value of pingCounter. Increments the ping count
     * by 1 if @param exists. If @param does not exist in the map, sets
     * ping count as 1.
     * @param id nodeID of the pinging target.
     */
    public synchronized void incrementPingCount(int id) {
        if (this.pingCounter.containsKey(id)) {
            this.pingCounter.put(id, this.pingCounter.get(id) + 1);
        } else {
            this.pingCounter.put(id, 1);
        }
    }
    /**
     * Setter of ping count, value of pingCounter. Decrements the ping count
     * by 1 if @param exists.
     * @param id nodeID of the pinging target.
     */
    public synchronized void decrementPingCount(int id) {
        if (this.pingCounter.containsKey(id) && this.pingCounter.get(id) > 0) {
            this.pingCounter.put(id, this.pingCounter.get(id) - 1);
        }
    }

    /**
     * Returns a list of nodeIDs of targeted nodes.
     * @return a list of nodeIDs
     */
    public List<Integer> getTargetIDList() {
        return new ArrayList<Integer>(this.pingCounter.keySet());
    }

    /**
     * Sets pingCounter with new sets of key values.
     * Returns a list of old targeted node ids.
     * @return a list of nodeIDs
     */
    public synchronized List<Integer> setNewPingCounter(List<Integer> newTargetIDs) {
        List<Integer> oldIDs = this.getTargetIDList();
        this.pingCounter = new HashMap<>();

        for (int x : newTargetIDs) {
            this.pingCounter.put(x, 0);
        }

        return oldIDs;
    }
}
