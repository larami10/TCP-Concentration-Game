# TCP Concentration Game

A simple Concentration game using TCP Sockets. It has the following features:

1. Run server through terminal.
2. Connect a client to server (default port is 8080).
3. Once connection is made, a GUI will display for user to play Concentration game.
4. Multiple servers can run at a time, but one client can connect per server.
5. The server terminal will display the user's actions.

## How to use the Project

You can clone the repository and:

```
cd TCP-Concentration-Game
```

### To Run Concentration Game in default port 8080

To run the server in default port 8080:

```
gradle runServer
```

To connect the client to default port 8080:

```
gradle runClient
```

### To Run Concentration Game in port 8000

To run server in port 8000:

```
gradle runServer -Pport=8000
```

connect client to port 8000:

```
gradle runClient -Pport=8000 -Phost=localhost
```
