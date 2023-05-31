import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input
 * text box,
 * a button, and a text area for status.
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 * -> modal means that it opens the GUI and suspends background processes.
 * Processing
 * still happens in the GUI. If it is desired to continue processing in the
 * background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x
 * dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the
 * grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui implements OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picturePanel;
	OutputPanel outputPanel;
	public static Client client;
	public int count = 0;
	public static String name;
	public static boolean start = false;
	public static boolean numQuestionsConfirmed = false;

	/**
	 * Construct dialog
	 */
	public ClientGui() {
		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);
	}

	/**
	 * Shows the current state in the GUI
	 * 
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}

	/**
	 * Creates a new game and set the size of the grid
	 * 
	 * @param dimension - the size of the grid will be dimension x dimension
	 */
	public void newGame(int dimension) {
		picturePanel.newGame(dimension);
		// outputPanel.appendOutput("Started new game with a " + dimension + "x" +
		// dimension + " board.");
	}

	/**
	 * Insert an image into the grid at position (col, row)
	 * 
	 * @param filename - filename relative to the root directory
	 * @param row      - the row to insert into
	 * @param col      - the column to insert into
	 * @return true if successful, false if an invalid coordinate was provided
	 * @throws IOException An error occured with your image file
	 */
	public boolean insertImage(String filename, int row, int col) throws IOException {
		String error = "";
		try {
			// insert the image
			if (picturePanel.insertImage(filename, row, col)) {
				// put status in output
				// outputPanel.appendOutput("Inserting " + filename + " in position (" + row +
				// ", " + col + ")");
				return true;
			}
			error = "File(\"" + filename + "\") not found.";
		} catch (PicturePanel.InvalidCoordinateException e) {
			// put error in output
			error = e.toString();
		}
		outputPanel.appendOutput(error);
		return false;
	}

	/**
	 * Submit button handling
	 * 
	 * Handles different inputs such as:
	 * start, more, next, give up, quit
	 */
	@Override
	public void submitClicked() {
		// Pulls the input box text
		String input = outputPanel.getInputText();
		// if has input
		if (input.length() > 0) {
			// append input to the output panel
			outputPanel.appendOutput(input);
			try {
				// client quits and terminates connection (does not work as it should)
				if (input.equals("quit")) {
					client.clientInput = input;
					client.request = Client.quit();

					// we are converting the JSON object we have to a byte[]
					client.output = JsonUtils.toByteArray(client.request);
					NetworkUtils.Send(client.out, client.output);
					System.out.println("Submit");
					System.out.println("Sent for next question");
					System.out.println("Waiting on response");

					client.output = NetworkUtils.Receive(client.in);
					client.message = JsonUtils.fromByteArray(client.output);
					outputPanel.appendOutput(client.message.getString("data"));
					System.out.println("Quiting'");
				}

				// if game has not officially started
				if (start == false) {
					// handle invalid input
					if (!input.equals("start") && numQuestionsConfirmed == true) {
						client.request = Client.invalidInput();
						client.output = JsonUtils.toByteArray(client.request);
						NetworkUtils.Send(client.out, client.output);
						System.out.println("Submit");
						System.out.println("Sent invalid input");
						System.out.println("Waiting on response");

						// Display invalid input
						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);
						outputPanel.appendOutput(client.message.getString("start"));
						System.out.println("Invalid input. Input needs to be 'start'");
						// else send client name
					} else if (count == 0) {
						client.clientName = input;
						client.request = Client.sendName();

						// we are converting the JSON object we have to a byte[]
						client.output = JsonUtils.toByteArray(client.request);
						NetworkUtils.Send(client.out, client.output);
						System.out.println("Submit");
						System.out.println("Waiting on response");

						// get server message
						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);
						System.out.println(client.message.getString("data"));

						// display message to get number of questions to GUI
						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);
						outputPanel.appendOutput(client.message.getString("data"));

						count++;
						// send number of questions to server
					} else if (count == 1) {
						// handle invalid input
						try {
							client.numQuestions = Integer.parseInt(input);
							client.request = Client.sendNumQuestions();

							// we are converting the JSON object we have to a byte[]
							client.output = JsonUtils.toByteArray(client.request);
							NetworkUtils.Send(client.out, client.output);
							System.out.println("Submit");
							System.out.println("Sent a number");
							System.out.println("Waiting on response");

							// get server message
							client.output = NetworkUtils.Receive(client.in);
							client.message = JsonUtils.fromByteArray(client.output);

							// error handling for out of bounds input
							if (client.message.getString("type").equals("maxQuestionsReached")) {
								outputPanel.appendOutput(client.message.getString("data"));
								System.out.println("Not enough questions, choose number from 1 to 6");
								// else client is greeted and informed of how many questions they will answer
							} else {
								System.out.println(client.message.getString("data"));

								client.output = NetworkUtils.Receive(client.in);
								client.message = JsonUtils.fromByteArray(client.output);
								outputPanel.appendOutput(client.message.getString("data"));

								count++;
								numQuestionsConfirmed = true;
							}
							// error handling for invalid input NumberformatException
						} catch (NumberFormatException e) {
							client.request = Client.invalidInput();
							client.output = JsonUtils.toByteArray(client.request);
							NetworkUtils.Send(client.out, client.output);
							System.out.println("Submit");
							System.out.println("Sent invalid input");
							System.out.println("Waiting on response");

							// invalid input displayed
							client.output = NetworkUtils.Receive(client.in);
							client.message = JsonUtils.fromByteArray(client.output);
							outputPanel.appendOutput(client.message.getString("questions"));
							System.out.println("Invalid input. Input needs to be an integer.");
						}
					}
				}

				// if game has started handle more, next, give up, answer, quit inputs
				if (start == true && numQuestionsConfirmed == true) {
					// handle more input
					if (input.equals("more")) {
						client.clientInput = input;
						client.request = Client.more();

						// we are converting the JSON object we have to a byte[]
						client.output = JsonUtils.toByteArray(client.request);
						NetworkUtils.Send(client.out, client.output);
						System.out.println("Submit");
						System.out.println("Sent for more details");
						System.out.println("Waiting on response");

						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);

						// display message/image if more details available
						if (client.message.getString("type").equals("more")) {
							outputPanel.appendOutput(client.message.getString("data"));
							System.out.println("Got more details");

							client.output = NetworkUtils.Receive(client.in);
							client.message = JsonUtils.fromByteArray(client.output);
							insertImage(client.message.getString("image"), 0, 0);
							// else client is informed that no more details are available
						} else {
							outputPanel.appendOutput(client.message.getString("data"));
							System.out.println("No more details available");
						}
						// handle next input
					} else if (input.equals("next")) {
						client.clientInput = input;
						client.request = Client.next();

						// we are converting the JSON object we have to a byte[]
						client.output = JsonUtils.toByteArray(client.request);
						NetworkUtils.Send(client.out, client.output);
						System.out.println("Submit");
						System.out.println("Sent for next question");
						System.out.println("Waiting on response");

						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);

						// display next question if available
						if (client.message.get("type").equals("questionSent")) {
							outputPanel.appendOutput(client.message.getString("data"));
							System.out.println("Got next question");

							client.output = NetworkUtils.Receive(client.in);
							client.message = JsonUtils.fromByteArray(client.output);
							insertImage(client.message.getString("image"), 0, 0);
							// else client is informed that no more questions are available
						} else {
							outputPanel.appendOutput(client.message.getString("data"));
							System.out.println("Out of questions to guess");
						}
						// handle give up input
					} else if (input.equals("give up")) {
						client.clientInput = input;
						client.request = Client.giveUp();

						// we are converting the JSON object we have to a byte[]
						client.output = JsonUtils.toByteArray(client.request);
						NetworkUtils.Send(client.out, client.output);
						System.out.println("Submit");
						System.out.println("Sent for next question");
						System.out.println("Waiting on response");

						// give up message displayed
						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);
						outputPanel.appendOutput(client.message.getString("data"));
						System.out.println("Giving up");

						// winner/loser messages/images are displayed accordingly
						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);
						outputPanel.appendOutput(client.message.getString("data"));

						if (client.message.get("type").equals("winner")) {
							System.out.println("Got a win");
						} else {
							System.out.println("Got a lose");
						}

						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);
						insertImage(client.message.getString("image"), 0, 0);

						// reset to original game settings
						count = 0;
						start = false;
						numQuestionsConfirmed = false;
						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);
						outputPanel.appendOutput(client.message.getString("data"));
						// handle any other input as an answer
					} else {
						client.clientInput = input;
						client.request = Client.answer();

						// we are converting the JSON object we have to a byte[]
						client.output = JsonUtils.toByteArray(client.request);
						NetworkUtils.Send(client.out, client.output);
						System.out.println("Submit");
						System.out.println("Sent for answer");
						System.out.println("Waiting on response");

						client.output = NetworkUtils.Receive(client.in);
						client.message = JsonUtils.fromByteArray(client.output);

						// if server evaluates client's answer as correct, display accordingly
						if (client.message.getString("type").equals("correctAnswer")) {
							outputPanel.appendOutput(client.message.getString("data"));
							System.out.println("Got a correct answer");

							client.output = NetworkUtils.Receive(client.in);
							client.message = JsonUtils.fromByteArray(client.output);

							// display winner/loser messages/images accordingly
							if (client.message.getString("type").equals("winner") ||
									client.message.getString("type").equals("lose")) {
								outputPanel.appendOutput(client.message.getString("data"));

								if (client.message.get("type").equals("winner")) {
									System.out.println("Got a win");
								} else {
									System.out.println("Got a lose");
								}

								client.output = NetworkUtils.Receive(client.in);
								client.message = JsonUtils.fromByteArray(client.output);
								insertImage(client.message.getString("image"), 0, 0);

								// reset to original game settings
								count = 0;
								start = false;
								numQuestionsConfirmed = false;
								client.output = NetworkUtils.Receive(client.in);
								client.message = JsonUtils.fromByteArray(client.output);
								outputPanel.appendOutput(client.message.getString("data"));
							} else {
								insertImage(client.message.getString("image"), 0, 0);
							}
							// else display incorrect message
						} else {
							outputPanel.appendOutput(client.message.getString("data"));
							System.out.println("Got incorrect answer");
						}
					}
				}

				// if game hasn't started, but client entered 'start'
				if (start == false && input.equals("start") && numQuestionsConfirmed == true) {
					client.clientInput = input;
					client.request = Client.sendStartGame();

					// we are converting the JSON object we have to a byte[]
					client.output = JsonUtils.toByteArray(client.request);
					NetworkUtils.Send(client.out, client.output);
					System.out.println("Submit");
					System.out.println("Sent a start");
					System.out.println("Waiting on response");

					// server starts the game
					client.output = NetworkUtils.Receive(client.in);
					client.message = JsonUtils.fromByteArray(client.output);
					insertImage(client.message.getString("image"), 0, 0);

					client.output = NetworkUtils.Receive(client.in);
					client.message = JsonUtils.fromByteArray(client.output);
					outputPanel.appendOutput(client.message.getString("data"));
					System.out.println("Got a question\nQuestion");
					start = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// clear input text box
			outputPanel.setInputText("");
		}
	}

	/**
	 * Key listener for the input text box
	 * 
	 * Change the behavior to whatever you need
	 */
	@Override
	public void inputUpdated(String input) {
		if (input.equals("surprise")) {
			outputPanel.appendOutput("You found me!");
		}
	}

	public static void main(String[] args) throws IOException {
		// create the frame
		ClientGui main = new ClientGui();

		// prepare the GUI for display
		main.newGame(1);

		// show an error for a missing image file
		main.insertImage("img/hi.png", 0, 0);

		// start client server taking in args for port and host
		client = new Client(args);

		// displays server's first message once connection is made
		main.outputPanel.appendOutput(client.message.getString("data"));

		// show the GUI dialog as modal
		main.show(true);
	}
}
