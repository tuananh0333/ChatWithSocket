package chat;

import java.io.*;
import java.net.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class ChatClient extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String userName;
	String roomName;

	PrintWriter sentToServer;
	static BufferedReader readFromServer;

	DefaultListModel<String> listUsersInRoom;
	JTextArea txtaMessages;
	JTextField txtInput;
	JList<String> usersInRoom;

	JButton btnSend, btnExit;
	Socket client;

	public ChatClient(String userName, String roomName, String servername) throws Exception {
		super(userName); // set title for frame
		this.userName = userName;
		this.roomName = roomName;

		client = new Socket(servername, 9999);
		readFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
		sentToServer = new PrintWriter(client.getOutputStream(), true);

		sentToServer.println(userName); // send name to server
		listUsersInRoom = new DefaultListModel<String>();
//		try {
//			if(readFromServer.ready()) {
//				if (readFromServer.readLine().equals(ChatServer.HAVE_LOGED_IN)) {
//					JOptionPane.showMessageDialog(rootPane, "This account is already loged in");
//					login();
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		sentToServer.println(roomName);
		buildChatInterface();
		new MessagesThread().start(); // create thread to listen for messages
	}

	/**
	 * Build Java form for better experience
	 */
	public void buildChatInterface() {
		//Left
		JPanel listUserField = new JPanel();
		listUserField.add(Box.createVerticalStrut(20));
		listUserField.setLayout(new BoxLayout(listUserField, BoxLayout.Y_AXIS));
		
		JLabel lblText = new JLabel("Users in room "+ roomName+": "); //lable
		lblText.setAlignmentX(RIGHT_ALIGNMENT);
		listUserField.add(lblText);
		
		listUserField.add(Box.createVerticalStrut(10)); //space
		usersInRoom = new JList<String>(listUsersInRoom); //list view
		usersInRoom.setAlignmentX(RIGHT_ALIGNMENT);
		listUserField.add(usersInRoom);
		
		listUserField.add(Box.createHorizontalStrut(50));
		add(listUserField, "West"); //add to Panel
		
		//Right
		btnSend = new JButton("Send");
		btnExit = new JButton("Exit");

		txtaMessages = new JTextArea();
		txtaMessages.setRows(20);
		txtaMessages.setEditable(false);
		txtInput = new JTextField(50);
		add(txtaMessages, "Center");

		JPanel bp = new JPanel(new FlowLayout());
		bp.add(txtInput);
		bp.add(btnSend);
		bp.add(btnExit);
		add(bp, "North"); 	
		
		btnSend.addActionListener(this);
		btnExit.addActionListener(this);
		setSize(500, 300);
		setVisible(true);
		setLocationRelativeTo(null);
		pack();
	}

	// inner class for Send Message to Server
	class MessagesThread extends Thread {
		@Override
		public void run() {
			String line;
			try {
				while (true) {
					line = readFromServer.readLine();
					
					if(line.equals(ChatServer.NEW_USER_LOGED)) {
						listUsersInRoom.clear();
						int countInRoom = Integer.parseInt(readFromServer.readLine());
						for (int i = 0; i < countInRoom; i++) {
							listUsersInRoom.addElement(readFromServer.readLine());
						}
					} else {
						txtaMessages.append(line + "\n");
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	
	// inner class for always listen from Server
	class ListenFromServer extends Thread {
		@Override
		public void run() {
//			String line;
			try {
				while (true) {
					
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public void sendMessage() {
		sentToServer.println(txtInput.getText());
		txtaMessages.append("me: " + txtInput.getText() + "\n");
		txtInput.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnExit) {
			sentToServer.println(ChatServer.LOG_OUT); // send end to server so that server knows about the termination
			System.exit(0);
		} else {
			// send message to server
			sendMessage();
		}
	}

	public static void login() {
		String name = "";
		String room = "";

		JTextField nameField = new JTextField(5);
		JTextField roomField = new JTextField(5);

		JPanel loginPanel = new JPanel();
		loginPanel.add(new JLabel("Enter your name: "));
		loginPanel.add(nameField);
		loginPanel.add(Box.createHorizontalStrut(15)); // a spacer
		loginPanel.add(new JLabel("Enter room id: "));
		loginPanel.add(roomField);

		int result = JOptionPane.showConfirmDialog(null, loginPanel, "Please enter your name and room id",
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			if (nameField.getText().isEmpty() || roomField.getText().isEmpty()) {
				JOptionPane.showMessageDialog(loginPanel, "You must enter your name and room id");
				login();
			} else {
				name = nameField.getText();
				room = roomField.getText();

				String servername = "localhost";
				try {
					new ChatClient(name, room, servername);
				} catch (Exception ex) {
					System.out.println("Error --> " + ex.getMessage());
				}
			}
		}
	}

	public static void main(String[] args) {
		login();
	}
}