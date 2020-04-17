# Peer-to-Peer-DHT

## Message headers

REQUEST/ = header for request packet
RESPONSE/ = header for packet in response to REQUEST
NOTIFY/ = header for notifying the target node
DATA/ = header for data packet

REQUEST/PING:
  Format: REQUEST/PING:<HOST_ID#>:<SUCCESSOR_PRIORITY>
  
RESPONSE/PING = Ping response
REQUEST/JOIN = 