
/*--------------------------------------------------------
0. From John Reagan host server. 
1. Name: Athina Verroiopoulou / Date: 1/22/2017
2. Java version used: 1.8 
3. Precise command-line compilation examples / instructions: e.g.:
> javac HostServer.java
4. Precise examples / instructions to run this program:
0. Start the HostServer in some shell. >> java HostServer
1. start a web browser and point it to http://localhost:1565. Enter some text and press
the submit button to simulate a state-maintained conversation.
2. start a second web browser, also pointed to http://localhost:1565 and do the same. Note
that the two agents do not interfere with one another.
3. To suggest to an agent that it migrate, enter the string "migrate"
in the text box and submit. The agent will migrate to a new port, but keep its old state.
4.During migration, stop at each step and view the source of the web page to see how the
server informs the client where it will be going in this stateless environment.
5. List of files needed for running the program.
 HostServer.java
6. Notes:
DESIGN OVERVIEW
Here is the high-level design, more or less:
HOST SERVER
  Runs on some machine
  Port counter is just a global integer incrememented after each assignment
  Loop:
    Accept connection with a request for hosting
    Spawn an Agent Looper/Listener with the new, unique, port

AGENT LOOPER/LISTENER
  Make an initial state, or accept an existing state if this is a migration
  Get an available port from this host server
  Set the port number back to the client which now knows IP address and port of its
         new home.
  Loop:
    Accept connections from web client(s)
    Spawn an agent worker, and pass it the state and the parent socket blocked in this loop
  
AGENT WORKER
  If normal interaction, just update the state, and pretend to play the animal game
  (Migration should be decided autonomously by the agent, but we instigate it here with client)
  If Migration:
    Select a new host
    Send server a request for hosting, along with its state
    Get back a new port where it is now already living in its next incarnation
    Send HTML FORM to web client pointing to the new host/port.
    Wake up and kill the Parent AgentLooper/Listener by closing the socket
    Die

WEB CLIENT
  Just a standard web browser pointing to http://localhost:1565 to start.
  
 A framework that can host agents and migrate them to different server/port. 
While Server is always on localhost, HostListener port of 1565, can work the same on other / different hosts.
 The state is kept as integer and increments when needed.
----------------------------------------------------------*/

