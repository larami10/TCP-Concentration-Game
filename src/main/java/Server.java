import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Random;

import javax.imageio.ImageIO;

import org.json.*;

public class Server {
  public static String name;
  public static int numQuestions;
  public static int currentQuestion = 1;
  public static boolean questionsAvailable = true;
  public static String answer;
  public static int imageSize = 1;
  public static int questionNumber;
  public static int moreDetailsCount = 0;
  public static int currentImageCount = 1;
  public static String currentImage;
  public static int time = 30;
  public static int points = 0;
  public static int totalPointsAvailable;

  /**
   * JSONObject containing a message to request
   * the client's name.
   * 
   * @return json
   */
  public static JSONObject requestName() {
    JSONObject json = new JSONObject();
    json.put("type", "requestName");
    json.put("data", "Hello, please tell me your name.");
    return json;
  }

  /**
   * JSONObject containing information for what
   * to send the client after receiving their name.
   * 
   * @return json
   */
  public static JSONObject retrieveName() {
    JSONObject json = new JSONObject();
    json.put("type", "retrieveName");
    json.put("data", "- Got a name");
    return json;
  }

  /**
   * JSONObject containing a message to let
   * the client know that there is a connection.
   * 
   * @return json
   */
  public static JSONObject sendHello() {
    JSONObject json = new JSONObject();
    json.put("type", "sendHello");
    json.put("data", "Got a hello\nGot start message");
    return json;
  }

  /**
   * JSONObject containing messages to greet
   * the client by name and ask for how many questions
   * they wish to guess.
   * 
   * @return json
   */
  public static JSONObject sendWelcome() {
    JSONObject json = new JSONObject();
    json.put("type", "sendWelcome");
    json.put("data", "Hello " + name + ", how many "
        + "questions would you like me to ask?");
    return json;
  }

  /**
   * JSONObject containing messages letting the
   * terminal know that the server received a number
   * of questions.
   * 
   * @return json
   */
  public static JSONObject retrieveNumQuestions() {
    JSONObject json = new JSONObject();
    json.put("type", "retrieveNumQuestions");
    json.put("data", "- Got a number");
    return json;
  }

  /**
   * JSONObject containing invalid input message
   * letting the user that they selected an invalid
   * number for their number of questions.
   * 
   * @return json
   */
  public static JSONObject maxQuestionsReached() {
    JSONObject json = new JSONObject();
    json.put("type", "maxQuestionsReached");
    json.put("data", "You have chosen an invalid number "
        + "of questions, please choose a number from "
        + "1 to 6.");
    return json;
  }

  /**
   * JSONObject that shows a ready message
   * 
   * @return json
   */
  public static JSONObject sendReady() {
    JSONObject json = new JSONObject();
    json.put("type", "sendReady");
    json.put("data", "- Got a ready");
    return json;
  }

  /**
   * JSONObject that will let the client know
   * that they do not have anymore questions that
   * they can guess and either provide a correct
   * guess or 'quit' to give up
   * 
   * @return json
   */
  public static JSONObject outOfQuestions() {
    JSONObject json = new JSONObject();
    json.put("type", "outOfQuestions");
    json.put("data", "You are out of available "
        + "questions to guess.\n"
        + "Please take a guess or type 'quit' "
        + "to give up.");
    return json;
  }

  /**
   * JSONObject used to let the client know how many
   * questions they will be shown and how much time
   * they have to guess them all.
   * 
   * @return json
   */
  public static JSONObject confirmNumQuestions() {
    JSONObject json = new JSONObject();
    json.put("type", "confirmNumQuestions");
    json.put("data", "Thank you " + name + ", I will "
        + "show you " + numQuestions + " different "
        + "images to guess.\nYou have "
        + time * numQuestions + " seconds to guess "
        + "them correctly. Type 'start' to get the "
        + "first question.");
    return json;
  }

  /**
   * JSONObject showing when a game has started
   * 
   * @return json
   */
  public static JSONObject startGame() {
    JSONObject json = new JSONObject();
    json.put("type", "startGame");
    json.put("data", "- Got a start");
    return json;
  }

  /**
   * JSONObject used to hold an image
   * 
   * @return json
   */
  public static JSONObject image() {
    JSONObject json = new JSONObject();
    json.put("type", "image");
    json.put("image", currentImage + imageSize + ".png");
    json.put("answer", answer);

    return json;
  }

