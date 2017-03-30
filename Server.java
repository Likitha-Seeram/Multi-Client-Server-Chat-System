/* Name: Likitha Seeram (1001363714)
 * Assignment: CSE 5306 Lab#1
 * References: 
 * 1) A chat application to broadcast messages - http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 * 2) HTTP message formats - http://www.tcpipguide.com/free/t_HTTPResponseMessageFormat.htm, http://www.tcpipguide.com/free/t_HTTPRequestMessageFormat.htm
 * 3) Youtube videos on Client-Server socket programming - https://www.youtube.com/watch?v=vCDrGJWqR8w
 */
package com;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/* 
 * Server class that handles all the server side requests coming from the client.
 * Sockets are used for reading and writing HTTP format messages.
 */
public class Server {
	private static int uniqueId;  //// a unique ID for each connection
	private ArrayList<ClientThread> al;  //Array list for clients who registered with server
	private ServerGUI sg;  //Server GUI
	private SimpleDateFormat sdf;  //to display time
	private int port;  //port for connection
	private boolean keepGoing;  //A boolean variable to keep track of state of server and client

	String HttpResponse, HttpMessage; // String messages used at Input and
									  // Output streams which are in HTTP format

	// Constructors that receives port to listen for connection
	public Server(int port) {
		this(port, null);
	}
	public Server(int port, ServerGUI sg) {
		this.sg = sg;  //GUI
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");  //Format of time to display
		al = new ArrayList<ClientThread>();  //Array List of clients
	}

	/* 
	 * This methods runs until server is in start state i.e., 
	 * until till it stops. It keeps track of clients being
	 * created in an array list.
	 */
	public void start() {
		keepGoing = true;
		try {
			//Creating socket for server
			ServerSocket serverSocket = new ServerSocket(port);
			//Server waits for connections
			while (keepGoing) {
				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();
				//if server stops
				if (!keepGoing)
					break;
				ClientThread t = new ClientThread(socket); //Client thread 
				al.add(t);  //Adding thread to the array list
				t.start();
			}
			try {
				//When server stops
				serverSocket.close();
				for (int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
						//closing socket streams when server closes
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					} catch (IOException ioE) {
					}
				}
			} catch (Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		} catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}

