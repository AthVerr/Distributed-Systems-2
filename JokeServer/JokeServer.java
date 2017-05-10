import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

/*--------------------------------------------------------
1. Name: Athina Verroiopoulou / Date: 1/22/2017
2. Java version used: 1.8 
3. Precise command-line compilation examples / instructions: e.g.:
> javac JokeServer.java or javac *.java
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
Not implement the second server
----------------------------------------------------------*/

 class Cookie { //helper class that will allow the connection of the joke and proverb arraylists with the client hashtable
	//volatile is a keyword that is used in multithreading, allow null values, when we access volatile synchronization is happening
    private volatile ArrayList <String> j;
	private volatile ArrayList <String> p;  
	public Cookie() {
		j = new ArrayList<String>();
		p = new ArrayList<String>();
	}
	public ArrayList <String> getJoke(){ 
		return j;
	}
	public ArrayList <String> getProverb(){ 
		return p;
	}
}

class Worker extends Thread {
private static ArrayList <String> proverb = new ArrayList <String> (); //a list for the proverb 
private static ArrayList <String> jokes = new ArrayList <String> (); //a list for the jokes 
//we select hashtable and not hashmap as hashtable is synchronized
static volatile Hashtable<String,Cookie> clients = new Hashtable<String,Cookie>(); //store client cookies into the hashtable with type of String and Cookie class 
	public static int mode=0; // the default mode from client admin is for jokes (0 mode)
	Cookie newCookie; //class cookie
	protected Socket sock; //Create a local Socket  
	//create the socket for communication, act as a constructor 
	Worker(Socket s) {
		sock = s; //pass s as a socket parameter and assign it to sock 
	} 
	public void run() {
		PrintStream sendClientResponse = null; //initialize the value
		BufferedReader readClientMessage = null; //initialize the value
		try {
			//connect with a socket and read and send messages to a client
			readClientMessage = new BufferedReader(new InputStreamReader(sock.getInputStream())); //read
			sendClientResponse = new PrintStream(sock.getOutputStream()); //write
			try {
				String userName;//save the client response - the given user name
				userName = readClientMessage.readLine(); //read what the client message, the user name
				System.out.println("connection to the client with Username" +" " + userName); //message with the user name
				String cookie = readClientMessage.readLine(); //read from JokeClient the cookie 
			    //If this is a new client, add it to the client hashtable
			    if (!clients.containsKey(cookie)){ //if the given cookie not exist in the hashtable
			    	 newCookie = new Cookie(); //call the helper class
			    	 //in the hashtable we store the cookie and that for that client the arraylists should be unique 
			    	clients.put(cookie, newCookie); //map a key (cookie) to a value
			    }
			    jokes=clients.get(cookie).getJoke(); //retrieve the cookie from the hashtable and the joke for that client
			    proverb=clients.get(cookie).getProverb(); 
			    System.out.println("My cookie is" +" " + cookie); //print the cookie , for helping reasons - no need
				getJokesProverb(userName,sendClientResponse); //call the method, username and output is parameters
			} catch (IOException x) {
				System.out.println("Read error"); //in case of error in reading
				x.printStackTrace();
			}
			sock.close(); //we call the close method of Java.net.Socket class to terminate the connection with the specific client, not the server
			System.out.println("Worker thread closed");
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
 //inside the method we have to call the admin to set the proper mode
	//then we add the joke/proverbs into the arraylist if its empty, we shuffle the joke/proverbs and send them one by one per user request
	static void getJokesProverb(String userName, PrintStream sendClientResponse) {
		try {
			//the choice of mode is read by AdminWorker and depending on that AdminWorker notify Worker for that mode
			if(mode==0){//joke mode
				if (jokes.isEmpty()){ //arraylist is empty
				  putJokes(userName);	// call the method to add the jokes in the arraylist  and we pass the username as parameter
				}
				Collections.shuffle(jokes); //randomize the jokes as we shuffle them
				for (String j : jokes) {
					Iterator<String> it = jokes.iterator();
					while(it.hasNext()) { //while it has a joke in the arraylist
					    if (it.next().contains(j)) { //get the joke
					    	sendClientResponse.println(j);//send each joke to Jokeclient
							sendClientResponse.flush();//clean the memory
					        it.remove(); //remove the joke from the arraylist
					        //use that to see the arraylist with the removed jokes
					        //see the list getting empty
					        System.out.println("..........\n"+jokes+"..........\n");
					        System.out.println("Already Send to Client the joke: \n"+j); //the removed element from the arraylist
					    }
					}
				   }  
	            }//end of mode 0
			 if(mode==1){//proverb mode
				 if (proverb.isEmpty()){ //arraylist is empty
					 putProverbs(userName);	// call the method to add the proverbs in the arraylist and we pass the username as parameter
					}
					Collections.shuffle(proverb); //randomize the proverbs as we shuffle them
					for (String p : proverb) {
						Iterator<String> it = proverb.iterator();
						while(it.hasNext()) { //while it has a proverb in the arraylist
						    if (it.next().contains(p)) { //get the proverb
						    	sendClientResponse.println(p);//send each proverb to Jokeclient
								sendClientResponse.flush();//clean the memory
						        it.remove(); //remove the proverb from the arraylist
						       //use that to see the arraylist with the removed jokes
						        //see the list getting empty
						        System.out.println("..........\n"+proverb+"..........\n"); 
						        System.out.println("Already Send to Client the proverb: \n"+p); //the removed element from the arraylist
						    }
						}
					   }  
			 }//end of mode 1
			 
		} catch (Exception ex) {
			sendClientResponse.println("Error during I/O");
		}
	}//end getJokesProverb method
	
	//jokeResponce and proverbResponce are used to add jokes and prover in the array list
	//user name is passed as parameter in order to used as the required output 
	private static void putJokes(String userName) {
		jokes.add("JA "+userName+ " : "+" Can a kangaroo jump higher than a house???? Of course, a house does not jump at all.");
		jokes.add("JB "+userName+" : "+" Why does Snoop Dogg carry an umbrella??? Fo’ drizzle!! ;) ");
		jokes.add("JC "+userName+" : " +" What kind of shoes do ninjas wear??? Sneakers!!!");
		jokes.add("JD "+userName+" : " +" How does NASA organize their company parties??? They planet!!");
	}
	private static void putProverbs(String userName) {
		proverb.add("PA "+userName+ " : "+" The beginning is the half of every action");
		proverb.add("PB "+userName+" : "+"  Before you can score, u must have a goal");
		proverb.add("PC "+userName+" : " +" Act quickly , think slowly ");
		proverb.add("PD "+userName+" : " +" Whatever is good to know is difficult to learn");
	}
}//end of worker thread

class AdminWorker extends Thread{
	Socket Adminsock; //Create a local Socket value "sock" 
	//create the socket for communication, act as a constructor 
	String mode;
	AdminWorker(Socket sock) {
		Adminsock = sock; //pass s as a socket parameter and assign it to sock 
	} 
	public void run() {
		mode="joke"; //default mode
		PrintStream sendAdminClientResponse = null; //initialize the value
		BufferedReader readAdminClientMessage = null; //initialize the  value
		try {
			//connect with a socket and read and send messages to the admin client
			readAdminClientMessage = new BufferedReader(new InputStreamReader(Adminsock.getInputStream())); 
			sendAdminClientResponse = new PrintStream(Adminsock.getOutputStream()); 
			try {
				String AdminCommand;//save the admin client response 
				AdminCommand = readAdminClientMessage.readLine(); //read what the admin client send, the mode
				switch(AdminCommand){ 
				case"pro": mode="proverb";
				Worker.mode=1; //communicate with worker thread and notify for mode 1
				sendAdminClientResponse.println("The selected mode is : " + mode); //message to JokeClientAdmin
					break;
				case"j": mode="joke";
			Worker.mode=0;  //communicate with worker thread and notify for mode 0
				sendAdminClientResponse.println("The selected mode is : " + mode);
					break;
				default:mode="joke";
				Worker.mode=0;  //communicate with worker thread and notify for mode 0
					sendAdminClientResponse.println("The selected mode is : " + mode);
				}
			} catch (IOException x) {
				System.out.println("Read error");//in case of error in reading
				x.printStackTrace();
			}
			Adminsock.close(); //we call the close method of Java.net.Socket class to terminate the connection with the specific client, not the server
			System.out.println("thread admin is closed");
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
 
}

class AdminLooper implements Runnable { //make it run , not require its own thread 
	  public static boolean adminControlSwitch = true;
	  public void run(){ //make it run
	    System.out.println("Admin thread");
	    int requests = 6; 
	    int port = 5050;  // different port 
	    Socket sock;
	    try{
	      ServerSocket servsock = new ServerSocket(port, requests);
	      System.out.println("JokeServer connection to adminClient, listening at port 5050.\n");//print message
	   while (adminControlSwitch) {
		sock = servsock.accept(); //start the connection
		new AdminWorker (sock).start(); //start the new thread
	   }
	   servsock.close();
	   System.out.println("closed\n");//print exit message
	    }catch (IOException ioe) {System.out.println(ioe);}
	  }
}

public class JokeServer {  //the server
	public static boolean controlSwitch= true;
	public static void main(String a[]) throws IOException {
		int requests = 8; //number of client requests	
		int port = 4545;
		ServerSocket server=null;
	try{
	    AdminLooper AL = new AdminLooper(); //when multiple threads share the same object
	   Thread thread = new Thread(AL); //create thread with the admin object
	    thread.start();  //starts the new thread 
		server = new ServerSocket(port, requests);//the socket take as parameter the port and request number and we create an object
		System.out.println("JokeServer connection to Jokeclient, listening at port 4545.\n");//print message
	 } catch (Exception e) {}
		while (controlSwitch) { //work for unlimited connections
			Socket connection = server.accept(); //start the connection
			new Worker(connection).start(); ////start the new thread
		}
		server.close();
		System.out.println("closed\n");//print exit message
     }
}
