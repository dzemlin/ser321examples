/**
 File: Server.java
 Author: Student in Fall 2020B
 Description: Server class in package taskone.
 */

package taskone;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/**
 * Class: Server
 * Description: Server tasks.
 */
class ThreadPoolServer extends Thread{

    private ServerSocket server;
    private Socket conn;
    private int id;
    private StringList strings;
    private boolean activeConn = false;

    public ThreadPoolServer(ServerSocket serverSock, int id, StringList strings) {
        this.server = serverSock;
        this.id = id;
        this.strings = strings;
    }

    public void run() {
        while (true) {
            if (!activeConn) {
                System.out.println("Accepting a Request...");
                try {
                    conn = server.accept();
                    activeConn = true;
                    Performer performer = new Performer(conn, strings);
                    performer.doPerform();

                    System.out.println("close socket of client ");
                    conn.close();
                    activeConn = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Socket sock = null;
        int id = 0;
        int port;
        int limit = -1;
        StringList strings = new StringList();

        if (args.length != 2) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runTask3 -Pport=9099 -Psize=5 -q --console=plain");
            System.exit(1);
        }
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }
        try {
            limit = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Size] must be an integer");
            System.exit(2);
        }

        Executor pool = Executors.newFixedThreadPool(limit);

        ServerSocket serv = new ServerSocket(port);
        System.out.println("Server Started...");

        for (int i = 0; i < limit; i++) {
            pool.execute(new ThreadPoolServer(serv, id++, strings));
        }
    }
}