	/*
	 * This method is executed when when the server stops. It goes back to its
	 * initial state (default address and port)
	 */
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		} catch (Exception e) {
		}
	}
	
	/*
	 * Display method is to append events, exceptions or notifications 
	 * like 'Client registered' under Events Log text area
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if (sg == null)
			System.out.println(time);  //If GUI is not present, message is printed on console
		else
			sg.appendEvent(time + "\n");
	}
	
	/*
	 * This method is used to to frame a HTTPResponse at server side when a
	 * client sends connection request or messages to other client. This is 
	 * also used to notify disconnection notification
	 */
	private synchronized void sendPrivateMessage(String user1, String message, String user2, String type) {
		String time = sdf.format(new Date());
		//Looping through the arraylist of clients
		for (int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			boolean check = ct.username.equals(user2);
			//To check if the the client to whom the message is being sent is available
			if (check) {
				HttpResponse = "PUT HTTP/1.1 200 OK type=" + type + "&from=" + user1 + "&to=" + user2 + "&message="
						+ message + "&time=" + time + "&length=" + message.length();
				ct.writeMsg(HttpResponse);  //to write to the required client
			}
		}
	}

	/*
	 * To remove a client from array list when it logs off/dies
	 */
	synchronized void remove(int id) {
		for (int i = 0; i < al.size(); ++i) {
			//Looping through the arraylist
			ClientThread ct = al.get(i);
			if (ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	
	/*
	 * For creating a server instance and starting it
	 */
	public static void main(String[] args) {
		int portNumber = 1500;
		//creating a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}
	
	/*
	 * ClientThread class is used for clients where each client has its own instance
	 * of this thread class. Input and output streams are created.
	 */
	class ClientThread extends Thread {
		Socket socket;  //a socket to listen
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;  //a unique id 
		String username;  //username of client
		String date;  //data of connection
		
		//Constructor
		ClientThread(Socket socket) {
			id = ++uniqueId;
			this.socket = socket;
			try {
				//Creating data streams
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput = new ObjectInputStream(socket.getInputStream());
				
				//Reading the username of client and displaying it in events log
				username = (String) sInput.readObject();
				display(username + " just connected.");
			} catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			} catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}
		
		/*
		 * This method runs until the client is logged in and reacts
		 * to events and messages sent by clients
		 */
		public void run() {
			boolean keepGoing = true;
			while (keepGoing) {
				try {
					// Message is being read from socket input stream
					HttpMessage = (String) sInput.readObject();
				} catch (IOException | ClassNotFoundException e) {
					//This Exception occurs if the client is not available or logs off
					display(username + " Exception reading Streams: " + e);
					String time = sdf.format(new Date());
					//When a client in connection closes by clicking X button of window, notifying the other
					String msg = username + " closed \n";
					for (int i = al.size(); --i >= 0;) {
						ClientThread ct = al.get(i);
						HttpResponse = "PUT HTTP/1.1 200 OK type=close&from=" + username + "&to=" + ct.username
								+ "&message=" + msg + "&time=" + time + "&length=" + msg.length();
						ct.writeMsg(HttpResponse);  //writing to the client
					}
					break;
				}
				//Parsing the HTTP message received
				String data = HttpMessage.substring(HttpMessage.lastIndexOf("?") + 1); //removing header of http message
				
				//Extracting each parameter present in http message
				String type = retrieveParameter(data, "type");
				String from = retrieveParameter(data, "from");
				String to = retrieveParameter(data, "to");
				String msg = retrieveParameter(data, "message");
				String time = retrieveParameter(data, "time");
				String len = retrieveParameter(data, "length");
				
				//Appending the HTTP message in events log
				sg.appendEvent(HttpMessage + "\n");
				if (!msg.isEmpty()) {
					String chats = time + " " + from + ":" + msg + "\n";
					sg.appendRoom(chats);  //appending message to the server chat room
				}
				
				/*
				 * The following conditions checks for type of the message received 
				 * and acts accordingly. 
				 * 'Chat' -- Input: Message received from client, Action: Sending message to connected client
				 * 'logout' -- Client is disconnected from the server
				 * 'Online Users -- A list of clients currently registered with server are sent
				 * 'Connect' -- Input: Connection request, Action: Connection is made for a set of desired clients
				 * 'Disconnect' -- Client disconnects from the chat (or connection made earlier)
				 * 'Busy' -- Client requested for connection is already in connection with another, hence request is rejected
				 */
				
				if (type.equals("chat")) {
					sendPrivateMessage(from, msg, to, type);  //sending messages among connected clients
				}

				if (type.equals("logout")) {
					display(username + " disconnected with a LOGOUT message.");  //displaying in events log
					if (!to.isEmpty()) {
						sendPrivateMessage(from, "User logged off from chat \n", to, type); //sending messages among connected clients
					}
					//when a client logs off, stop the loop that listens for events
					keepGoing = false;
					break;
				}
				if (type.equals("onlineUsers")) {
					//To get list of online users
					for (int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						String info = (i + 1) + ") " + ct.username + " since " + ct.date;
						String timestamp = sdf.format(new Date());
						HttpResponse = "PUT HTTP/1.1 200 OK type=" + type + "&from=server&to=" + ct.username
								+ "&message=" + info + "&time=" + timestamp + "&length=" + info.length();
						writeMsg(HttpResponse);
					}
				}
				if (type.equals("connect")) {
					//If the requested client is logged in, then request is sent to the client.
					//Else request is rejected
					Boolean clientCheck = false;
					for (int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						if (ct.username.equals(to)) {
							clientCheck = true;
							break;
						}
					}
					if (clientCheck) {
						//If request client is available, make the connection request
						msg = "Connection accepted";
						sendPrivateMessage(from, msg, to, type);  //sending messages among connected clients
					} else {
						//If request client is not available, reject the connection request
						msg = "Client not available \n";
						String timestamp = sdf.format(new Date());
						HttpResponse = "PUT HTTP/1.1 200 OK type=disconnect&from=server&to=" + from + "&message=" + msg
								+ "&time=" + timestamp + "&length=" + msg.length();
						writeMsg(HttpResponse);  //writing to client
					}
				}
				if (type.equals("disconnect")) {
					//for disconnecting the connection made among any two clients
					msg = "User disconnected from chat \n";
					sendPrivateMessage(from, msg, to, type);  //sending messages among connected clients
				}

				if (type.equals("busy")) {
					msg = "Requested client is busy \n";
					sendPrivateMessage(from, msg, to, type);  //sending messages among connected clients
				}
			}
			//Remove the client from array list and close it
			remove(id);
			close();
		}
		
		/*
		 * This method is used for retrieving useful parameters 
		 * from the HTTP message received
		 */
		public String retrieveParameter(String data, String field) {

			String parameters[] = data.split("&");  //breaking the message using the delimiter '&'
			for (int i = 0; i < parameters.length; i++) {
				int x = parameters[i].indexOf("=");  //breaking the message using the delimiter '='
				if (parameters[i].substring(0, x).equals(field)) {
					return parameters[i].substring(x + 1);  //returning value of requested field
				}
			}
			return null;
		}
		
		//Closing input and output data streams
		private void close() {
			try {
				if (sOutput != null)
					sOutput.close();
			} catch (Exception e) {
			}
			try {
				if (sInput != null)
					sInput.close();
			} catch (Exception e) {
			}
			;
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
			}
		}
		
		/*
		 * This method is used to write HTTP Response messages
		 * from server to client
		 */
		private boolean writeMsg(String msg) {
			if (!socket.isConnected()) {
				close();
				return false;
			}
			try {
				//write message to the stream
				sOutput.writeObject(msg);
			} catch (IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}
