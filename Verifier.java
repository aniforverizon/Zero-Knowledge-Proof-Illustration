/*
 * Programmer: Andres Carranza
 * Date: 4/7/2019
 * This class will act as the "verifier"
 * It asks a prover to prove that it has a special key
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Verifier {
	private static final int SERVER_PORT = 4321;
	private static final String TYPE = "VERIFIER";
	private static final String REQUEST_TO_CHANGE_STATE = "request to change state";
	private static final String CLOSE_CONNECTION = "close connection";
	private static Socket socket;
	private static boolean isConnected;
	private static BufferedReader serverIn;
	private static PrintStream serverOut;

	public static void connectWithServer() throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Enter Ip of server(Lock):");
		socket = new Socket(in.nextLine(), SERVER_PORT);

		serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		serverOut = new PrintStream(socket.getOutputStream());
		isConnected = true;

		serverOut.println(TYPE);
		in.close();
	}


	//gets state from server
	public static String getState() {
		String state;
		try {
			state = serverIn.readLine();
		} catch(IOException e) {
			return null;

		}
		return state;
	}

	//Asks server to ask the prover to change the Lock's state
	public static void changeState(String newState) {

		serverOut.println(REQUEST_TO_CHANGE_STATE);
		serverOut.println(newState);



	}

	public static void closeConnection() {
		serverOut.println(CLOSE_CONNECTION);

		try {
			socket.close();
			serverIn.close();
			serverOut.close();
		} catch(IOException e) {
			e.printStackTrace();
		}


	}

	public static void main(String[] args){

		System.out.println("\n*****************************************************************************");
		System.out.println("To verify that the prover has the key to the lock of the Lock class");
		System.out.println("this class will ask the Prover to perform a number of tasks.");
		System.out.println("The class Lock has a boolean field named state. This state can be either");
		System.out.println("true or false. To change this state one must provide the correct key (In");
		System.out.println("the form of a BigInteger). This class will ask the Prover to change the ");
		System.out.println("state of the lock class to either true or false. For example, if we ask the");
		System.out.println("Prover to change the state to true, but the state of the class remains false,");
		System.out.println("then we know the Prover does not have the key. However, if the key changes to ");
		System.out.println("true, then we know the Prover has the key because without it he would not be ");
		System.out.println("able to change it. ");
		System.out.println("*****************************************************************************\n\n");

		isConnected = false;
		while(!isConnected) {
			try {
				connectWithServer();
			} catch(IOException e) {
				System.out.println("Failure to connect with server. Try again");
			}

		}

		String state = "false";
		System.out.println("\n\nThe current value for state is: "+ state  );
		System.out.println("Requesting prover to change state to true");
		changeState("true");
		state = getState();
		System.out.println("New value of state is: " + state);

		System.out.println("\n\nThe current value for state is: "+ state  );
		System.out.println("Requesting prover to change state to false");
		changeState("false");
		state = getState();
		System.out.println("New value of state is: " + state);

		System.out.println("\n\nThe current value for state is: "+ state  );
		System.out.println("Requesting prover to change state to true");
		changeState("true");
		state = getState();
		System.out.println("New value of state is: " + state);

		System.out.println("\nSuccess ");
		System.out.println("The prover poses the key.");
		closeConnection();
	}	

}