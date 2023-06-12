package Assign32starter;

import java.awt.Dimension;

import org.json.*;
import java.awt.event.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.Base64;
import java.util.Iterator;

import javax.swing.*;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui implements Assign32starter.OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picPanel;
	OutputPanel outputPanel;
	Socket sock;
	OutputStream out;
	InputStream in;
	String host;
	int port;
	int pass = 0;

	/**
	 * Construct dialog
	 * @throws IOException 
	 */
	public ClientGui(String host, int port) throws IOException {
		this.host = host; 
		this.port = port; 

		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// setup the top picture frame
		picPanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picPanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);

		picPanel.newGame(1);

		// Add listener so a close message can be sent to the server on exit
		WindowListener listener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				closeApp();
			}
		};
		frame.addWindowListener(listener);

		// send connection request
		sendSubmission("start", true);
	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}

	/**
	 * Creates a new game and set the size of the grid 
	 * @param dimension - the size of the grid will be dimension x dimension
	 * No changes should be needed here
	 */
	public void newGame(int dimension) {
		picPanel.newGame(1);
		outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
	}

	/**
	 * Insert an image into the grid at position (col, row)
	 * 
	 * @param res - JSONObject that have the image data
	 * @param row - the row to insert into
	 * @param col - the column to insert into
	 * @return true if successful, false if an invalid coordinate was provided
	 * @throws IOException An error occured with your image file
	 */
	public boolean insertImage(JSONObject res, int row, int col) throws IOException {
		Base64.Decoder dcdr = Base64.getDecoder();
		byte[] bytes = dcdr.decode(res.getString("image"));

		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			picPanel.insertImage(bais, row, col);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void sendSubmission(String submission, boolean getResponce) {
		open();

		JSONObject connReq = new JSONObject();
		try {
			connReq.put("submission", submission);
			if (submission.equals("quit")) {
				connReq.put("pass", pass);
			}
			NetworkUtils.Send(out, JsonUtils.toByteArray(connReq));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (getResponce) {
			JSONObject response = new JSONObject();
			try {
				byte[] responseBytes = NetworkUtils.Receive(in);
				System.out.println("Got a response from server");
				response = JsonUtils.fromByteArray(responseBytes);
			} catch (IOException ioe) {
				System.out.println("No response from server");
				ioe.printStackTrace();
				return;
			}

			String type = response.getString("type");
			if (type.equals("start")) {
				readResponseStart(response);
			} else if (type.equals("login")) {
				readResponseLogin(response);
			} else if (type.equals("leaderBoard")) {
				readResponseLeaderBoard(response);
			} else if (type.equals("play")) {
				readResponsePlay(response);
			} else if (type.equals("more")) {
				readResponseMore(response);
			} else if (type.equals("next")) {
				readResponseNext(response);
			} else if (type.equals("guess")) {
				readResponseGuess(response);
			} else if (type.equals("gameOver")) {
				readResponseGameOver(response);
			} else if (type.equals("error")) {
				readResponseError(response);
			}

			closeConn();
		}
	}

	public void readResponseError(JSONObject response) {
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
		}
		if (response.has("userError")) {
			outputPanel.appendOutput(response.getString("userError"));
		}
	}

	public void readResponseStart(JSONObject response) {
		if (response.has("image")) {
			try {
				insertImage(response, 0, 0);
			} catch (IOException ioe) {
				System.out.println("Received image with invalid data");
				ioe.printStackTrace();
			}
		}
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
		}
		if (response.has("systemError")) {
			outputPanel.appendOutput(response.getString("systemError"));
		}
		if (response.has("pass")) {
			pass = response.getInt("pass");
		}
	}

	public void readResponseLogin(JSONObject response) {
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
		}
		if (response.has("message2")) {
			outputPanel.appendOutput(response.getString("message2"));
		}
		if (response.has("message3")) {
			outputPanel.appendOutput(response.getString("message3"));
		}
		if (response.has("userError")) {
			outputPanel.appendOutput(response.getString("userError"));
		}
		if (response.has("systemError")) {
			outputPanel.appendOutput(response.getString("systemError"));
		}
	}

	public void readResponseLeaderBoard(JSONObject response) {
		if (response.has("object")) {
			outputPanel.appendOutput("-----------------------------------------------------------");
			outputPanel.appendOutput("------------- Persistent Leader Board -------------");
			JSONObject lb = response.getJSONObject("object");
			Iterator<String> keys = lb.keys();
			while (keys.hasNext()){
				String key = keys.next();
				outputPanel.appendOutput(key + " = " + lb.getInt(key));
			}
			outputPanel.appendOutput("-----------------------------------------------------------");
			outputPanel.appendOutput("-----------------------------------------------------------");
		}
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
		}
	}

	public void readResponsePlay (JSONObject response) {

		if (response.has("image")) {
			try {
				insertImage(response, 0, 0);
			} catch (IOException ioe) {
				System.out.println("Received image with invalid data");
				ioe.printStackTrace();
			}
		}
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
		}
		if (response.has("systemError")) {
			outputPanel.appendOutput(response.getString("systemError"));
		}
	}

	public void readResponseMore (JSONObject response) {
		if (response.has("image")) {
			try {
				insertImage(response, 0, 0);
			} catch (IOException ioe) {
				System.out.println("Received image with invalid data");
				ioe.printStackTrace();
			}
		}
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
		}
		if (response.has("systemError")) {
			outputPanel.appendOutput(response.getString("systemError"));
		}
		if (response.has("userError")) {
			outputPanel.appendOutput(response.getString("userError"));
		}
	}

	public void readResponseNext (JSONObject response) {
		if (response.has("image")) {
			try {
				insertImage(response, 0, 0);
			} catch (IOException ioe) {
				System.out.println("Received image with invalid data");
				ioe.printStackTrace();
			}
		}
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
		}
		if (response.has("message2")) {
			outputPanel.appendOutput(response.getString("message2"));
		}
		if (response.has("message3")) {
			outputPanel.appendOutput(response.getString("message3"));
		}
		if (response.has("message4")) {
			outputPanel.appendOutput("-----------------------------------------------------------");
			outputPanel.appendOutput(response.getString("message4"));
		}
		if (response.has("points")) {
			outputPanel.setPoints(response.getInt("points"));
		}
		if (response.has("systemError")) {
			outputPanel.appendOutput(response.getString("systemError"));
		}
	}

	public void readResponseGuess (JSONObject response) {
		if (response.has("image")) {
			try {
				insertImage(response, 0, 0);
			} catch (IOException ioe) {
				System.out.println("Received image with invalid data");
				ioe.printStackTrace();
			}
		}
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
			outputPanel.appendOutput("-----------------------------------------------------------");
		}
		if (response.has("message2")) {
			outputPanel.appendOutput(response.getString("message2"));
		}
		if (response.has("message3")) {
			outputPanel.appendOutput(response.getString("message3"));
		}
		if (response.has("message4")) {
			outputPanel.appendOutput("-----------------------------------------------------------");
			outputPanel.appendOutput(response.getString("message4"));
		}
		if (response.has("points")) {
			outputPanel.setPoints(response.getInt("points"));
		}
		if (response.has("userError")) {
			outputPanel.appendOutput(response.getString("userError"));
		}
		if (response.has("systemError")) {
			outputPanel.appendOutput(response.getString("systemError"));
		}
	}

	public void readResponseGameOver (JSONObject response) {
		if (response.has("image")) {
			try {
				insertImage(response, 0, 0);
			} catch (IOException ioe) {
				System.out.println("Received image with invalid data");
				ioe.printStackTrace();
			}
		}
		if (response.has("message")) {
			outputPanel.appendOutput(response.getString("message"));
			outputPanel.appendOutput("-----------------------------------------------------------");
		}
		if (response.has("message2")) {
			outputPanel.appendOutput(response.getString("message2"));
		}
		if (response.has("message3")) {
			outputPanel.appendOutput(response.getString("message3"));
		}
		if (response.has("message4")) {
			outputPanel.appendOutput("-----------------------------------------------------------");
			outputPanel.appendOutput(response.getString("message4"));
		}
		if (response.has("points")) {
			outputPanel.setPoints(response.getInt("points"));
		}
		if (response.has("systemError")) {
			outputPanel.appendOutput(response.getString("systemError"));
		}
	}

	/**
	 * Submit button handling
	 * Right now this method opens and closes the connection after every interaction, if you want to keep that or not is up to you. 
	 */
	@Override
	public void submitClicked() {
		System.out.println("submit clicked ");

		// Pulls the input box text
		String input = outputPanel.getInputText();
		outputPanel.setInputText("");

		if (input.equals("quit")) {
			sendSubmission(input, false);
			closeConn();
			frame.setVisible(false);
			frame.dispose();
		} else {
			sendSubmission(input, true);
		}
	}

	/**
	 * Key listener for the input text box
	 * Change the behavior to whatever you need
	 */
	@Override
	public void inputUpdated(String input) {
		if (input.equals("surprise")) {
			outputPanel.appendOutput("You found me!");
		}
	}

	public void open() {
		try {
			sock = new Socket(host, port);
			out = sock.getOutputStream();
			in = sock.getInputStream();
		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}
	
	public void closeConn() {
        try {
            if (out != null)  out.close();
            if (sock != null) sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void closeApp() {
		sendSubmission("quit", false);
		closeConn();
		frame.setVisible(false);
		frame.dispose();
	}

	public static void main(String[] args) throws IOException {
		// create the frame
		try {
			String host = args[0];
			int port = Integer.parseInt(args[1]);

			ClientGui main = new ClientGui(host, port);
			main.show(true);

		} catch (Exception e) {e.printStackTrace();}
	}
}
