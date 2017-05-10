
/*  
1. Name: Athina Verroiopoulou / Date: 2/18/2017
2. Java version used: 1.8 
File is: BCHandler.java is used with MyWebServer back channel. 
3. Precise command-line compilation examples / instructions: e.g.:
We need rem jcxclient.bat to create BCHandler.class and allow usage with xml libraries,  http://xstream.codehaus.org/tutorial.html
use inside the file the command:
javac -cp "C:\Program Files\Java\jdk1.8.0_111\lib\xstream-1.2.1.jar;c:\Program Files\Java\jdk1.8.0_111\lib\xpp3_min-1.1.3.4.O.jar" BCHandler.java
4. Precise examples / instructions to run this program: e.g.:
In separate shell windows:
rxclient.bat
shim mimer-data.xyz : run BCHandler with shim.bat
then run rx.bat to start the WebServer
open mimer-call.html with Firefox and click the link
the browser will pop up a new window that allow to open with shim.bat
shim.bat is associated with .xyz file by selecting open with in the file and choose shim.bat file
5. List of files needed for running the program. e.g.:
mimer-discussion.html 
MyWebServer.java
BCHandler.java 
serverlog.txt
checklist-mimer.html
mimer-data.xyz
5. Notes: e.g.:
This program allow the communication with the server, take as input the .xyz file and make the data into xml and then the same data deserialize it into symbolic form
 allow back channel access with webserver by MIME
//----------------------------------------------------------------------*/

import java.io.*;
import java.net.Socket;
import java.util.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
class myDataArray {//create a class to store the data into a string array
int num_lines = 0;//initialize the number of the lines
String[] lines = new String[8];//make an object of the String array 
}
//BBCClient and Handler combined in one class
public class BCHandler {
	
private static String XMLfileName = "mimer.output";  //a string type variable that initialize the path in which the XML file name exist
private static PrintWriter      toXmlOutputFile;   //initialize the XML output file writer , the type of toXmlOutputFile variable is PrintWriter 
private static File             xmlFile;   //initialize the XML file , the type of xmlFile variable is File 
private static BufferedReader   fromMimeDataFile; //initialize the reader from the buffer used for the temporary Mime data file  , the type of fromMimeDataFile variable is BufferedReader 


public static void main(String args[]) {

String serverName;
String argOne = "WillBeFileName";//initialize the file name 
if (args.length < 1) serverName = "localhost";//check for the server and select local host as the default one
else serverName = args[0];
XStream xstream = new XStream(); //instantiate the XStream class
String[] testLines = new String[4]; //a string array object that will be used to test the lines
int i=0;
myDataArray da = new myDataArray(); //make an object of the class myDataArray to count the lines
myDataArray daTest = new myDataArray();//make an object of the class myDataArray to test the lines

System.out.println("Athinas back channel Client.\n");
System.out.println("Using server: " + serverName + ", Port: 2540 / 2570");//print the using server
BufferedReader in = new BufferedReader(new InputStreamReader(System.in));//read
  try {


    System.out.println("Athina is executing the java app.");
    System.out.flush();//clean/flush the memory
  //use the java.lang.System.getProperties() method in order to find the system properties
    Properties p = new Properties(System.getProperties());//make an object p to take the environmental variables
  // use the method getProperty(firstarg) from the object p to get the current set of system properties that contains the temporary file name
     argOne = p.getProperty("firstarg"); //filter that "firstarg" string and store the results into a string variable with the name argOne
    System.out.println("File name is: " + argOne);//print the system properties/ the path of the temporary file name, "miner-data.xyz" in our case
       fromMimeDataFile = new BufferedReader(new FileReader(argOne));//read the temporary Mime data file, pass the value of the temporary file name into the filereader
   
       // Only allows for five lines of data in input file plus safety:
       //we use the object da from the class myDataArray to search into the "miner-data.xyz" file and get the data
       //while is true when we have another line in the file to read (not empty) and at the same time lines of data should be 5 or less plus safety(1 to 7 as i<8)
    while(((da.lines[i++] = fromMimeDataFile.readLine())!= null) && i < 8){
	System.out.println("Data in this line is: " + da.lines[i-1]);//print the data from "miner-data.xyz" file line by line
    }
    // the number of the lines is equal to the length of buffer
    da.num_lines = i-1;//the number of lines we have is the i minus 1
    System.out.println("total number / i is: " + i);//print the i, we should have an i=6 in the handler as we have 5 lines of data      	
	String xml = xstream.toXML(da);//make a call to XStream in order to convert da to XML	
	 sendToBC(xml, serverName); // Send xml to the server with the use of back channel
	  System.out.println("\n\n Serialized into XML:");
	  System.out.print(xml);//print data in xml form	  
	  daTest = (myDataArray) xstream.fromXML(xml); //from the xml we can reconstruct an object by deserialize the xml
	  System.out.println("\n\n Deserialized the data into symbolic form: ");
	  for(i=0; i < daTest.num_lines; i++){//for the number of lines we have
	 System.out.println(daTest.lines[i]);}//print the data line by line , not in xml form
	  System.out.println("\n");   
    //same as handler, might go after while
    xmlFile = new File(XMLfileName);//make a file object of the temporary XML file with the path "mimer.output"
    if (xmlFile.exists() == true && xmlFile.delete() == false){//if the file exist , it should be deleted 
	throw (IOException) new IOException("XML file delete failed.");//throw an exception if delete is not successful
    }
    xmlFile = new File(XMLfileName);//make a file object of the temporary XML file with the path "mimer.output"
    if (xmlFile.createNewFile() == false){ // when the file not exist we should create a new one 
	throw (IOException) new IOException("XML file creation failed.");// check if the creation of the XML file was successful
    }
    else{// use PrintWriter to create the XML file output
	toXmlOutputFile = 
	  new PrintWriter(new BufferedWriter(new FileWriter(XMLfileName)));//take as parameter the XML file path
	toXmlOutputFile.println("First arg to Handler is: " + argOne + "\n");//XML output, show the data
	toXmlOutputFile.println(xml);//location of  XML	
	System.out.println("Closing the output file");
	toXmlOutputFile.close();

    }
  }
  catch (Throwable e) {
    e.printStackTrace();
  }
  finally {
		if (toXmlOutputFile != null) {
			try {toXmlOutputFile.close();} catch (Exception ex) {}
		}
	}
}


static void sendToBC (String sendData, String serverName){//method to send the xml data to the server (back channel)
    Socket sock;
   BufferedReader fromServer;
   PrintStream toServer;
   String textFromServer;
   try{    
     sock = new Socket(serverName, 2570);   //connection with the server, back channel communication
      toServer   = new PrintStream(sock.getOutputStream()); //send to server, write
      // blocked until server replies that data is send
      fromServer = 	new  BufferedReader(new InputStreamReader(sock.getInputStream()));//receive from server, read    
      toServer.println(sendData);//send the data that is in xml form to the server
      toServer.println("end_of_xml");
      toServer.flush(); //clean the buffer  
      System.out.println("Blocking on acknowledgment from Server... ");
      textFromServer = fromServer.readLine();//read server response, that should be the acknowledgment of receiving the data, and block while waiting
      if (textFromServer != null){//print the server message if is not empty
    System.out.println(textFromServer);}
      sock.close();
    } catch (IOException x) {
      System.out.println ("Socket error.");
      x.printStackTrace ();
    }
  }



}
