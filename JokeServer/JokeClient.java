import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.UUID;

/*--------------------------------------------------------
1. Name: Athina Verroiopoulou / Date: 1/22/2017
2. Java version used: 1.8 
3. Precise command-line compilation examples / instructions: e.g.:
> javac JokeClient.java or javac *.java
4. Precise examples / instructions to run this program: e.g.:
In separate shell windows:
> java JokeServer
> java JokeClient
> java JokeClientAdmin
All acceptable commands are displayed on the various consoles.
This runs across machines, in which case you have to pass the IP address of
the server to the clients. For example, if the server is running at 140.192.1.22 then you would type:
> java JokeClient 192.168.0.34 (my IP)
> java JokeClientAdmin 192.168.0.34 (my IP)
5. List of files needed for running the program. e.g.:
 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java
5. Notes: e.g.:works as it is required
----------------------------------------------------------*/

//JokeClient ask for a username and then a user have to press enter in order to request for a joke/proverb
public class JokeClient {
	static final String cookie = UUID.randomUUID().toString(); //generate unique sequences of bytes , want to create a cookie and pass it to server
	public static void main(String args[]) {
		String serverName; 
		if (args.length < 1)  //in case we don't give another IP,input
			serverName = "localhost"; //the default choice is the local host
		else
			serverName = args[0]; //if we give an ip it connects to that one, for my machine the IP is : "192.168.0.34"
		System.out.println("!!! Athina's JokeClient !!!\n"); //print the message
		System.out.println("Server that we try to connect is : " + " " +serverName + ", listening to Port: 4545\n");//print server name
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); //read 
		try {
			String idName;//a string that is used to save the user name provided by the user
			String connect=null; //depending the command will connect to the server, have to be enter
			System.out.println("Please enter your username for identification");
			idName = in.readLine(); //read the user name
			do { //run until quit message or  a word with "quit " inside
				System.out.print("The user : " + " " + idName +" asks for a joke or proverb ," + "Please press enter to confirm or quit to exit: ");
				System.out.flush();//is used to empty the memory from every byte that is written in the buffer
				connect = in.readLine();//reads enter or quit
				if (connect.isEmpty()&& (connect.indexOf("quit") < 0)){ //no quit and enter from user
					getConnection(idName,serverName); //user name and server name as parameter
				}
				else if (connect.equals("quit")){System.out.println("user wants to exit \n");}
				else {System.out.println("Wrong input,Press enter or w");} //in case of invalid output, not enter
				} while (connect.indexOf("quit") < 0); // this will run until the user give an exit message , a word with "quit " inside
			System.out.println("Exit from JokeClient \n"); //print exit message
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

//this method is called from the main to connect with the server
	static void getConnection(String idName,String serverName) {
		String serverResponce; //a string to save server answer 
		try {
			Socket sock = new Socket(serverName, 4545); //connection with server and port 4545
			//The BufferedReader and PrintStream are used to send and receive messages from sockets
			BufferedReader	receiveFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //read
			PrintStream sendToServer = new PrintStream(sock.getOutputStream()); //write
			//The server is waiting for client to ask for a joke or proverb
			sendToServer.println(idName); //send a request to the server with the user name
			sendToServer.flush(); //clean the memory
			sendToServer.println(cookie); //send cookie
			sendToServer.flush(); //clean the memory
	        //server response should be a joke or proverb
				serverResponce = receiveFromServer.readLine(); //the client reads the server message 
				if (serverResponce != null)//if the server answer is not empty
					System.out.println(serverResponce);//print server message
			sock.close(); //we call the close method of Java.net.Socket class to terminate the connection
		} catch (IOException x) {
			System.out.println("Socket error."); //the try catch is used to identify connection errors 
			x.printStackTrace();
		}
	}
}