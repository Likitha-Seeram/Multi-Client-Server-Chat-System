/* Name: Likitha Seeram (1001363714)
 * Assignment: CSE 5306 Lab#1
 * References: 
 * 1) A chat application to broadcast messages - http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 * 2) HTTP message formats - http://www.tcpipguide.com/free/t_HTTPResponseMessageFormat.htm, http://www.tcpipguide.com/free/t_HTTPRequestMessageFormat.htm
 * 3) Youtube videos on Client-Server socket programming - https://www.youtube.com/watch?v=vCDrGJWqR8w
 */
package com;

import java.net.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

/*
 * Client class runs as a GUI and is used to send requests/ messages to  
 * the server. It also listens to messages/responses sent by server
 */
public class Client {

	// Data streams at client side
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;

	private SimpleDateFormat sdf;  //for displaying time
	private ClientGUI cg;  //GUI

	private String server, username;  //strings to store sever address and client username
	private int port;  //port number for connecting

	/*
	 * Constructors. 
	 * Client registers by giving the parameters 'server address',
	 * 'port' and 'user name'
	 */
	Client(String server, int port, String username) {
		this(server, port, username, null);
	}

	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;
		sdf = new SimpleDateFormat("HH:mm:ss"); // messages are displayed in client window along with time
	}

	/*
	 * Method for a client to connect with server. If the connection is
	 * accepted, then message is sent to the client. A listener class is started
	 * that responds to messages from server.
	 */
	public boolean start() {
		try {
			//creating socket by giving server and port values
			socket = new Socket(server, port);
		} catch (Exception ec) {
			display("Error connectiong to server:" + ec);  //to display message on client window
			return false;
		}
		
		//when client is connected to server
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);  //to display message on client window
		try {
			//Creating data streams
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO); //to display message on client window
			return false;
		}
		new ListenFromServer().start();  //When client is logged in, creating an instance of listener class to listen to server
		try {
			sOutput.writeObject(username);  //writing client user name to server
		} catch (IOException eIO) {
			display("Exception doing login : " + eIO);  //to display message on client window
			disconnect();
			return false;
		}
		return true;
	}

	/*
	 * To display messages or server notifications on client's chat window
	 */
	private void display(String msg) {
		if (cg == null)
			System.out.println(msg);
		else
			cg.append(msg + "\n");  //to display message on client window
	}

	/*
	 * This method is used to send requests from a client to server through
	 * socket output stream
	 */
	void sendMessage(String user, String message, String type) {
		try {
			String timestamp = sdf.format(new Date()) + "\n";
			int length = message.length();
			//any request from client is formated into this HTTP message format before writing to server
			String HttpRequest = "GET /com/server.java HTTP/1.1?type=" + type + "&from=" + username + "&to=" + user
					+ "&message=" + message + "&time=" + timestamp + "&length=" + length;
			sOutput.writeObject(HttpRequest);  //to write to server
		} catch (IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When a client disconnects by logging off or dies (by clicking close
	 * button), need to close the data streams and then socket
	 */
	private void disconnect() {
		try {
			if (sInput != null)
				sInput.close();
		} catch (Exception e) {
		}
		try {
			if (sOutput != null)
				sOutput.close();
		} catch (Exception e) {
		}
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
		}
		if (cg != null)
			cg.connectionFailed();
	}

	/*
	 * Client is created by taking the default values of server address, port
	 * and user name
	 */
	public static void main(String[] args) {
		//default values
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";
		Client client = new Client(serverAddress, portNumber, userName);  //creating an instance of client
		if (!client.start())
			return;
		client.disconnect();
	}

	/*
	 * This class takes the responses/messages from server. Basing on the type
	 * of message, it acts accordingly by sending messages and actions to the
	 * client GUI
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while (true) {
				try {
					//Reading message from server
					String HttpMessage = (String) sInput.readObject();
					//Parsing the HTTP message
					String data = HttpMessage.substring(HttpMessage.lastIndexOf("OK") + 3);
					//To extract value for all the required parammeters
					String type = retrieveParameter(data, "type");
					String from = retrieveParameter(data, "from");
					String to = retrieveParameter(data, "to");
					String msg = retrieveParameter(data, "message");
					String time = retrieveParameter(data, "time");
					String length = retrieveParameter(data, "length");
					String chats = time + " " + from + ":" + msg + "\n";
					if (cg == null) {
						//when GUI is not present, print to console
						System.out.println(chats);
						System.out.print("> ");
					} else {
						//Basing on the type of message received, do the following
						if (type.equals("connect")) {
							// To make connection with the requested client
							cg.makeConnection(from, msg);

						} else if (type.equals("disconnect") || type.equals("logout") || type.equals("busy")
								|| type.equals("close")) {
							// To disconnect from a connection
							cg.removeConnection(from, msg);
						} else
							cg.append(chats);  //to display message on client window
					}
				} catch (IOException e) {
					//Exception returned when client logs off or when server crashes
					System.out.println(e);
					display("Server has close the connection: " + e);  //to display message on client window
					if (cg != null)
						cg.connectionFailed();
					break;
				} catch (ClassNotFoundException e2) {
				}
			}
		}
		
		/*
		 * This method is used to retrieve required parameters from the 
		 * HTTP message received
		 */
		private String retrieveParameter(String data, String field) {
			String parameters[] = data.split("&");  //breaking the message using the delimiter '&'
			for (int i = 0; i < parameters.length; i++) {
				int x = parameters[i].indexOf("=");  //breaking the message using the delimiter '='
				if (parameters[i].substring(0, x).equals(field)) {
					return parameters[i].substring(x + 1);  //returning value of requested field
				}
			}
			return null;
		}
	}
}
