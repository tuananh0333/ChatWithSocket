package chat;

import java.io.*;
import java.util.*;
import java.net.*;

public class ChatServer {
	public static String LOG_OUT = "exit";
	public static String HAVE_LOGED_IN = "loged";
	public static String NEW_USER_LOGED = "new";

	List<HandleClient> clients;
	private ServerSocket server;

	public ChatServer() {
		clients = new ArrayList<HandleClient>();
	}
	
	public void updateUsersInRoom(HandleClient client) {
		int countInRoom = 0;
		for (HandleClient c : clients) {
			if (c.getRoomName().equals(client.getRoomName())) {
				countInRoom++;
			}
		}
		for (HandleClient c : clients) {
			if (c.getRoomName().equals(client.getRoomName())) {
				c.sendAlert(NEW_USER_LOGED);
				c.sendAlert(Integer.toString(countInRoom));
				for(HandleClient hc : clients) {
					if (hc.getRoomName().equals(client.getRoomName())) {
						c.sendAlert(hc.getUserName());
					}
				}
			}
		}
	}

	public void process() throws Exception {
		server = new ServerSocket(9999, 10);
		System.out.println("Server Started...");
		while (true) {
			Socket client = server.accept();
			HandleClient newC = new HandleClient(client);
			clients.add(newC);
			updateUsersInRoom(newC);
		}
	}

	public static void main(String[] args) throws Exception {
		new ChatServer().process();
	}

	public void broadcast(String user, String room, String message) {
		// send message to all connected users
		for (HandleClient c : clients) {
			if (c.getRoomName().equals(room)) {
				if (!c.getUserName().equals(user)) {
					c.sendMessage(user, message);
				}
			}
		}
	}

	/**
	 * Inner class to handle each client
	 */
	class HandleClient extends Thread {
		String name = "";
		String room = "";

		BufferedReader input;
		PrintWriter output;

		public HandleClient(Socket client) throws Exception {
			// get input and output streams
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			output = new PrintWriter(client.getOutputStream(), true);

			// read name and room
			name = input.readLine();
			room = input.readLine();
			start();
		}

		public void sendMessage(String uname, String msg) {
			output.println(uname + ": " + msg);
		}
		
		public void sendAlert(String alert) {
			output.println(alert);
		}

		public String getUserName() {
			return name;
		}

		public String getRoomName() {
			return room;
		}

		public void run() {
			String line;
			try {
				while (true) {
					line = input.readLine();
					if (line.equals(LOG_OUT)) {
						clients.remove(this);
						updateUsersInRoom(this);
						break;
					}
					broadcast(name, room, line);//-send messages to all
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
}