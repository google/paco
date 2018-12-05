#
# Jetty HTTP Connector
#

[depend]
server

[xml]
etc/jetty-http.xml

[ini-template]
### HTTP Connector Configuration

## Connector host/address to bind to
# jetty.http.host=0.0.0.0

## Connector port to listen on
# jetty.http.port=8080

## Connector idle timeout in milliseconds
# jetty.http.idleTimeout=30000

## Connector socket linger time in seconds (-1 to disable)
# jetty.http.soLingerTime=-1

## Number of acceptors (-1 picks default based on number of cores)
# jetty.http.acceptors=-1

## Number of selectors (-1 picks default based on number of cores)
# jetty.http.selectors=-1

## ServerSocketChannel backlog (0 picks platform default)
# jetty.http.acceptorQueueSize=0

## Thread priority delta to give to acceptor threads
# jetty.http.acceptorPriorityDelta=0

## HTTP Compliance: RFC7230, RFC2616, LEGACY
# jetty.http.compliance=RFC7230
