/**
  File: Performer.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package taskone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

/**
 * Class: Performer 
 * Description: Threaded Performer for server tasks.
 */
class Performer {

    private StringList state;
    private Socket conn;

    public Performer(Socket sock, StringList strings) {
        this.conn = sock;
        this.state = strings;
    }

    public JSONObject add(String str) {
        JSONObject json = new JSONObject();
        if (str == null || str.length() < 1) {
            json = error("Given string was blank");
            json.put("ok", false);
            return json;
        }
        if (state.contains(str)) {
            json = error("String is already in the list");
            json.put("ok", false);
            return json;
        }
        state.add(str);
        json.put("ok", true);
        json.put("type", "add");
        json.put("data", state.toString());
        return json;
    }

    public JSONObject clear() {
        JSONObject json = new JSONObject();
        if (state.size() > 0) {
            state.clear();
            json.put("ok", true);
            json.put("type", "clear");
            json.put("data", state.toString());
        } else {
            json = error("List is already clear");
            json.put("ok", false);
        }
        return json;
    }

    public JSONObject find(String str) {
        JSONObject json = new JSONObject();
        int indx = state.find(str);
        json.put("ok", true);
        json.put("type", "find");
        json.put("data", indx + "");
        return json;
    }

    public JSONObject display() {
        JSONObject json = new JSONObject();
        if (state.size() > 0) {
            json.put("ok", true);
            json.put("type", "display");
            json.put("data", state.toString());
        } else {
            json = error("List is empty");
            json.put("ok", false);
        }
        return json;
    }

    public JSONObject sort() {
        JSONObject json = new JSONObject();
        if (state.size() > 0) {
            state.sort();
            json.put("ok", true);
            json.put("type", "sort");
            json.put("data", state.toString());
        } else {
            json = error("List is empty");
            json.put("ok", false);
        }
        return json;
    }

    public JSONObject prepend(String str, int indx) {
        System.out.println(str.length());
        System.out.println(indx + " = index");
        JSONObject json = new JSONObject();
        if (str == null || state.size() <= indx ) {
            json = error("No string at given index");
            json.put("ok", false);
        } else {
            state.prepend(str, indx);
            json.put("ok", true);
            json.put("type", "prepend");
            json.put("data", state.toString());
        }
        return json;
    }

    public JSONObject quit() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "quit");
        json.put("data", "A Client has quit");
        return json;
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("error", err);
        return json;
    }


    public void doPerform() {
        boolean quit = false;
        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client:");
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                System.out.println("Got a Message!");
                JSONObject returnMessage = new JSONObject();
   
                int choice = message.getInt("selected");
                String inStr;
                    switch (choice) {
                        case (1):
                            inStr = (String) message.get("data");
                            returnMessage = add(inStr);
                            break;
                        case (2):
                            returnMessage = clear();
                            break;
                        case (3):
                            inStr = (String) message.get("data");
                            returnMessage = find(inStr);
                            break;
                        case (4):
                            returnMessage = display();
                            break;
                        case (5):
                            returnMessage = sort();
                            break;
                        case (6):
                            String inStrAry[] = ((String) message.get("data")).split(" ", 2);
                            inStr = inStrAry[1];
                            int indx = Integer.parseInt(inStrAry[0]);
                            returnMessage = prepend(inStr, indx);
                            break;
                        case (0):
                            returnMessage = quit();
                            quit = true;
                            break;
                        default:
                            returnMessage = error("Invalid selection: " + choice 
                                    + " is not an option");
                            break;
                    }
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
