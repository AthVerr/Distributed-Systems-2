/*--------------------------------------------------------
1. Name: Athina Verroiopoulou / Date: 2/18/2017, File: MyWebServer
2. Java version used: 1.8 
3. Precise command-line compilation examples / instructions: e.g.:
> compile MyWebServer.java with the help of jcx.bat file
use : javac -cp "C:\Program Files\Java\jdk1.8.0_111\lib\xstream-1.2.1.jar;c:\Program Files\Java\jdk1.8.0_111\lib\xpp3_min-1.1.3.4.O.jar" MyWebServer.java
in order to allow usage with xml libraries,  http://xstream.codehaus.org/tutorial.html
4. Precise examples / instructions to run this program: e.g.:
When we have the MyWebServer.class file we run rx.bat to open the server 
then open mimer-call.html with Firefox and click the link or type http://localhost:2540/mimer-data.xyz
the browser will pop up a new window that allow to open with shim.bat
5. List of files needed for running the program. e.g.:
mimer-discussion.html 
MyWebServer.java
BCHandler.java 
serverlog.txt
checklist-mimer.html
mimer-data.xyz
5. Notes: e.g.:
The program is modified to be more simple than MyWebServer and do only what needed
added BC-looper-code-snippet.txt as instructed 
----------------------------------------------------------*/
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
	class Worker extends Thread {
		Socket sock; //Create a local Socket value "sock" 
		//create the socket for communication, act as a constructor 
		Worker(Socket s) {
			sock = s; //pass s as a socket parameter and assign it to sock 
		} 
		public void run() {
			PrintStream sendClientResponse = null; //initialize the out value
			BufferedReader readClientMessage = null; //initialize the in value
			try {
				String[] request = null ;
				readClientMessage = new BufferedReader(new InputStreamReader(sock.getInputStream())); //read from client 
				sendClientResponse = new PrintStream(sock.getOutputStream()); //write/respond to the client
				try {
					String ClientRequest;
					ClientRequest = readClientMessage.readLine(); //read the client request
					System.out.println(ClientRequest); //print the response
					request = ClientRequest.split(" "); //put in a table the words so we can find the request, this should be a get request [0]
					//check for invalid request
					  if (!request[0].matches("GET")) {
				            	System.out.println("Bad Request-the server can not understand this request type"); 	}        
					   //search for a get request, is the first element 
						// the second one [1]  is the  path and the third part [2] is the http 
						String character = request[1].substring(1); //extract the characters from the string, get the path
		            		            	
			            	//respond to the client by returning the correct MIME headers for directory or simple files
		            	  getFileHeader( character, sendClientResponse);//call the method to send the correct MIME headers and reveal the data of the file
						                    
		            
		            sendClientResponse.println("Got your request"); //respond back to client
		            sendClientResponse.flush();
				} catch (IOException x) {
					System.out.println("Server read error");
					x.printStackTrace();
				}
				sock.close(); 
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
			
		}
	
		
		private String setHeader(File data,String type){
			 StringBuilder ClientResponse = new StringBuilder(); //use for append, creates empty builder
			 //write the http headers, use append to add the characters
	        ClientResponse.append("HTTP/1.1 200 OK\n");
	        ClientResponse.append("Content-Length: " + data.length() + "\n");
	        ClientResponse.append("Content-Type: " + type);
	        ClientResponse.append("\r\n\r\n");  	        
	       return ClientResponse.toString();//return the header
		}
		
		
		private void getFileHeader(String character,PrintStream sendClientResponse) { //take as parameter the file characters 
			String filedata;
		        String datatype = null; //initialize the type of the content , can be txt or html
		        if (character.matches("")) { //search the path
            		character = "./"; //this is for directory
                }
		        File data = new File(character);//put the path/character into a file
		          // search if the file path is a file (/) or a directory (./)  
		           
		            
		        //  choose what type the file is		        
		        if (character.endsWith(".txt")) { //if its a txt file
		        	datatype = "text/plain";//set text type
		        } else if (character.endsWith(".html")) {//if its an html file
		        	datatype = "text/html";//set html type
		        }
		        else if (character.endsWith(".xyz")) {//if its an xyz file
		        	datatype = "application/xyz";//set xyz type
		        }
               //put the method for the header into a string variable
		        String Mimeheader=setHeader(data,datatype);
		        try {
		        BufferedReader readInput = new BufferedReader(new FileReader(character));//take as input the requested file 
		        try {
	            	sendClientResponse.println(Mimeheader); //send the tostring method/ header to the client 
	                System.out.println(Mimeheader);//print the header
					sendClientResponse.flush();
	                while ((filedata = readInput.readLine()) != null) {// while there is an input , save the input to a file
	                	sendClientResponse.println(filedata);//the input is send to the client
	                    System.out.println(filedata);//print the input
	                    sendClientResponse.flush();
	                }
	            } catch (IOException ioe) {
	                ioe.printStackTrace();
	            }
	        } catch (FileNotFoundException fn) {
	        	fn.printStackTrace();
	        }
		             }
		
		}
	
	
	class myDataArray {//create a class to store the data into a string array
		int num_lines = 0;//initialize the number of the lines
		String[] lines = new String[10];//make an object of the String array 
		}
	
		class BCWorker extends Thread {
		    private Socket sock;
		    private int i;
		    BCWorker (Socket s){sock = s;}
		    PrintStream out = null; BufferedReader in = null;

		    String[] xmlLines = new String[15]; //a string array object that will be used to count the xml lines
		    String[] testLines = new String[10];//a string array object that will be used to test the lines
		    String xml;
		    String temp;
		    XStream xstream = new XStream();//instantiate the XStream class
		    final String newLine = System.getProperty("line.separator");//make an object p to take the environmental variables
		    myDataArray da = new myDataArray();//call the class myDataArray and create object da
		    
		    public void run(){
		      System.out.println("Called Athinas BC worker.");
		      try{
			in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));//read
			out = new PrintStream(sock.getOutputStream()); // write, send the responce to client
			i = 0; xml = ""; //initialize 
			while(true){
			  temp = in.readLine();//read the data that is in a tempory file
			  if (temp.indexOf("end_of_xml") > -1) break;//quit when have end_of_xml 
			  else xml = xml + temp + newLine; // use stringbuilder to store the xml data into different lines
			}
			System.out.println("The XML marshaled data:");
			System.out.println(xml);//print the xml data
			out.println("Acknowledging Back Channel Data Receipt"); // send the acknowledge to client 
			out.flush(); sock.close();
			
		        da = (myDataArray) xstream.fromXML(xml);  //from the xml we can reconstruct an object by deserialize the xml
			System.out.println("Here is the restored / deserialized data: ");
			for(i = 0; i < da.num_lines; i++){//for the number of lines we have
			  System.out.println(da.lines[i]);//print the data line by line , not in xml form
			}
		      }catch (IOException ioe){
		      } 
		    }
		}

		class BCLooper implements Runnable {  //make it run , not require its own thread 
		  public static boolean adminControlSwitch = true;		  
		  public void run(){ //make it run
		    System.out.println("In Athinas BC Looper thread, waiting for 2570 connections");		    
		    int q_len = 6; //number of client requests
		    int port = 2570;  //port for back door channel communication
		    Socket sock;		    
		    try{
		      ServerSocket servsock = new ServerSocket(port, q_len);
		      while (adminControlSwitch) {
			// wait client connection
			sock = servsock.accept();//start the connection
			new BCWorker (sock).start(); //start the new thread
		      }
		    }catch (IOException ioe) {System.out.println(ioe);}
		  }
		}
		
	public class MyWebServer { //server	
		public static void main(String a[]) throws IOException {
			int requests = 8; //number of client requests			
			int port = 2540;//the port that the server is waiting for connection with client
			File file = new File(".");
       	 try{
       	      String dirRoot = file.getCanonicalPath();//set the root directory where the program will run
       	      System.out.print("The directory root is: " + dirRoot + "\n");//print the directory
       	    }catch (Throwable e){e.printStackTrace();}
			Socket sock;
			BCLooper AL = new BCLooper(); ////when multiple threads share the same object, back door channel communication
			Thread t = new Thread(AL);//create thread with the BCLooper object
			t.start();  //starts the new thread 
			ServerSocket server = new ServerSocket(port, requests);//the socket take as parameter the port and request number and we create an object
					System.out.println("Athinas MYWebserver, listening at port: 2540 \n");
			while (true) {
				//the object created from the server socket uses the accept method to block the program running until there is an incoming request
				sock = server.accept(); //start the connection
				new Worker(sock).start(); //start the new thread
			}
		}
	}

