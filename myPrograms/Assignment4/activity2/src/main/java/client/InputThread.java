package client;

import buffers.RequestProtos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public class InputThread extends Thread {
    BufferedReader input;
    OutputStream out;
    SockBaseClient client;

    public InputThread(BufferedReader input, OutputStream out) {
        this.input = input;
        this.out = out;
    }

    public void run() {
        while (true) {
            String toSend = "";
            try {
                toSend = input.readLine();
            } catch (IOException ioe) {
                System.out.println("Invalid Input");
            }
            if (toSend == null && toSend.length() > 0) {
            } else if (toSend.equals("exit")) {
                client.quit(out);
                break;
            } else {
                RequestProtos.Request op = RequestProtos.Request.newBuilder()
                        .setOperationType(RequestProtos.Request.OperationType.ANSWER)
                        .setAnswer(toSend).build();
                try {
                    op.writeDelimitedTo(out);
                } catch (Exception e) {
                    System.out.println("Invalid Input");
                }

            }
        }
    }
}
