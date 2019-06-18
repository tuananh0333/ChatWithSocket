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

	JPanel roomLayout;
	JPanel inputLayout;
	JPanel chatLayout;

	String userName;
	String roomName;

	PrintWriter sentToServer;
	static BufferedReader readFromServer;

	DefaultListModel<String> listUsersInRoom;
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
		sentToServer.println(roomName);
		buildChatInterface();
		new MessagesThread().start(); // create thread to listen for messages
	}

	private JButton createJButton(String buttonName) {
		JButton btn = new JButton(buttonName);
		btn.addActionListener(this);
		return btn;
	}

	/**
	 * Add message interface method
	 * 
	 * @param mess:       Frame of a message
	 * @param messageBox: text area, show user's message
	 * @param name:       lable, show user's name
	 */
	private void addMessage(String m, String who) {
		int pos = FlowLayout.LEFT;
		if (who.equals("me")) {
			pos = FlowLayout.RIGHT;
		}

		JPanel mess = new JPanel(new FlowLayout(pos));
		mess.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		mess.setBackground(Color.DARK_GRAY);

		JLabel name = new JLabel(who);
		name.setForeground(Color.WHITE);
		mess.add(name);

		JTextArea messageBox = new JTextArea();
		messageBox.setText(m);
		messageBox.setEditable(false);
		messageBox.setLineWrap(true);
		messageBox.setBackground(Color.WHITE);
		
		mess.add(messageBox);

		if (who.equals("me")) {
			mess.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}

		chatLayout.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
			}

			//Resize when change window's size
			@Override
			public void componentResized(ComponentEvent e) {
				mess.setPreferredSize(new Dimension(mess.getParent().getWidth(), mess.getMinimumSize().height));
				Double width = messageBox.getParent().getWidth() * 0.6;
				messageBox.setPreferredSize(new Dimension(width.intValue(), messageBox.getMinimumSize().height));
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		chatLayout.add(mess);
		
		//Set size when draw a message
		//set messageBox size
		Dimension chatW = chatLayout.getSize();
		Double width = chatW.getWidth() * 0.6;
		messageBox.setPreferredSize(new Dimension(width.intValue(), messageBox.getPreferredSize().height));
		messageBox.setSize(messageBox.getPreferredSize());

		//set message group size
		mess.setPreferredSize(new Dimension(mess.getParent().getWidth(), mess.getMinimumSize().height));
		mess.setSize(mess.getPreferredSize());
		
		int hei = chatLayout.getSize().height+messageBox.getSize().height+5;
		chatLayout.setPreferredSize(new Dimension(chatLayout.getSize().width, hei));
		
		chatLayout.validate();
		chatLayout.repaint();
	}

	/**
	 * Method to build app's interface for better look
	 * 
	 * @param inputLayout: input field, to type message, send message and logout
	 * @param roomLayout:  Panel show list user in chat room
	 * @param chatLayout:  Panel show messages
	 */
	private void buildChatInterface() {
		setLayout(new BorderLayout());
		inputLayout = new JPanel();
		roomLayout = new JPanel();
		chatLayout = new JPanel();
		inputLayout.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		roomLayout.setLayout(new BorderLayout());
		chatLayout.setLayout(new FlowLayout(FlowLayout.LEFT));

		// type
		inputLayout = new JPanel(new FlowLayout());
		txtInput = new JTextField(50);
		btnExit = createJButton("Exit");
		btnSend = createJButton("Send");
		inputLayout.add(txtInput);
		inputLayout.add(btnSend);
		inputLayout.add(btnExit);

		// room
		roomLayout.add(new JLabel("Members in room " + roomName + ": "), "North");
		usersInRoom = new JList<String>(listUsersInRoom); // list view
		roomLayout.add(usersInRoom, "Center");

		// Messages
		chatLayout.setBackground(Color.darkGray);
		
		JScrollPane chatsc = new JScrollPane(chatLayout);
		chatsc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// add into JFrame
		add(inputLayout, "South");
		add(roomLayout, "West");
		getContentPane().add(chatsc, "Center");
		

		setPreferredSize(new Dimension(800, 400));
		
		chatLayout.setPreferredSize(new Dimension(chatsc.getSize().width, chatLayout.getPreferredSize().height));
		
		System.out.println(chatLayout.getPreferredSize());
		System.out.println(chatLayout.getSize());
		
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

					if (line.equals(Constants.NEW_USER_LOGED)) {
						listUsersInRoom.clear();
						int countInRoom = Integer.parseInt(readFromServer.readLine());
						for (int i = 0; i < countInRoom; i++) {
							listUsersInRoom.addElement(readFromServer.readLine());
						}
					} else {
						String[] aStr = line.split(":", 2);
						addMessage(aStr[1], aStr[0]);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	public void sendMessage() {
		sentToServer.println(txtInput.getText());
		addMessage(txtInput.getText(), "me");
		txtInput.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnExit) {
			sentToServer.println(Constants.LOG_OUT); // send end to server so that server knows about the termination
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
					ex.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		login();
	}
}