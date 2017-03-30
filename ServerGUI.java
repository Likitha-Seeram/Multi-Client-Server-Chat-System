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
 * A Server GUI
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener {

	private static final long serialVersionUID = 1L;
	private JButton stopStart, client; // Stop and Start Button, Button to create client
	private JTextArea chat, event;  //Text are for chat room and events log
	private JTextField tPortNumber;  //port number
	private Server server; //server
	
	/*
	 * Server Constructor that receives a port to listen for connections.
	 * GUI is created by initializing panels for to insert buttons and
	 * text areas
	 */
	ServerGUI(int port) {
		super("Server");
		server = null;
		
		//North panel for buttons 'Start/Stop' and 'Create Client'
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		client = new JButton("Create Client");
		client.addActionListener(this);
		north.add(client);
		add(north, BorderLayout.NORTH);
		
		//Panels for chat room and events log text areas
		JPanel center = new JPanel(new GridLayout(2, 1));
		chat = new JTextArea(100, 100);
		chat.setEditable(false);
		appendRoom("Chat room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(100, 100);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));
		add(center);

		addWindowListener(this);
		setSize(500, 700);
		setVisible(true);
	}
	
	//To append messages in chat room text area of server
	void appendRoom(String str) {
		chat.append(str);  //to append message to client chat room
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	
	//To append messages in events log text area of server
	void appendEvent(String str) {
		event.append(str);  //to append message to client chat room
		event.setCaretPosition(chat.getText().length() - 1);
	}
	
	/*
	 * This method is used for responding to actions performed
	 * on server GUI (Start/Stop of server and creating client buttons)
	 */
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();  //retrieving the event object
		if (o == client) {
			//if the event is create client, then instance a client GUI by providing server address and port number
			if (server != null)
				new ClientGUI("localhost", 1500);
		} else {
			//if we need to stop server
			if (server != null) {
				server.stop();
				server = null;
				tPortNumber.setEditable(true);
				stopStart.setText("Start");
				return;
			}
			
			//to start server
			int port;
			try {
				port = Integer.parseInt(tPortNumber.getText().trim());
			} catch (Exception er) {
				appendEvent("Invalid port number");
				return;
			}
			
			//creating a new server as a thread
			server = new Server(port, this);
			new ServerRunning().start();
			stopStart.setText("Stop");
			tPortNumber.setEditable(false);
		}
	}
	
	/*
	 * Creating server by specifying port number
	 */
	public static void main(String[] arg) {
		new ServerGUI(1500);
	}
	
	/*
	 * When server crashes upon selecting X button on window,
	 * server connection is closed to free the port
	 */
	public void windowClosing(WindowEvent e) {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception eClose) {
			}
			server = null;
		}
		dispose();
		System.exit(0);
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
	
	/*
	 * A server thread
	 */
	class ServerRunning extends Thread {
		public void run() {
			server.start();  //executes until server stops
			
			//When server stops
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed\n");
			server = null;
		}
	}
}
