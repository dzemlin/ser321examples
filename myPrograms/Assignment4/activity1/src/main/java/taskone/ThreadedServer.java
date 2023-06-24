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
import org.json.JSONObject;

/**
 * Class: Server
 * Description: Server tasks.
 */
class ThreadedServer extends Thread{

    private Socket conn;
    private int id;
    private StringList strings;

    public ThreadedServer(Socket sock, int id, StringList strings) {
        this.conn = sock;
        this.id = id;
        this.strings = strings;
    }

    public void run() {
        Performer performer = new Performer(conn, strings);
        performer.doPerform();
        try {
            System.out.println("close socket of client ");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Socket sock = null;
        int id = 0;
        int port;
        StringList strings = new StringList();

        if (args.length != 1) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runTask2 -Pport=9099 -q --console=plain");
            System.exit(1);
        }
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }
        ServerSocket server = new ServerSocket(port);
        System.out.println("Server Started...");
        while (true) {
            System.out.println("Accepting a Request...");
            sock = server.accept();

            ThreadedServer myServerThread = new ThreadedServer(sock, id++, strings);
            myServerThread.start();
        }
    }
}
