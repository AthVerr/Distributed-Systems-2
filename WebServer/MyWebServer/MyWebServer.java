/*--------------------------------------------------------
1. Name: Athina Verroiopoulou / Date: 2/5/2017
2. Java version used: 1.8 
3. Precise command-line compilation examples / instructions: e.g.:
> javac MyWebServer.java or javac *.java
4. Precise examples / instructions to run this program: e.g.:
In separate shell windows:
> java MyWebServer, May use java MyTelnet or MyListener

  1.add to the directory that the web server is:
  2.the lion.html / dog.txt
  3.the addnums.html
  4. create subdirectories
  5. use localhost:2540 in Mozilla Firefox browzer or:
  
http://localhost:2540/
http://localhost:2540/lion.html
http://localhost:2540/dog.txt
5. List of files needed for running the program. e.g.:
 a. checklist-mywebserver.html
 b. MyWebServer.java
 c. serverlog.txt.
 d. http-streams.txt
5. Notes: e.g.:
The directory can be seen in the browser and simple html and txt are shown but is not working when we select a directory
May have some with the path and the memory, my program was running successfully when i did it in eclipse but when i moved it in a new directory
the directory is not loaded in the browser and seems there are memory issues
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
					  else {  //search for a get request, is the first element 
						// the second one [1]  is the  path and the third part [2] is the http 
						String character = request[1].substring(1); //extract the characters from the string, get the path
		            	
		            	// if the selected file is a web form
	                    if (character.contains("cgi")) { //if the path contains cgi, mean is a ".fake-cgi"
	                        getaddnum(character, sendClientResponse); //call method
	                        
	                    }else{
			            	//respond to the client by returning the correct MIME headers for directory or simple files
		            	  getFileHeader( character, sendClientResponse);//call the method to send the correct MIME headers and reveal the data of the file
						 
	                    }
		            }
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

		private void getaddnum(String character, PrintStream sendClientResponse) {
			// the character that is passed is the path that we have /cgi/addnums.fake-cgi?person=Matilda&num1=4&num2=5
			//we want to split the path in cgi and focus on the part ?person=Matilda&num1=4&num2=5 which is the last on [2]
			String[] split = character.split("cgi"); //split  the path and keep ?person=Matilda&num1=4&num2=5
			//then we want to seperate the name and the numbers
			String[] seperatePath = split[2].split("&"); //we select the third part and split it where "&" exist
			//we have to save the first[0] part ?person=Matilda, as the name 
			//we use the method substring to extract the characters of the string and find only the name that is pressed every time
			//given a start (?person) and an end which is the lenth of the name
			String name = seperatePath[0].substring(8, seperatePath[0].length());//save the name that we press each time
			//do the same for the 2 numbers that are string form and need to be saved as int, &num1 (5 start)
			int number1 = Integer.parseInt(seperatePath[1].substring(5, seperatePath[1].length()));//save the numbers, first number input
			int number2 = Integer.parseInt(seperatePath[2].substring(5, seperatePath[2].length()));//second number input
			int sum = number1+number2;//calculate the sum   
           String sumResponse="Dear "+name+", the sum of "+number1+" and "+number2+" is "+sum+".";
           sendClientResponse.println(sumResponse);//send the message to the browser
           sendClientResponse.flush();//clean the buffer
    	System.out.println(sumResponse);//print into the terminal
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
		             if (data.isDirectory()) { //it is a directory
		            	 System.out.println("a directory is requested");
		            	 getDirectoryHeader(character, sendClientResponse); //run the method for the directory
		             }
		             else{
		        //  choose what type the file is		        
		        if (character.endsWith(".txt")) { //if its a txt file
		        	datatype = "text/plain";//set text type
		        } else if (character.endsWith(".html")) {//if its an html file
		        	datatype = "text/html";//set html type
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
		private void getDirectoryHeader(String character,PrintStream sendClientResponse) {
			
	        String type = "[...]"; //initialize the type of the content , can be txt or html
	        File data = new File(character);//put the path/character into a file	       
	        File headerFile = new File ("").getAbsoluteFile();//get the path
	      //put the method for the header into a string variable
	        String Mimeheader=setHeader(data,type);	     
	        sendClientResponse.println(Mimeheader); // send  header
	       System.out.println(Mimeheader); //print header
	       sendClientResponse.flush();
	                //for a directory list of data to be view in terminal and browser
	        File[] strFilesDirs = headerFile.listFiles ( ); // Get all the files and directory under root directory
	        sendClientResponse.println("<h1>Index of "+headerFile.toString()+"</h1>"); //send html header to client
		    System.out.println("<h1>Index of "+headerFile.toString()+"</h1>");//print html header 
		    sendClientResponse.flush();
		    for(int i=0; i< strFilesDirs.length; i++){ //search one by one the content of the directory
		    	//send to the browser hot-link references of the files
	    	sendClientResponse.println("<a href="+strFilesDirs[i].getName()+">"+strFilesDirs[i].getName()+"</a><br>"); //send as a response to the client
	    	sendClientResponse.flush();
	    	System.out.println("<a href="+strFilesDirs[i].getName()+">"+strFilesDirs[i].getName()+"</a><br>");//in terminal
	    	
		    }  	  
	}
	}
	 
	public class MyWebServer {
	
		public static void main(String a[]) throws IOException {
			
			int requests = 8; //number of client requests			
			int port = 2540;//the port that the server is waiting for connection with client
			File file = new File(".");
       	 try{
       	      String dirRoot = file.getCanonicalPath();//set the root directory where the program will run
       	      System.out.print("The directory root is: " + dirRoot + "\n");//print the directory
       	    }catch (Throwable e){e.printStackTrace();}
			Socket sock;
			ServerSocket server = new ServerSocket(port, requests);
			System.out.println("Athinas MYWebserver, listening at port: 2540 \n");
			while (true) {
				//the object created from the server socket uses the accept method to block the program running until there is an incoming request
				sock = server.accept(); 
				new Worker(sock).start(); 
			}
		}
	}

