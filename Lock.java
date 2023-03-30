/*
 * This class will act as a "lock"
 * If one has the key to the lock (in this case a Big Integer), then one can access the method setState()
 * setState() controls the class' field state
 * state is a boolean
 * This class is also a server
 * Connects to Verifier and Prover
 */
import java.net.Socket;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;


public class Lock {
	private final int SERVER_PORT = 4321;
	private final BigInteger KEY = new BigInteger("123456789");
	private final String PROVER = "PROVER";
	private final String VERIFIER = "VERIFIER";
	private final String REQUEST_TO_CHANGE_STATE = "request to change state";
	private final String CLOSE_CONNECTION = "close connection";
	private ServerSocket serverSocket;
	private HashMap<String, ServerThread> serverThreads;
	private boolean state;

	public Lock(){
		state = false;
		serverThreads = new HashMap<String, ServerThread>();
		try {
			System.out.println("Ip address: " + InetAddress.getLocalHost().getHostAddress());
			serverSocket = new ServerSocket(SERVER_PORT);
			for(int i = 0; i < 2; i++) {
				ServerThread serverThread = new ServerThread(serverSocket.accept(), this);
				Thread t = new Thread(serverThread);
				t.start();
			}
		} catch (IOException e) {}
	}

	//Returns the current state
	public boolean getState() {
		return state;
	}

	public void closeConnection() {
		serverThreads.get(PROVER).closeConnection();
	}
	
	//Only allows access to state if the correct key is provided
	public void setState(BigInteger key, String newState) {
		if(this.KEY.equals(key)) {
			if(newState.equals("true"))
				state = true;
			else
				state = false;
		}
		if(state) {
			serverThreads.get(VERIFIER).stateChanged("true");
		}
		else {
			serverThreads.get(VERIFIER).stateChanged("false");
		}


	}

	public void changeState(String state) {
		serverThreads.get(PROVER).changeState(state);
	}

	public static void main(String[] args) {
		Lock lock = new Lock();
	}

	private class ServerThread implements Runnable{
		private Socket client;
		private BufferedReader clientIn;
		private PrintStream clientOut;
		private String type = null;
		private Lock lock;

		public ServerThread(Socket socket, Lock l) throws IOException{
			this.client = socket;
			lock = l;
			clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			clientOut = new PrintStream(client.getOutputStream());

			type = clientIn.readLine();

			serverThreads.put(type, this);
		}

		public void run(){
			try {
				String messageReceived;
				while(true) {
					messageReceived =  clientIn.readLine();
					if(type.equals(PROVER)) {
						switch(messageReceived) {
						case REQUEST_TO_CHANGE_STATE:
							String key = clientIn.readLine();
							String state = clientIn.readLine();
							setState(new BigInteger(key), state);
							break; 
						}
					}
					else if(type.equals(VERIFIER)) {
						switch(messageReceived) {
						case REQUEST_TO_CHANGE_STATE:
							String state = clientIn.readLine();
							lock.changeState(state);
							break;
						case CLOSE_CONNECTION:
							lock.closeConnection();
						}
					}
					if(messageReceived.equals(CLOSE_CONNECTION))
						break;
				}
			} catch(IOException e) {}
		}

		public void changeState(String newState) {
			clientOut.println(REQUEST_TO_CHANGE_STATE);
			clientOut.println(newState);
		}

		public void stateChanged(String newState) {
			clientOut.println(newState);
		}
		
		public void closeConnection() {
			clientOut.println(CLOSE_CONNECTION);
		}
	}
}
