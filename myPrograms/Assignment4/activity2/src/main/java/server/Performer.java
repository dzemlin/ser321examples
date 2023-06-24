/**
 File: Performer.java
 Author: Student in Fall 2020B
 Description: Performer class in package taskone.
 */

package server;

import buffers.RequestProtos;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.RequestProtos.Request;
import buffers.ResponseProtos;
import buffers.ResponseProtos.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import static server.SockBaseServer.leaderBoard;

/**
 * Class: Performer
 * Description: Threaded Performer for server tasks.
 */
class Performer {
    static String logFilename = "logs.txt";
    private Game game;
    private Socket conn;
    private String playerName = "";
    private int wins = 0;
    private int logins = 0;

    public Performer(Socket sock, Game game) {
        this.conn = sock;
        this.game = game;
    }

    private Response viewLeaderBoards() {
        Response.Builder res = Response.newBuilder()
                .setResponseType(Response.ResponseType.LEADER);

        Iterator<String> keys = leaderBoard.keys();
        while (keys.hasNext()){
            String nameOut = keys.next();
            int winsOut = leaderBoard.getJSONArray(nameOut).getInt(0);
            int loginsOut = leaderBoard.getJSONArray(nameOut).getInt(1);

            ResponseProtos.Entry leader = ResponseProtos.Entry.newBuilder()
                    .setName(nameOut)
                    .setWins(winsOut)
                    .setLogins(loginsOut)
                    .build();

            res.addLeader(leader);
        }

        Response response = res.build();
        return response;
    }

    private Response playGame() {
        Response response = Response.newBuilder()
                .setResponseType(Response.ResponseType.BYE)
                .setMessage("IM A GAME!!!")
                .build();
        wins += 1;
        updateLeaderBoard();
        saveLeaderBoard();
        return response;
    }

    private Response giveAnswer() {
        System.out.println("IM KNOW THE ANSWER!!!");
        Response response = Response.newBuilder()
                .setResponseType(Response.ResponseType.BYE)
                .setMessage("IM KNOW THE ANSWER!!!")
                .build();
        return response;
    }

    public Response quit() {
        Response response = Response.newBuilder()
                .setResponseType(Response.ResponseType.BYE)
                .setMessage("Goodbye " + playerName + ".")
                .build();
        updateLeaderBoard();
        saveLeaderBoard();
        return response;
    }

    /**
     * Replaces num characters in the image. I used it to turn more than one x when the task is fulfilled
     * @param num -- number of x to be turned
     * @return String of the new hidden image
     */
    public String replace(int num){
        for (int i = 0; i < num; i++){
            if (game.getIdx()< game.getIdxMax())
                game.replaceOneCharacter();
        }
        return game.getImage();
    }


    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect)
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message){
        try {
            // read old log file
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log: logsObj.getLogList()){

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    private void updateLeaderBoard() {
        JSONArray numbers = new JSONArray();
        numbers.put(wins);
        numbers.put(logins);
        leaderBoard.put(playerName, numbers);
    }

    private void saveLeaderBoard() {
        try {
            FileWriter file = new FileWriter("leaderB.json");
            file.write(leaderBoard.toString());
            file.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void loadLeaderBoard() {
        if (leaderBoard != null) {
            return;
        }

        File lb = new File("leaderB.json");
        if (lb.exists()) {
            try {
                String myJson = new Scanner(new File("leaderB.json")).useDelimiter("\\Z").next();
                leaderBoard  = new JSONObject(myJson);
                return;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            leaderBoard = new JSONObject();
            saveLeaderBoard();
        }
    }

    public void doPerform() {
        boolean quit = false;
        try {
            OutputStream out = conn.getOutputStream();
            InputStream in = conn.getInputStream();
            // read the proto object and put into new objct
            Request op = Request.parseDelimitedFrom(in);
            Response res;
            String result = null;



            // if the operation is NAME (so the beginning then say there is a commention and greet the client)
            if (op.getOperationType() == Request.OperationType.NAME) {
                // get name from proto object
                playerName = op.getName();

                // writing a connect message to the log with name and CONNENCT
                writeToLog(playerName, RequestProtos.Message.CONNECT);
                System.out.println("Got a connection and a name: " + playerName);
                loadLeaderBoard();
                if (leaderBoard.has(playerName)) {
                    JSONArray statArry = leaderBoard.getJSONArray(playerName);
                    wins = statArry.getInt(0);
                    logins = statArry.getInt(1) + 1;
                    updateLeaderBoard();
                } else {
                    wins = 0;
                    logins = 1;
                    updateLeaderBoard();
                }
                saveLeaderBoard();

                Response response = Response.newBuilder()
                        .setResponseType(Response.ResponseType.GREETING)
                        .setMessage("Hello " + playerName + " and welcome. \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game")
                        .build();
                response.writeDelimitedTo(out);
            }

            /*
            // Example how to start a new game and how to build a response with the image which you could then send to the server
            // LINE 67-108 are just an example for Protobuf and how to work with the differnt types. They DO NOT
            // belong into this code.
            game.newGame(); // starting a new game

            // adding the String of the game to
            Response response2 = Response.newBuilder()
                    .setResponseType(Response.ResponseType.TASK)
                    .setImage(game.getImage())
                    .setTask("Great task goes here")
                    .build();

            // On the client side you would receive a Response object which is the same as the one in line 70, so now you could read the fields
            System.out.println("Task: " + response2.getResponseType());
            System.out.println("Image: \n" + response2.getImage());
            System.out.println("Task: \n" + response2.getTask());

            // Creating Entry and Leader response
            Response.Builder res = Response.newBuilder()
                    .setResponseType(Response.ResponseType.LEADER);

            // building and Entry
            Entry leader = Entry.newBuilder()
                    .setName("name")
                    .setWins(0)
                    .setLogins(0)
                    .build();

            // building and Entry
            Entry leader2 = Entry.newBuilder()
                    .setName("name2")
                    .setWins(1)
                    .setLogins(1)
                    .build();

            res.addLeader(leader);
            res.addLeader(leader2);

            Response response3 = res.build();

            for (Entry lead: response3.getLeaderList()){
                System.out.println(lead.getName() + ": " + lead.getWins());
            }
            */


                while (!quit) {
                    op = Request.parseDelimitedFrom(in);
                    System.out.println("Got a Message!");

                    if (op.hasOperationType()) {
                        if (op.getOperationType() == Request.OperationType.LEADER) {
                            res = viewLeaderBoards();
                            res.writeDelimitedTo(out);
                        }
                        if (op.getOperationType() == Request.OperationType.NEW) {
                            res = playGame();
                            res.writeDelimitedTo(out);
                        }
                        if (op.getOperationType() == Request.OperationType.ANSWER) {
                            res = giveAnswer();
                            res.writeDelimitedTo(out);
                        }
                        if (op.getOperationType() == Request.OperationType.QUIT) {
                            res = quit();
                            res.writeDelimitedTo(out);
                            quit = true;
                        }
                    } else {
                        res = Response.newBuilder()
                                .setResponseType(Response.ResponseType.ERROR)
                                .setMessage("Your request was not recognized, please try again")
                                .build();
                        res.writeDelimitedTo(out);
                    }
                }
                // close the resource
                System.out.println("close the resources of client ");
                out.close();
                in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
