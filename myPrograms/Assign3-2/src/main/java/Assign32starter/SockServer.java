package Assign32starter;
import java.awt.image.BufferedImage;
import java.net.*;
import java.util.*;

import java.io.*;
import org.json.*;

import javax.imageio.ImageIO;

/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public class SockServer {
	static final int PICTURES_PER_LOCATION = 4;
	static String currentLocation = "";
	static int currentPicNum = 0;
	static Stack<String> locationStack = new Stack<>();
	static Stack<Integer> pictureNumberStack = new Stack<>();
	static Socket sock;
	static InputStream in;
	static OutputStream out;
	static int port;
	static ServerSocket serv;
	static JSONObject leaderBoard = new JSONObject();
	static String loginName;
	static int points = 0;
	static int gameState = 0; // 0 = waiting for connection, 1 = waiting for login, 2 = mode select, 3 = Leader Boards, 4 = In Game, 5 = End Game
	static int pass = 0;

	public static void main (String args[]) {

		// check for arguments
		if (args.length != 1) {
			System.out.println("Requires arguments: <port(int)>");
			System.exit(1);
		}

		// parse arguments
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException nfe) {
			System.out.println("port must be an integer");
			System.exit(2);
		}

		// initialize leader board
		loadLeaderBoard();

		// open socket
		try {
			serv = new ServerSocket(port);
			System.out.println("Server ready for connetion");

			// placeholder for the person who wants to play a game
			loginName = "";
			points = 0;

			// read in one object, the message. we know a string was written only by knowing what the client sent. 
			// must cast the object from Object to desired type to be useful
			System.out.println("waiting for connection");
			while(true) {
				sock = serv.accept(); // blocking wait

				// set up input and output streams
				in = sock.getInputStream();
				out = sock.getOutputStream();

				byte[] messageBytes = NetworkUtils.Receive(in);
				JSONObject inMessage = JsonUtils.fromByteArray(messageBytes);

				if ((inMessage.getString("submission").equals("quit"))) {
					comQuit(inMessage);
				} else if (gameState != 0 && inMessage.getString("submission").equals("start")) {
					comReject();
				} else if (gameState == 0 && inMessage.getString("submission").equals("start")) {
					comStart();
				} else if (gameState == 1) {
					comLogin(inMessage);
				} else if ((gameState == 2 || gameState == 5) && inMessage.getString("submission").equals("s")) {
					comLeaderBoard();
				} else if ((gameState == 2 || gameState == 3 || gameState == 5) && inMessage.getString("submission").equals("p")) {
					comPlay();
				} else if ((gameState == 4) && inMessage.getString("submission").equals("more")) {
					comMore();
				} else if ((gameState == 4) && inMessage.getString("submission").equals("next")) {
					comNext();
				} else if ((gameState == 4)) {
					comGuess(inMessage);
				} else {
					comInvalid(inMessage);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void createDeck() {
		locationStack.push("ireland");
		locationStack.push("berlin");
		locationStack.push("paris");
		locationStack.push("phoenix");
		locationStack.push("rome");
		locationStack.push("SanFransisco");
		locationStack.push("switzerland");

		Collections.shuffle(locationStack);
	}

	private static void shuffleNumbers(int max) {
		for (int i = 1; i <= max; i++) {
			pictureNumberStack.push(i);
		}
		Collections.shuffle(pictureNumberStack);
	}

	public static void comInvalid(JSONObject inMessage) {
		JSONObject outMessage = new JSONObject();
		System.out.println("Invalid Submission: " + inMessage.get("submission").toString());
		outMessage.put("type", "error");
		outMessage.put("userError", "Invalid Submission");
		sendmesage(out, outMessage);
		closeConn();
	}

	public static void comQuit(JSONObject inMessage) {
		if (inMessage.getInt("pass") == pass) {
			System.out.println("Client has quit the session");
			currentLocation = "";
			currentPicNum = 0;
			locationStack = new Stack<>();
			pictureNumberStack = new Stack<>();
			loginName = null;
			points = 0;
			gameState = 0;
			closeConn();
		}
	}

	public static void comReject() {
		JSONObject outMessage = new JSONObject();
		System.out.println("- Blocked an extra client");

		outMessage.put("type", "start");
		try {
			outMessage.put("ok", false);
			addImg("img/questions.jpg", outMessage);
			outMessage.put("message", "The server is busy with another client");
		} catch (Exception e) {
			outMessage.put("ok", false);
			outMessage.put("systemError", "Image not found.");
		}

		sendmesage(out, outMessage);
	}
	
	public static void comStart() {
		JSONObject outMessage = new JSONObject();
		System.out.println("- Got a start");

		outMessage.put("type", "start");
		try {
			outMessage.put("ok", true);
			addImg("img/hi.png", outMessage);
			outMessage.put("message", "Hello, please enter your name.");
			Random rand = new Random();
			pass = rand.nextInt();
			outMessage.put("pass", pass);
		} catch (Exception e) {
			outMessage.put("ok", false);
			outMessage.put("systemError", "Image not found.");
		}

		if (outMessage.getBoolean("ok")){
			gameState = 1;
		}
		sendmesage(out, outMessage);
	}

	public static void comLogin(JSONObject inMessage) {
		System.out.println("- Got a login");
		JSONObject outMessage = new JSONObject();
		outMessage.put("type", "login");
		loginName = inMessage.getString("submission");
		if (loginName != null && loginName.length() > 0) {
			if (isLettersOrDigits(loginName)) {
				outMessage.put("ok", true);
				if (leaderBoard.has(loginName)) {
					outMessage.put("message", "Welcome back " + loginName + "!");
					outMessage.put("message2", "Type 's' and click submit to see the leader board of high scores.");
					outMessage.put("message3", "Type 'p' and click submit to play the game.");
				} else {
					leaderBoard.put (loginName, 0);
					saveLeaderBoard();
					outMessage.put("message", "Welcome " + loginName + "!");
					outMessage.put("message2", "Type 's' and click submit to see the leader board of high scores.");
					outMessage.put("message3", "Type 'p' and click submit to play the game.");
				}
			} else {
				outMessage.put("ok", false);
				outMessage.put("userError", "Please renter name. Name must consist of only letters or numbers and " +
						"may not have spaces or special characters.");
			}
		} else {
			outMessage.put("ok", false);
			outMessage.put("systemError", "Key 'login' is empty");
		}

		if (outMessage.getBoolean("ok")){
			gameState = 2;
		}
		sendmesage(out, outMessage);
	}

	public static void comLeaderBoard() {
		System.out.println("- Got a LeaderBoard");
		JSONObject outMessage = new JSONObject();
		outMessage.put("type", "leaderBoard");
		outMessage.put("ok", true);
		outMessage.put("object", leaderBoard);
		outMessage.put("message", "Type 'p' and click submit to play the game.");

		if (outMessage.getBoolean("ok")){
			gameState = 3;
		}
		sendmesage(out, outMessage);
	}

	public static void comPlay() {
		createDeck();
		shuffleNumbers(PICTURES_PER_LOCATION);
		currentLocation = locationStack.pop();
		currentPicNum = pictureNumberStack.pop();
		String cityOrCountry = "CITY";
		if (currentLocation.equals("ireland") || currentLocation.equals("switzerland")) {
			cityOrCountry = "COUNTRY";
		}

		System.out.println("Correct answer is: " + currentLocation);
		gameState = 4;

		JSONObject outMessage = new JSONObject();

		outMessage.put("type", "play");
		try {
			outMessage.put("ok", true);
			addImg("img/" + currentLocation + "" + currentPicNum + ".jpg", outMessage);
			outMessage.put("message", "Guess the " + cityOrCountry + ". type 'more' for another hint picture. Type 'next' to skip");
		} catch (Exception e) {
			outMessage.put("ok", false);
			outMessage.put("systemError", "Image not found.");
		}

		sendmesage(out, outMessage);
	}

	public static void comMore() {
		JSONObject outMessage = new JSONObject();
		outMessage.put("type", "more");
		if (pictureNumberStack.size() > 0) {
			currentPicNum = pictureNumberStack.pop();
			String cityOrCountry = "CITY";
			if (currentLocation.equals("ireland") || currentLocation.equals("switzerland")) {
				cityOrCountry = "COUNTRY";
			}
			System.out.println("Client asked for a new picture");
			try {
				outMessage.put("ok", true);
				addImg("img/" + currentLocation + "" + currentPicNum + ".jpg", outMessage);
				if (pictureNumberStack.size() > 0) {
					outMessage.put("message", "Guess the " + cityOrCountry + ". type 'more' for another hint picture. Type 'next' to skip");
				} else {
					outMessage.put("message", "Guess the " + cityOrCountry + ". Type 'next' to skip");
				}
			} catch (Exception e) {
				outMessage.put("ok", false);
				outMessage.put("systemError", "Image not found.");
			}
		} else {
			outMessage.put("ok", false);
			outMessage.put("userError", "There are no more pictures of this location, you must guess or type 'next'.");
		}
		sendmesage(out, outMessage);
	}

	public static void comNext() {
		System.out.println("Client has skipped a location");
		JSONObject outMessage = new JSONObject();
		if (locationStack.size() > 0) {
			pictureNumberStack.clear();
			pictureNumberStack = new Stack<>();
			shuffleNumbers(PICTURES_PER_LOCATION);
			currentLocation = locationStack.pop();
			currentPicNum = pictureNumberStack.pop();
			String cityOrCountry = "CITY";
			if (currentLocation.equals("ireland") || currentLocation.equals("switzerland")) {
				cityOrCountry = "COUNTRY";
			}

			System.out.println("Correct answer is: " + currentLocation);
			outMessage.put("type", "next");
			try {
				outMessage.put("ok", true);
				addImg("img/" + currentLocation + "" + currentPicNum + ".jpg", outMessage);
				outMessage.put("message", "Guess the " + cityOrCountry + ". type 'more' for another hint picture. Type 'next' to skip");
				points -= 4;
				outMessage.put("points", points);
			} catch (Exception e) {
				outMessage.put("ok", false);
				outMessage.put("systemError", "Image not found.");
			}

			sendmesage(out, outMessage);
		} else {
			points -= 4;
			gameOver(outMessage, false);
		}
	}

	public static void comGuess(JSONObject inMessage) {
		String guess = inMessage.getString("submission");
		JSONObject outMessage = new JSONObject();
		if (guess.toUpperCase().equals(currentLocation.toUpperCase())){
			System.out.println("Client guessed correctly!");
			int pointsToAdd = 1;
			if (pictureNumberStack.size() > 0) {
				pointsToAdd = 2 + pictureNumberStack.size();
			}

			if (locationStack.size() > 0) {
				pictureNumberStack.clear();
				pictureNumberStack = new Stack<>();
				shuffleNumbers(PICTURES_PER_LOCATION);
				currentLocation = locationStack.pop();
				currentPicNum = pictureNumberStack.pop();
				String cityOrCountry = "CITY";
				if (currentLocation.equals("ireland") || currentLocation.equals("switzerland")) {
					cityOrCountry = "COUNTRY";
				}

				System.out.println("Correct answer is: " + currentLocation);
				try {
					outMessage.put("type", "guess");
					outMessage.put("ok", true);
					addImg("img/" + currentLocation + "" + currentPicNum + ".jpg", outMessage);
					outMessage.put("message", "Correct!!!");
					outMessage.put("message2", "Guess the " + cityOrCountry + ". type 'more' for another hint picture. Type 'next' to skip");
					points += pointsToAdd;
					outMessage.put("points", points);
				} catch (Exception e) {
					outMessage.put("type", "guess");
					outMessage.put("ok", false);
					outMessage.put("systemError", "Image not found.");
				}

				sendmesage(out, outMessage);
			} else {
				points += pointsToAdd;
				gameOver(outMessage, true);
			}
		} else {
			System.out.println("Client guessed incorrectly.");
			outMessage.put("type", "guess");
			outMessage.put("ok", false);
			outMessage.put("userError", "Nope. wrong answer. try again, or type 'next' to skip");
			sendmesage(out, outMessage);
		}
	}

	public static void gameOver(JSONObject response, boolean lastMoveWasCorrect) {
		response.put("type", "gameOver");
		if (points > 12) {
			saveLeaderBoard();
			try {
				response.put("ok", true);
				addImg("img/win.jpg", response);
				if (lastMoveWasCorrect) {
					response.put("message", "Correct!!!");
				}
				response.put("message2", "You Win!");
				response.put("message3", "Score = " + points);
				response.put("message4", "If you want to play again, please enter you name, or type 'quit'.");
				response.put("points", points);
			} catch (Exception e) {
				response.put("ok", false);
				response.put("systemError", "Image not found.");
			}
		} else {
			try {
				response.put("ok", true);
				addImg("img/lose.jpg", response);
				if (lastMoveWasCorrect) {
					response.put("message", "Correct!!!");
				}
				response.put("message2", "You Lose.");
				response.put("message3", "Score = " + points);
				response.put("message4", "If you want to play again, please enter you name, or type 'quit'.");
				response.put("points", points);
			} catch (Exception e) {
				response.put("ok", false);
				response.put("systemError", "Image not found.");
			}
		}
		sendmesage(out, response);
		currentLocation = "";
		currentPicNum = 0;
		locationStack = new Stack<>();
		pictureNumberStack = new Stack<>();
		points = 0;
		gameState = 1;
	}

	public static boolean isLettersOrDigits(String str) {
		if (str == null || str.length() < 1) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isLetterOrDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static JSONObject addImg(String filename, JSONObject outMessage) throws Exception {
		File file = new File(filename);

		if (file.exists()) {
			String suf = filename.substring(filename.length() - 3);
			BufferedImage img = ImageIO.read(file);
			byte[] bytes = null;
			try (ByteArrayOutputStream outBytes = new ByteArrayOutputStream()) {
				ImageIO.write(img, suf, outBytes);
				bytes = outBytes.toByteArray();
			}

			if (bytes != null) {
				Base64.Encoder encdr = Base64.getEncoder();
				outMessage.put("image", encdr.encodeToString(bytes));
				return outMessage;
			}
		} else {
			System.err.println("Cannot find file: " + file.getAbsolutePath());
			closeConn();
			System.exit(2);
		}
		return outMessage;
	}

	// sends a message to the client
	static void sendmesage(OutputStream outputStream, JSONObject outMessage) {
		try {
			byte[] output = JsonUtils.toByteArray(outMessage);
			NetworkUtils.Send(outputStream, output);
		} catch(Exception e) {e.printStackTrace();}
	}
	// close the connection with the client

	static void saveLeaderBoard() {
		try {
			if (leaderBoard.has(loginName)){
				if (points > leaderBoard.getInt(loginName)) {
					leaderBoard.put(loginName, points);
				}
			}
			FileWriter file = new FileWriter("leaderB.json");
			file.write(leaderBoard.toString());
			file.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	static void loadLeaderBoard() {
		File lb = new File("leaderB.json");
		if (lb.exists()) {
			try {
				String myJson = new Scanner(new File("leaderB.json")).useDelimiter("\\Z").next();
				leaderBoard  = new JSONObject(myJson);
				return;
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		leaderBoard = new JSONObject();
	}

	static void closeConn() {
		try {
			in.close();
			out.close();
			sock.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
