/* Name: Likitha Seeram (1001363714)
 * Assignment: CSE 5306 Lab#1
 * References: 
 * 1) A chat application to broadcast messages - http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 * 2) HTTP message formats - http://www.tcpipguide.com/free/t_HTTPResponseMessageFormat.htm, http://www.tcpipguide.com/free/t_HTTPRequestMessageFormat.htm
 * 3) Youtube videos on Client-Server socket programming - https://www.youtube.com/watch?v=vCDrGJWqR8w
 */
package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * A client GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel label;  //To hold 'Enter UserName' and 'Enter Message' labels
	private JTextField tf;  //To hold UserName and message values
	private JTextField tfServer, tfPort, tfuser; //Text fields to hold server address, port and connection request user name
	private JButton login, logout, onlineUsers, send, connect, disconnect;  //Buttons
	private JTextArea ta;  //Chat Room
	private Client client;  //Client object
	private int defaultPort;  //To hold default port value
	private String defaultHost;  //To hold default server address

	String username;  //To hold client's username
	
	//Constructor that takes server address and port name
	ClientGUI(String host, int port) {

		super("Client");
		defaultPort = port;
		defaultHost = host;
		
		//North panel containing server address, port, connection client, label and textfield to enter user name and message
		JPanel northPanel = new JPanel(new GridLayout(3, 1));
		JPanel serverAndPort = new JPanel(new GridLayout(1, 6, 1, 2));
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfuser = new JTextField();

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel("Connect with: "));
		serverAndPort.add(tfuser);
		tfuser.setEditable(false);
		northPanel.add(serverAndPort);

		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);
		
		//Chat Room Panel
		ta = new JTextArea("Welcome to the Chat room\n", 70, 70);
		JPanel centerPanel = new JPanel(new GridLayout(1, 1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);
		
		/*
		 * South Panel containing 6 Buttons.Login, Logout, Online Users(to check who are 
		 * registered currently), connect (to connect with a available user), send message, 
		 * disconnect (disconnecting from chat)
		 */
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);
		onlineUsers = new JButton("Clients Online");
		onlineUsers.addActionListener(this);
		onlineUsers.setEnabled(false);
		connect = new JButton("Connect");
		connect.addActionListener(this);
		connect.setEnabled(false);
		send = new JButton("Send Message");
		send.addActionListener(this);
		send.setEnabled(false);
		disconnect = new JButton("Disconnect");
		disconnect.addActionListener(this);
		disconnect.setEnabled(false);

		JPanel southPanel = new JPanel(new GridLayout(2, 3));
		JPanel actionBar1 = new JPanel(new GridLayout(1, 3, 1, 2));
		actionBar1.add(login);
		actionBar1.add(logout);
		actionBar1.add(onlineUsers);
		JPanel actionBar2 = new JPanel(new GridLayout(1, 3, 1, 2));
		actionBar2.add(connect);
		actionBar2.add(send);
		actionBar2.add(disconnect);
		southPanel.add(actionBar1);
		southPanel.add(actionBar2);

		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(650, 650);
		setVisible(true);
		tf.requestFocus();
	}
	
	//To append messages to the chat room
	void append(String str) {
		ta.append(str);  //appending messages to text area
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	
	/*
	 * Resetting text fields and buttons to their original state when 
	 * client gets disconnected from server
	 */
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		onlineUsers.setEnabled(false);
		label.setText("Enter your username below");
		tf.setText("Anonymous");
		tf.setEditable(true);
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		tfServer.setEditable(true);
		tfPort.setEditable(true);
		tfuser.setEditable(false);
		connect.setEnabled(false);
		send.setEnabled(false);
		disconnect.setEnabled(false);
		tf.removeActionListener(this);
	}

	/*
	 * Any action performed on client GUI is captured and processed 
	 * in this method. Client takes these from GUI and sends to server
	 */
	public void actionPerformed(ActionEvent e) {
		String type;
		Object o = e.getSource(); //Extracting action object
		//when the event is logout
		if (o == logout) {
			client.sendMessage(tfuser.getText(), "", "logout");
			tfuser.setText("");
			return;
		}
		//when the event is to check online users
		if (o == onlineUsers) {
			client.sendMessage("", "", "onlineUsers");
			return;
		}
		//when the event is to send message
		if (o == send) {
			if (tfuser.getText().isEmpty() || tf.getText().isEmpty()) {
				return;
			} else {
				append("  Me:" + tf.getText() + "\n");
				client.sendMessage(tfuser.getText(), tf.getText(), "chat");
				tf.setText("");
				tfuser.setEditable(false);
				disconnect.setEnabled(true);
				return;
			}
		}
		//when the event is to make a connection request
		if (o == connect) {
			if (tfuser.getText().isEmpty()) {
				return;
			} else {
				client.sendMessage(tfuser.getText(), "", "connect");
				String s = tfuser.getText();
				append("Connecting with the user " + s + "\n");  //appending messages to text area
				tf.setEditable(true);
				tfuser.setEditable(false);
				connect.setEnabled(false);
				send.setEnabled(true);
				disconnect.setEnabled(true);
			}
		}
		//when the event is close the connection
		if (o == disconnect) {
			if (tfuser.getText().isEmpty()) {
				return;
			} else {
				client.sendMessage(tfuser.getText(), "", "disconnect");
				String s = tfuser.getText();
				append("Disconnecting from the chat with " + s + "\n");  //appending messages to text area
				tfuser.setEditable(true);
				tfuser.setText("");
				tf.setEditable(false);
				connect.setEnabled(true);
				send.setEnabled(false);
				disconnect.setEnabled(false);
				return;
			}
		}
		//when the event is login
		if (o == login) {
			username = tf.getText().trim();
			if (username.length() == 0)
				return;
			String server = tfServer.getText().trim();
			if (server.length() == 0)
				return;
			String portNumber = tfPort.getText().trim();
			if (portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			} catch (Exception en) {
				return;
			}
			client = new Client(server, port, username, this);
			if (!client.start())
				return;
			tf.setText("");
			tf.setEditable(false);
			tfuser.setText("");
			label.setText("Enter your message below");
			login.setEnabled(false);
			logout.setEnabled(true);
			onlineUsers.setEnabled(true);
			connect.setEnabled(true);
			send.setEnabled(false);
			disconnect.setEnabled(false);
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			tfuser.setEditable(true);
			tf.addActionListener(this);
			append("with UserName:" + username + "\n \n");
		}
	}
	
	/*
	 * Client GUI is initiated by by providing
	 * server address and port number
	 */
	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}

	/*
	 * This method is used to make connection with another client
	 */
	public void makeConnection(String user, String chats) {
		if (tfuser.getText().isEmpty()) {
			tfuser.setText(user);
			tf.setEditable(true);
			tfuser.setEditable(false);
			connect.setEnabled(false);
			send.setEnabled(true);
			disconnect.setEnabled(true);
			append(chats + " with " + user + "\n");
		} else {
			client.sendMessage(user, "", "busy");
		}
	}

	/*
	 * This method is used for disconnecting a client from its
	 * connection
	 */
	public void removeConnection(String from, String chats) {
		if (tfuser.getText().equals(from) || from.equals("server")) {
			tfuser.setEditable(true);
			tfuser.setText("");
			tf.setEditable(false);
			send.setEnabled(false);
			connect.setEnabled(true);
			disconnect.setEnabled(false);
			append(chats);
		}
	}
}