  /**
   * nextImage() will be used to select the
   * next image when called
   */
  private static void nextImage() {
    // if client hasn't reached their total number of questions
    if (currentQuestion < numQuestions) {
      // reset the count to 0 when the last image is reached
      if (currentImageCount == 6) {
        currentImageCount = 0;
      }

      // update count and current question number
      currentImageCount++;
      currentQuestion++;

      // select an image based on current image
      switch (currentImageCount) {
        case (1):
          currentImage = "img/car/car";
          answer = "car";
          break;
        case (2):
          currentImage = "img/cat/cat";
          answer = "cat";
          break;
        case (3):
          currentImage = "img/cucumber/cucumber";
          answer = "cucumber";
          break;
        case (4):
          currentImage = "img/hat/hat";
          answer = "hat";
          break;
        case (5):
          currentImage = "img/pug/pug";
          answer = "pug";
          break;
        case (6):
          currentImage = "img/puppy/puppy";
          answer = "puppy";
          break;
      }
      // reset imageSize to 1 to always display smallest detail first
      imageSize = 1;
    } else {
      // No more question available
      questionsAvailable = false;
    }
  }

  /**
   * JSONObject asking client what the image is
   * 
   * @return json
   */
  public static JSONObject questionSent() {
    JSONObject json = new JSONObject();
    json.put("type", "questionSent");
    json.put("data", "What is this?");
    return json;
  }

  /**
   * JSONObject used to display more detail
   * 
   * @return json
   */
  public static JSONObject more() {
    JSONObject json = new JSONObject();
    json.put("type", "more");
    json.put("data", "Here you go with more details");
    return json;
  }

  /**
   * JSONObject used to let user know that there
   * are no more details left to show
   * 
   * @return json
   */
  public static JSONObject noMoreDetails() {
    JSONObject json = new JSONObject();
    json.put("type", "noMoreDetails");
    json.put("data", "There are no more details that "
        + "can be provided. Provide a guess or "
        + "type 'next' for a new question to guess.");
    return json;
  }

  /**
   * JSONObject letting client know that they
   * have guessed correctly
   * 
   * @return json
   */
  public static JSONObject correctAnswer() {
    JSONObject json = new JSONObject();
    json.put("type", "correctAnswer");
    json.put("data", "You guessed correctly! :)");
    return json;
  }

  /**
   * JSONObject letting client know that they
   * have guessed incorrectly
   * 
   * @return json
   */
  public static JSONObject incorrectAnswer() {
    JSONObject json = new JSONObject();
    json.put("type", "IncorrectAnswer");
    json.put("data", "Wrong answer. Try again!");
    return json;
  }

  /**
   * JSONObject congratulating winner along
   * with their total points
   * 
   * @return json
   */
  public static JSONObject winner() {
    JSONObject json = new JSONObject();
    json.put("type", "winner");
    json.put("data", "Congratulations " + name
        + "!! You won with " + points
        + " total points out of " + totalPointsAvailable
        + " points possible!!");
    return json;
  }

  /**
   * JSONObject displaying winner image
   * 
   * @return json
   */
  public static JSONObject winnerImage() {
    JSONObject json = new JSONObject();
    json.put("type", "winner");
    json.put("image", "img/win.jpg");
    return json;
  }

  /**
   * JSONObject letting client know that they lost
   * along with their total points
   * 
   * @return json
   */
  public static JSONObject lose() {
    JSONObject json = new JSONObject();
    json.put("type", "lose");
    json.put("data", "Better luck next time " + name
        + "! You lose this round with " + points
        + " points out of " + totalPointsAvailable
        + " points possible!");
    return json;
  }

  /**
   * JSONObject displaying loser image
   * 
   * @return json
   */
  public static JSONObject loseImage() {
    JSONObject json = new JSONObject();
    json.put("type", "lose");
    json.put("image", "img/lose.jpg");
    return json;
  }

  /**
   * JSONObject displaying invalid input message
   * 
   * @return json
   */
  public static JSONObject invalidInput() {
    JSONObject json = new JSONObject();
    json.put("type", "InvalidInput");
    json.put("start", "Invalid input. Type 'start' to begin!");
    json.put("questions", "Invalid input. Please "
        + "type a number from 1 to 6.");
    return json;
  }

  /**
   * JSONObject displaying give up message
   * 
   * @return json
   */
  public static JSONObject giveUp() {
    JSONObject json = new JSONObject();
    json.put("type", "giveUp");
    json.put("data", "You gave up, that's too bad :(");
    return json;
  }

  /**
   * JSONObject displaying quit message
   * 
   * @return json
   */
  public static JSONObject quit() {
    JSONObject json = new JSONObject();
    json.put("type", "giveUp");
    if (name == null) {
      json.put("data", "Sorry to see you go " + name
          + "!! Come back soon!! Your final score was: "
          + points);
    } else {
      json.put("data", "Sorry to see you go!! "
          + "Come back soon!! Your final score was: "
          + points);
    }

    return json;
  }

