import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.*;

public class Client {
  public Socket sock;
  public String host = "localhost";
  public Integer port = 8080;
  public OutputStream out;
  public InputStream in;
  public JSONObject message;
  public static String clientName;
  public JSONObject request;
  public static int numQuestions;
  public static String clientInput;
  public static byte[] output;
  public static String answer;

  /**
   * runServer & runClient are sufficient to 
   * establish a connection from the client to
   * the server, but can also provide 2 arguments
   * to the client in the terminal to specify a
   * host and port if needed.
   * 
   * @param args arguments taken from terminal
   * for port and host
   */
  public Client(String args[]) {	    
	// works with no inputs or 2
	// no error handling for wrong arguments
	if (args.length >= 2){
	  port = Integer.valueOf(args[0]);
	}
	if (args.length >= 1){ // host, if provided
	  host = args[1];
	}
	    
	// try to instantiate a socket to connect to server
	try {
	  sock = new Socket(host, port);
	  out = sock.getOutputStream();
	  in = sock.getInputStream();
	  
	  System.out.println("Got a connection to the server.");
	    
	  // use to send requests to the server
	  byte[] requestName = NetworkUtils.Receive(in);
	  message = JsonUtils.fromByteArray(requestName); 
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * send request to retrieve server message
   * after client name is sent to the server
   * 
   * @return request
   */
  public static JSONObject sendName() {
    JSONObject request = new JSONObject();
    request.put("selected", 1);
    request.put("data", clientName);
    return request;
  }
  
  /**
   * send request to retrieve server message
   * after client sends the server a number
   * for the number of questions the client
   * wants to guess.
   * 
   * @return request
   */
  public static JSONObject sendNumQuestions() {
	JSONObject request = new JSONObject();
	request.put("selected", 2);
	request.put("data", numQuestions);
    return request;
  }
  
  /**
   * send request to retrieve server message
   * to start the guessing game.
   * 
   * @return request
   */
  public static JSONObject sendStartGame() {
    JSONObject request = new JSONObject();
    request.put("selected", 3);
    return request;
  }
  
  /**
   * send request to server to retrieve
   * more details
   * 
   * @return request
   */
  public static JSONObject more() {
	JSONObject request = new JSONObject();
	request.put("selected", 4);
	return request;
  }
  
  /**
   * send request to server to retrieve
   * next question
   * 
   * @return request
   */
  public static JSONObject next() {
	JSONObject request = new JSONObject();
	request.put("selected", 5);
	return request;
  }
  
  /**
   * send request to server to retrieve
   * answer
   * 
   * @return request
   */
  public static JSONObject answer() {
	JSONObject request = new JSONObject();
	request.put("selected", 6);
	request.put("answer", clientInput);
	return request;
  }
  
  /**
   * send request to server to retrieve
   * invalid input message if any
   * 
   * @return request
   */
  public static JSONObject invalidInput() {
	JSONObject request = new JSONObject();
	request.put("selected", 7);
	return request;
  }
  
  /**
   * send request to server to retrieve
   * information if the user decides to
   * give up
   * 
   * @return request
   */
  public static JSONObject giveUp() {
	JSONObject request = new JSONObject();
	request.put("selected", 8);
	return request;
  }
  
  /**
   * send request to server to retrieve
   * information if the user decides to
   * quit
   * 
   * @return request
   */
  public static JSONObject quit() {
	JSONObject request = new JSONObject();
	request.put("selected", 9);
	return request;
  }
}