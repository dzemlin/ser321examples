package server;

import org.json.JSONObject;

import java.net.ServerSocket;
import java.net.Socket;

class SockBaseServer extends Thread{
    public static JSONObject leaderBoard;
    Socket clientSocket = null;
    Game game;


    public SockBaseServer(Socket sock, Game game){
        this.clientSocket = sock;
        this.game = game;
    }

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer. 
    public void run(){
        Performer performer = new Performer(clientSocket, game);
        performer.doPerform();
        try {
            System.out.println("close socket of client ");
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main (String args[]) throws Exception {
        Game game = new Game();

        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099; // default port
        int sleepDelay = 10000; // default delay
        Socket clientSocket = null;
        ServerSocket serv = null;

        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        try {
            serv = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        while (true) {
            System.out.println("Accepting a Connections...");
            clientSocket = serv.accept();
            SockBaseServer server = new SockBaseServer(clientSocket, game);
            server.start();
        }

    }
}