  /**
   * JSONObject displaying play again message
   * 
   * @return json
   */
  public static JSONObject playAgain() {
    JSONObject json = new JSONObject();
    json.put("type", "playAgain");
    json.put("data", "Enter your name to play again "
        + "or quit to end your session");
    return json;
  }

  /**
   * resets variables when play again occurs
   */
  private static void reset() {
    currentQuestion = 1;
    questionsAvailable = true;
    imageSize = 1;
    moreDetailsCount = 0;
    currentImageCount = 1;
    points = 0;
  }

  /**
   * Use port 8080 or argument for port number and
   * wait for client to connect. Once connect, the
   * server will begin sending and retrieving
   * information to play guessing game
   * 
   * @param args argument for port number
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    ServerSocket serv = null;

    Integer port = 8080;

    // works with no inputs or 1
    // no error handling for wrong arguments
    if (args.length >= 1) {
      port = Integer.valueOf(args[0]);
    }

    try {
      serv = new ServerSocket(port);
      System.out.println("Server ready for a connection");
      System.out.println("Server waiting for a connection");
      // NOTE: SINGLE-THREADED, only one connection at a time
      while (true) {
        Socket sock = null;
        try {
          sock = serv.accept(); // blocking wait
          OutputStream out = sock.getOutputStream();
          InputStream in = sock.getInputStream();

          System.out.println("- Got a start");
          System.out.println("New Connection");
          System.out.println("Image Successfully Manipulated!");

          JSONObject requestName = requestName();

          // we are converting the JSON object we have to a byte[]
          byte[] requestNameOutput = JsonUtils.toByteArray(requestName);
          NetworkUtils.Send(out, requestNameOutput);

          while (true) {
            byte[] messageBytes = NetworkUtils.Receive(in);
            JSONObject message = JsonUtils.fromByteArray(messageBytes);
            JSONObject returnMessage;
            if (message.has("selected")) {
              if (message.get("selected") instanceof Long || message.get("selected") instanceof Integer) {
                int choice = message.getInt("selected");
                switch (choice) {
                  // receive client name and send a message to ask client for a number of
                  // questions
                  case (1):
                    name = message.getString("data");
                    returnMessage = retrieveName();
                    System.out.println(returnMessage.getString("data"));

                    // let client know that their connection is alive
                    returnMessage = sendHello();
                    // we are converting the JSON object we have to a byte[]
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);

                    // greet client by name and ask for number of questions
                    returnMessage = sendWelcome();
                    // we are converting the JSON object we have to a byte[]
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);
                    break;
                  // get number of questions from the client
                  case (2):
                    numQuestions = message.getInt("data");

                    // provide error handling for invalid client input
                    if (numQuestions > 6 || numQuestions < 1) {
                      returnMessage = maxQuestionsReached();
                      System.out.println("- Got invalid number of questions");

                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                    } else {
                      // else set totalPointsAvailable based on number of questions
                      totalPointsAvailable = 10 * numQuestions;
                      returnMessage = retrieveNumQuestions();
                      System.out.println(returnMessage.getString("data"));

                      // send ready message to client
                      returnMessage = sendReady();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);

                      // confirm the number of questions client will have to guess
                      // ask client to 'start' game
                      returnMessage = confirmNumQuestions();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                    }
                    break;
                  // game begins and client is sent an image to guess
                  case (3):
                    returnMessage = startGame();
                    System.out.println(returnMessage.getString("data"));

                    // first image
                    currentImage = "img/car/car";
                    answer = "car";

                    returnMessage = image();
                    System.out.println(returnMessage.getString("answer"));
                    System.out.println(returnMessage.getString("image"));
                    System.out.println("Image successfully manipulated");
                    // we are converting the JSON object we have to a byte[]
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);

                    // what is this? is sent to client
                    returnMessage = questionSent();
                    // we are converting the JSON object we have to a byte[]
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);
                    break;
                  // client wants more details
                  case (4):
                    // send more details if available
                    if (imageSize < 3) {
                      imageSize++;
                      moreDetailsCount++;

                      returnMessage = more();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);

                      returnMessage = image();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);

                      System.out.println("More details: " + moreDetailsCount);
                      System.out.println("Image successfully manipulated");
                    } else {
                      // let client know that there are no more details available
                      returnMessage = noMoreDetails();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                      System.out.println("No more details available for image");
                    }
                    break;
                  // client wants to skip to the next image
                  case (5):
                    nextImage();

                    // if client still has questions available, skip to next image
                    if (questionsAvailable == true) {
                      returnMessage = questionSent();
                      System.out.println("Question skipped");
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);

                      returnMessage = image();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);

                      System.out.println(returnMessage.getString("answer"));
                      System.out.println(returnMessage.getString("image"));
                      System.out.println("Image successfully manipulated");
                    } else {
                      // else let client know that they do not have any more questions available
                      returnMessage = outOfQuestions();
                      System.out.println("Out of Questions to guess");
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                    }
                    break;
                  // evaluate client's answer
                  case (6):
                    returnMessage = image();
                    // if client gave correct answer
                    if (message.getString("answer").equals(returnMessage.getString("answer"))) {
                      // update points
                      if (imageSize == 1) {
                        points += 10;
                      } else if (imageSize == 2) {
                        points += 5;
                      } else if (imageSize == 3) {
                        points += 2;
                      }

                      // let client know they got a correct answer
                      returnMessage = correctAnswer();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                      System.out.println("Correct answer");

                      // display next image
                      nextImage();

                      // display winner or loser message/images if no more guess available
                      if (questionsAvailable == false && currentQuestion == numQuestions) {
                        if (points > (totalPointsAvailable / numQuestions)) {
                          returnMessage = winner();
                          // we are converting the JSON object we have to a byte[]
                          messageBytes = JsonUtils.toByteArray(returnMessage);
                          NetworkUtils.Send(out, messageBytes);
                          System.out.println("Send winning message");

                          returnMessage = winnerImage();
                          // we are converting the JSON object we have to a byte[]
                          messageBytes = JsonUtils.toByteArray(returnMessage);
                          NetworkUtils.Send(out, messageBytes);
                        } else {
                          returnMessage = lose();
                          // we are converting the JSON object we have to a byte[]
                          messageBytes = JsonUtils.toByteArray(returnMessage);
                          NetworkUtils.Send(out, messageBytes);
                          System.out.println("Send losing message");

                          returnMessage = loseImage();
                          // we are converting the JSON object we have to a byte[]
                          messageBytes = JsonUtils.toByteArray(returnMessage);
                          NetworkUtils.Send(out, messageBytes);
                        }

                        // reset to original game values
                        reset();

                        // return play again messages
                        returnMessage = playAgain();
                        // we are converting the JSON object we have to a byte[]
                        messageBytes = JsonUtils.toByteArray(returnMessage);
                        NetworkUtils.Send(out, messageBytes);
                        System.out.println("Send play again message");
                        break;
                      } else {
                        // else display next image
                        returnMessage = image();
                        // we are converting the JSON object we have to a byte[]
                        messageBytes = JsonUtils.toByteArray(returnMessage);
                        NetworkUtils.Send(out, messageBytes);
                      }
                    } else {
                      // else display incorrect answer message
                      returnMessage = incorrectAnswer();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                      System.out.println("Incorrect answer");
                    }
                    break;
                  // display invalid input messages
                  case (7):
                    returnMessage = invalidInput();
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);
                    System.out.println("- Got invalid input");
                    break;
                  // display winner/loser messages/images and display client points
                  case (8):
                    returnMessage = giveUp();
                    // we are converting the JSON object we have to a byte[]
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);
                    System.out.println("- Got a give up");

                    if (points > (totalPointsAvailable / numQuestions)) {
                      returnMessage = winner();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                      System.out.println("Send winning message");

                      returnMessage = winnerImage();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                    } else {
                      returnMessage = lose();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                      System.out.println("Send losing message");

                      returnMessage = loseImage();
                      // we are converting the JSON object we have to a byte[]
                      messageBytes = JsonUtils.toByteArray(returnMessage);
                      NetworkUtils.Send(out, messageBytes);
                    }

                    // reset to original game values
                    reset();

                    // return play again messages
                    returnMessage = playAgain();
                    // we are converting the JSON object we have to a byte[]
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);
                    System.out.println("Send play again message");
                    break;
                  // quit case (does not work as it should)
                  case (9):
                    returnMessage = quit();
                    // we are converting the JSON object we have to a byte[]
                    messageBytes = JsonUtils.toByteArray(returnMessage);
                    NetworkUtils.Send(out, messageBytes);
                    System.out.println("- Got a quit");

                    serv.close();
                }
              }
            }
          }
        } catch (Exception e) {
          System.out.println("Client disconnect");
        } finally {
          if (sock != null) {
            sock.close();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (serv != null) {
        serv.close();
      }
    }
  }
}