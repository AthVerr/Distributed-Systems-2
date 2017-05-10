import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/*--------------------------------------------------------
1. Name: Athina Verroiopoulou / Date: 1/22/2017
2. Java version used: 1.8 
3. Precise command-line compilation examples / instructions: e.g.:
> javac JokeClientAdmin.java or javac *.java
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
5. Notes: e.g.:
Runs according to the requirements. Have to press pro for proverb or j for joke or  word with "quit " inside to exit
anything else is consider invalid input.
Consider to send one more selection for exit to terminate the server, 
so the server could have one more switch case choice, but did not work out as expected
----------------------------------------------------------*/

// JokeClientAdmin sets the server in different modes (joke/proverb)
public class JokeClientAdmin {
	public static void main(String args[]) {
		String serverName; 
		if (args.length < 1) //in case we don't give another IP,input
			serverName = "localhost"; //the default choice is the local host
		else
			serverName = args[0];//if we give an ip it connects to that one
		System.out.println("!!! Athina's JokeClientAdmin !!!\n");
		System.out.println("Server that we try to connect is : " + " " +serverName + ", listening to Port: 5050 \n");//print the given server name we try to connect
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));//read from Socket
		try {
			String mode;//a string that is used to save the user command mode
			do {//run until quit message or a word with "quit " inside
				//give the choices for different mode,the user have to press "pro" or "j" or "quit"
				System.out.println("Please enter the mode u want: "+
				"Select pro: for proverb mode or j: for joke mode or quit to exit the adminClient \n");
				System.out.flush();//is used to empty the memory from every byte that is written in the buffer
				mode = in.readLine(); //read the mode choice from the buffer
				if ((mode.indexOf("quit") < 0)&&(((mode.equals("pro"))||(mode.equals("j"))))) {
					//if the mode is proverb or joke we call the method
					getModeToServer(mode,serverName); //take mode and server name  as parameter 
				}
				else if (mode.equals("quit")){System.out.println("admin wants to exit \n");} 
				else {System.out.println("Wrong input,Press enter or w");} //check for invalid selection
			} while (mode.indexOf("quit") < 0); // this will run until the user give an exit message or a word with "quit " inside
			System.out.println("Exit From JokeClientAdmin \n");//print exit message
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	// this method is called from the main to connect with the server and pass the selected mode
	static void getModeToServer(String mode, String serverName) {
		String serverResponce; // a string to save server answer
		try {
			Socket sock = new Socket(serverName, 5050); // connection with server in different port than JokeClient
			// The BufferedReader and PrintStream are used to send and receive messages from sockets
			BufferedReader receiveFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //read
			PrintStream sendToServer = new PrintStream(sock.getOutputStream()); //write
			// The server is waiting for admin client to set him in a mode
			sendToServer.println(mode); // send the selected mode to the server
			sendToServer.flush();// clean the memory
			serverResponce = receiveFromServer.readLine(); // read server response 
				if (serverResponce != null) // if the server answer is not empty 
					//the message should be: The selected mode is : proverb/joke
					//as default is the joke mode
					System.out.println(serverResponce);// print server message
			sock.close(); // we call the close method of Java.net.Socket class to terminate the connection
		} catch (IOException x) {
			System.out.println("Socket error."); //the try catch is used to identify connection errors 
			x.printStackTrace();
		}
	}
}
