/*
 * This class will act as a prover
 * It will perform certain tasks to prove it has a key
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;
public class Prover {
	private final int SERVER_PORT = 4321;
	private final BigInteger KEY = new BigInteger("123456789");
	private final String TYPE = "PROVER";
	private final String REQUEST_TO_CHANGE_STATE = "request to change state";
	private final String CLOSE_CONNECTION = "close connection";
	private Socket socket;
	private boolean isConnected;
	private BufferedReader serverIn;
	private PrintStream serverOut;

	public Prover()  {
		isConnected = false;
		while(!isConnected) {
			try {
				connectWithServer();
			} catch(IOException e) {
				System.out.println("Failure to connect with server. Try again");
			}
		}
		System.out.println("Connected to server");
	}

	public void connectWithServer() throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Enter Ip of server(Lock):");
		socket = new Socket(in.nextLine(), SERVER_PORT);

		serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		serverOut = new PrintStream(socket.getOutputStream());
		isConnected = true;

		ReceiveMessage rm = new ReceiveMessage();
		Thread t = new Thread(rm);
		t.start();


		serverOut.println(TYPE);

		in.close();
	}

	public void changeState(String state) {
		serverOut.println(REQUEST_TO_CHANGE_STATE);
		serverOut.println(KEY);
		serverOut.println(state);
	}

	public static void main(String[] args) {
		Prover prover = new Prover();
	}

	public void closeConnection() {
		isConnected = false;
		try {
			serverOut.println(CLOSE_CONNECTION);
			socket.close();
			serverIn.close();
			serverOut.close();
		} catch(IOException e) {
			e.printStackTrace();
		}


	}
	private  class ReceiveMessage implements Runnable{

		@Override
		public void run() {
			try {
				String message;
				while(isConnected) {
					message = serverIn.readLine();
					switch(message) {
					case REQUEST_TO_CHANGE_STATE:
						String state = serverIn.readLine();
						changeState(state);
						break;
					case CLOSE_CONNECTION:
						closeConnection();
						break;
					}
				}
			}catch(IOException e) {
			}
		}

	}
}