/*
HostServer:
Can only run with 3 browsers 
We send a message from a browser to the hosted agent that is waiting for connections.
When we connect we have to write a message that is either our name or migration request.
The value pairs will be submitted on localhost with port 3001. 
When you submit the state will be increased and keep increasing with every submission.
We do the same with another browser, that is in state 1 and send to port 3002. Their state will be kept.
We choose to migrate and that go to port 3003 but it keeps the state from previous conversation.
So my program can move to a different location and continue to communicate with the same web browser. 
This is valuable to keep a history for every client. We deal with a single host server only.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

//manage requests made at different active ports in agentlistener
//the request is passed to AgentWorker by an html parameter 
//if migrate string showed up switch the client to the next available port

class AgentWorker extends Thread { //created by agentListener
	
	Socket sock; //initialize socket for client connection
	agentHolder parentAgentHolder; //maintains agentstate holding socket and state counter
	int localPort; //initialize port for the particular request
	AgentWorker (Socket s, int prt, agentHolder ah) { //constructor with the above parameters 
		sock = s;
		localPort = prt;
		parentAgentHolder = ah;
	}
	public void run() {
		PrintStream out = null; //initialize PrintStream
		BufferedReader in = null;//initialize BufferedReader
		String NewHost = "localhost"; //initialize port
		int NewHostMainPort = 1565;		 //initialize port for Agent worker
		String buf = ""; //initialize read sting value
		int newPort;
		Socket clientSock;
		BufferedReader fromHostServer;
		PrintStream toHostServer;
		try {
			out = new PrintStream(sock.getOutputStream());//write
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));//read
			String inLine = in.readLine();//read client message
			//the following is used in order to be able to use different browsers 
			StringBuilder htmlString = new StringBuilder();//create StringBuilder object
			System.out.println();
			System.out.println("Request line: " + inLine);//print the message that client send
			if(inLine.indexOf("migrate") > -1) {//if migrate message is found
				//the supplied request contains migrate, switch the user to a new port
				//in order to switch user to a new port create new socket assigned with host server (main)
				clientSock = new Socket(NewHost, NewHostMainPort);// 1565
				fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));//read
				toHostServer = new PrintStream(clientSock.getOutputStream());//write
				//send request to host server main for the next open port
				toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");//send to host server
				toHostServer.flush();//clean buffer	
				for(;;) { 
					buf = fromHostServer.readLine();//read the host server response, a port
					if(buf.indexOf("[Port=") > -1) { //find if that port is valid,if not repeat steps
						break;//if valid exit for
					}
				}
				//use substring to extract into tempbuf the desired port 
				String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
				newPort = Integer.parseInt(tempbuf); //make the string integer and save it into newPort
				System.out.println("newPort is: " + newPort);//print the new port with a number (int) format
				//use append for the messages that we will send to the user
				htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine));//send html header to the user
				htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n");//send received migration message with port
				htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
				htmlString.append(AgentListener.sendHTMLsubmit());//send html finish form
				System.out.println("Killing parent listening loop.");//print message for terminate the waiting server port
				ServerSocket ss = parentAgentHolder.sock; //go back to the old port by using the parentAgentHolder object from agentHolder class
				ss.close();//port is closing
			} else if(inLine.indexOf("person") > -1) {//if the client message is not migrate , but a person variable
				parentAgentHolder.agentState++;//add one to the state, the game is on 
				//use AgentListener to display the state into the browser
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));//send the equivalent header
				htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
				htmlString.append(AgentListener.sendHTMLsubmit());//send finish header
			} else {
				//maybe a case of fav.ico request as its not a person or migrate, invalid input
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));//send the equivalent header
				htmlString.append("You have not entered a valid request!\n");
				htmlString.append(AgentListener.sendHTMLsubmit());		//send finish header
			}
			 //call sendHTMLtoStream method from AgentListener class to output html , using append
			AgentListener.sendHTMLtoStream(htmlString.toString(), out);
			sock.close();//we call the close method of Java.net.Socket class to terminate the connection with the specific client, not the server
		} catch (IOException ioe) {//in case of error
			System.out.println(ioe);
		}
	}
}

class agentHolder { //class that stores the agent state information / used for passing the state thought the different ports
	ServerSocket sock;
	int agentState; //store agent state variable	
	agentHolder(ServerSocket s) { sock = s;} //constructor deal with socket objects that are active
}

//activate the listener from HostServer, is used for handling different ports and requests
//a web browser sends requests to the listener and listener is responsible to handle them
class AgentListener extends Thread {	
	Socket sock; //initialize socket
	int localPort;	 //initialize port
	AgentListener(Socket As, int prt) { //constructor with socket and local port parameters
		sock = As;
		localPort = prt;
	}	
	int agentState = 0; //used in agentHolder class , agent state is initialized to 0
	
	//called from start() when a request is made on the listening port
	public void run() { 
		BufferedReader in = null; //initialize BufferedReader
		PrintStream out = null; //initialize PrintStream
		String NewHost = "localhost"; //initialize NewHost with localhost value
		System.out.println("In AgentListener Thread");		//print message that we are inside AgentListener Thread 
		try {
			String buf;
			out = new PrintStream(sock.getOutputStream()); //write
			in =  new BufferedReader(new InputStreamReader(sock.getInputStream())); //BufferedReader connection with socket / read
			buf = in.readLine(); //read what the browser have send, a state in a string form
			//if buf contains a value (state) continue with the request and save it into  agentHolder class
			if(buf != null && buf.indexOf("[State=") > -1) { //buf should not be empty and greater than -1
				// use the substring to extract the state from the given string
				String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State=")));
				agentState = Integer.parseInt(tempbuf); //save to variable agentState  the integer value of the string state 
				System.out.println("agentState is: " + agentState); //print state as number
			}
			System.out.println(buf); //print state			
			StringBuilder htmlResponse = new StringBuilder(); //create a StringBuilder object that can hold html response 
						//response to the browser using append method
			htmlResponse.append(sendHTMLheader(localPort, NewHost, buf)); //send the header
			htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");//show message
			htmlResponse.append("[Port="+localPort+"]<br/>\n");//show the connected port
			htmlResponse.append(sendHTMLsubmit());//html form is send 			
			sendHTMLtoStream(htmlResponse.toString(), out); //display everything
			ServerSocket servsock = new ServerSocket(localPort,2); //open a new connection 
			//create a new object of the class agentHolder to store the new socket connection and new agent state
			agentHolder agenthold = new agentHolder(servsock);
			agenthold.agentState = agentState;//assign the new state value to the created object
			
			//wait for connections.
			while(true) { //while there is a connection
				sock = servsock.accept();//start request				
				System.out.println("Got a connection to agent at port " + localPort);//message with port
				//start agent worker with parameters the sock connection,port and the new agentState
				new AgentWorker(sock, localPort, agenthold).start(); 
			}
		
		} catch(IOException ioe) { //in case of error
			System.out.println("Either connection failed, or just killed listener loop for agent at port " + localPort);
			System.out.println(ioe);
		}
	}

	static String sendHTMLheader(int localPort, String NewHost, String inLine) {
		StringBuilder htmlString = new StringBuilder();//create StringBuilder object
		//send the following html header
		htmlString.append("<html><head> </head><body>\n");
		htmlString.append("<h2>This is for submission to PORT " + localPort + " on " + NewHost + "</h2>\n");//port and host
		htmlString.append("<h3>You sent: "+ inLine + "</h3>");//the string that we send
		htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + localPort + "\">\n");
		htmlString.append("Enter text or <i>migrate</i>:");
		htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n");
		return htmlString.toString();//use to display append
	}
	static String sendHTMLsubmit() { //html form method with what to submit when we finish html
		return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
	}
	
	static void sendHTMLtoStream(String html, PrintStream out) { //method called from Agent listener with sendHTMLtoStream(htmlResponse.toString(), out);
		//html MIME header
		out.println("HTTP/1.1 200 OK");
		out.println("Content-Length: " + html.length()); //give the content length
		out.println("Content-Type: text/html");
		out.println("");		
		out.println(html);
	}
}
//the host server accepts new connection and migrate requests at port 1565
//after that we find another available port (hard coded for 3000) and assign to the Next port the request by starting the AgentListener
//AgentListener is responsible for all the work, but the important is that it should start a unique port each time
//for that reason we increase the NextPort by one
//server connection, main class
public class HostServer {
	public static int NextPort = 3000; //available port , used for finding another port from the one we listen new requests	
	public static void main(String[] a) throws IOException {
		//initial connection in port 1565, this is where it listens for new or migrate requests
		int q_len = 6; //number of client requests 
		int port = 1565;
		Socket sock;		
		ServerSocket servsock = new ServerSocket(port, q_len); //the socket take as parameter the port and request number and we create an object
		System.out.println("Athinas DIA Master receiver started at port 1565.");//print message to show the started port
		System.out.println("Connect from 1 to 3 browsers using \"http:\\\\localhost:1565\"\n"); //print message to show the localhost connection
		while(true) {		
			NextPort = NextPort + 1; //increment next port when we have a request			
			sock = servsock.accept();//start request		
			System.out.println("Starting AgentListener at port " + NextPort); //print message with the port index		
			new AgentListener(sock, NextPort).start(); //start a listener that points this new port and receives requests
		}		
	}
}