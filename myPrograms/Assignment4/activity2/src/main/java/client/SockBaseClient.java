package client;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos;
import buffers.ResponseProtos.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

class SockBaseClient {

    private static boolean mainMenuResponces(String toSend, OutputStream out) {
        if (toSend == null) {
            return false;
        } else if (toSend.equals("exit")) {
            quit(out);
            return true;
        } else if (toSend.equals("1")) {
            Request op = Request.newBuilder()
                    .setOperationType(Request.OperationType.LEADER).build();
            try {
                op.writeDelimitedTo(out);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else if (toSend.equals("2")) {
            Request op = Request.newBuilder()
                    .setOperationType(Request.OperationType.NEW).build();
            try {
                op.writeDelimitedTo(out);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("Not a valid option. Please enter '1', '2', or 'exit' ");
            return false;
        }
    }

    private static boolean leaderBoardResponces(String toSend, OutputStream out) {
        if (toSend == null) {
            return false;
        } else if (toSend.equals("exit")) {
            quit(out);
            return true;
        } else if (toSend.equals("1")) {
            Request op = Request.newBuilder()
                    .setOperationType(Request.OperationType.NEW).build();
            try {
                op.writeDelimitedTo(out);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("Not a valid option. Please enter '1' or 'exit' ");
            return false;
        }
    }

    public static void quit(OutputStream out) {
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.QUIT).build();
        try {
            op.writeDelimitedTo(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int i1=0, i2=0;
        int port = 9099; // default port

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        // Ask user for username
        System.out.println("Please provide your name for the server. ( ͡❛ ͜ʖ ͡❛)");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String toSend = stdin.readLine();

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(toSend).build();
        Response response;
        try {
            // connect to the server
            serverSock = new Socket(host, port);

            // write to the server
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            op.writeDelimitedTo(out);

            response = Response.parseDelimitedFrom(in);

            boolean loop = true;
            boolean goodInput;
            while (loop) {
                goodInput = false;
                if (response.hasResponseType()) {
                    if (response.getResponseType() == Response.ResponseType.GREETING) {
                        System.out.println(response.getMessage());
                        toSend = stdin.readLine();
                        goodInput = mainMenuResponces(toSend, out);
                    }
                    if (response.getResponseType() == Response.ResponseType.LEADER) {
                        for (ResponseProtos.Entry lead: response.getLeaderList()){
                            System.out.println(lead.getName() + ": Wins = " + lead.getWins() + ": Logins = " + lead.getLogins());
                        }
                        System.out.println("Enter '1' to start the game.");
                        toSend = stdin.readLine();
                        goodInput = leaderBoardResponces(toSend, out);

                    }
                    if (response.getResponseType() == Response.ResponseType.TASK) {
                        toSend = stdin.readLine();
                        //runTask
                    }
                    if (response.getResponseType() == Response.ResponseType.WON) {
                        toSend = stdin.readLine();
                        //restartGame
                        //runMainMenu
                    }
                    if (response.getResponseType() == Response.ResponseType.ERROR) {
                        toSend = stdin.readLine();
                        //rerunOptionsFromLastValidState
                    }
                    if (response.getResponseType() == Response.ResponseType.BYE) {
                        loop = false;
                    }
                } else {
                    //rerunOptionsFromLastValidState
                }

                if (goodInput) {
                    response = Response.parseDelimitedFrom(in);
                }
            }
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
        }
    }
}


