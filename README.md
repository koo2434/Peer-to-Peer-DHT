# Peer-to-Peer-DHT

Version: JAVA SDK 11

## Instruction

On initial startup:\
@param: <TYPE> <ID> <FIRST_SUCCESSOR> <SECOND_SUCCESSOR> <PING_INTERVAL>\\
Joining existing network:\
@param: <TYPE> <ID> <KNOWN_PEER> <PING_INTERVAL>\\
     TYPE:  'init' for inital startup, 'join' to join an existing network\
     ID: id number of this node\
     FIRST_SUCCESSOR: id number of the first successor\
     SECOND_SUCCESSOR: id number of the second successor\
     KNOWN_PEER: id number of a visible node in the network\
     PING_INTERVAL: time between each ping in seconds\

Refer to 'init.sh' file to use script to activate multiple nodes at once.
  
## Note

> For Data Insertion, nodes will insert files that only exist in './Files' folder. Add files with four digit names to './Files' to test Data insertion.\
> For Data Retrieval, files must be inserted prior to making requests. For example, in a case where file '4103.txt' is in './Files' does not guarantee it will be in one of the nodes in the network.