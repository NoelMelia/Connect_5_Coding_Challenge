package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//is a client program that connects to a server by using
// a socket and sends a greeting, and then waits for a response.
/**
 * Client will program that connects to a server by using a socket and sends a
 * greeting, and then waits for a response.
 * 
 *
 * Client -> Server Server -> Client ---------------- ---------------- MOVE <n>
 * (0 <= n <= 54) WELCOME <char> (char in {X, O}) QUIT VALID_MOVE
 * OTHER_PLAYER_MOVED <n> VICTORY DEFEAT TIE MESSAGE <text>
 * 
 * @author Noel Melia
 * @version 1.9
 *
 */
public class Client {

	private JFrame frame = new JFrame("----------------Connect 5------------- ");
	private JLabel messageLabel = new JLabel("");
	private ImageIcon icon;
	private ImageIcon opponentIcon;
	// 54 Squares on board 9 x 6 = 54 pieces
	private Square[] board = new Square[54];
	private Square currentSquare;

	private static int PORT = 1234;
	private Socket socket;
	private BufferedReader in;
	private static PrintWriter out;

	private String name;
	String response;
	boolean gameplay;

	/** 
	 * Client Constructor to listen for the server Address.
	 * Sets up the read and write for later in the Program.
	 * @param serverAddress - listen for address
     */
	public Client(String serverAddress) throws Exception {

		// Setup networking
		socket = new Socket(serverAddress, PORT);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		BoardSetUp();

	}

	/** 
	 * Setting up the Board for Gui Interface with sizes and colours. 
	 * Setting up the move that the player will select on the gui and it is sent to the Server.
     */
	private void BoardSetUp() {
		// Layout GUI
		messageLabel.setBackground(Color.lightGray);
		frame.getContentPane().add(messageLabel, BorderLayout.SOUTH);

		JPanel boardPanel = new JPanel();
		boardPanel.setBackground(Color.orange);
		// Board Size with Rows and Columns
		boardPanel.setLayout(new GridLayout(6, 9, 3, 3));
		for (int i = 0; i < board.length; i++) {
			final int j = i;
			board[i] = new Square();
			board[i].addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					currentSquare = board[j];
					out.println("MOVE " + j);
				}
			});
			boardPanel.add(board[i]);
		}
		frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
	}

	/** 
	 * Gets the Player name 
     */
	public String getName() {
		return name;
	}
	/** 
	 * Sets the Player name 
	 * @param name of the player
     */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Thread listens for messages from the Server and interacts with client. 
	 * Sets up the images for each player when they move.
     * 
     */
	public void play() throws Exception {
		String response;
		try {
			response = in.readLine();
			if (response.startsWith("BEGIN")) {

				String mark = response.substring(6);
				if (mark.equals("X")) {
					icon = new ImageIcon(getClass().getResource("/images/x.png"));
					opponentIcon = new ImageIcon(getClass().getResource("/images/o.png"));

				} else {
					icon = new ImageIcon(getClass().getResource("/images/o.png"));
					opponentIcon = new ImageIcon(getClass().getResource("/images/x.png"));

				}
				frame.setTitle("Welcome to Connect 5, " + name);
			}

			while (true) {
				response = in.readLine();
				if (response.startsWith("VALID_MOVE")) {
					messageLabel.setText("Valid move, please wait");
					currentSquare = board[Integer.parseInt(response.substring(10))];
					currentSquare.setIcon(icon);
					currentSquare.repaint();
				} else if (response.startsWith("OPPONENT_MOVED")) {
					int loc = Integer.parseInt(response.substring(15));
					board[loc].setIcon(opponentIcon);
					board[loc].repaint();
					messageLabel.setText("Opponent moved, your turn");
				} else if (response.startsWith("VICTORY")) {
					messageLabel.setText("You win");
					break;
				} else if (response.startsWith("DEFEAT")) {
					messageLabel.setText("You lose");
					break;
				} else if (response.startsWith("TIE")) {
					messageLabel.setText("You tied");
					break;
				} else if (response.startsWith("MESSAGE")) {
					messageLabel.setText(response.substring(8));
				} else if (response.startsWith("OTHER_PLAYER_LEFT")) {
					JOptionPane.showMessageDialog(frame, "Other Player left, You Win");
					System.out.println("Other Player Exited. You Win");
					break;

				}
			}

			out.println("QUIT");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket.close();
			frame.dispose();
		}
	}
	/**
     * Asks the Player after game has end to p[lay again with a Gui Message.
     * 
     */
	private boolean wantsToPlayAgain() {
		int response = JOptionPane.showConfirmDialog(frame, "Play Again?", 
				"Connect 5", 
				JOptionPane.YES_NO_OPTION);
		frame.dispose();
		return response == JOptionPane.YES_OPTION;
	}

	 /**
	  * Client Gui window of what the board square will look like.
	  * Sets the icon image that is inserted also and sets the name.
     * 
     */
	static class Square extends JPanel {
		JLabel label = new JLabel((Icon) null);

		public Square() {
			setBackground(Color.white);
			add(label);
		}

		public void setName(String name) {
			label.setText(name);
		}

		public void setIcon(Icon icon) {
			label.setIcon(icon);
		}

	}

	 /**
     * Runs the client as an application. 
     * Asks the Player for Name in console before gameplay. 
     * Then displays the Gui Interface and writes the name to server.
     * Asks the Player if they would like to play again. 
     */
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		String name;
		while (true) {
			String serverAddress = (args.length == 0) ? "localhost" : args[1];
			Client client = new Client(serverAddress);
			System.out.println("Please enter your name: ");
			name = sc.nextLine();

			client.setName(name);
			out.println(name);
			System.out.println("Welcome " + name);
			client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			client.frame.setSize(720, 480);
			client.frame.setVisible(true);
			client.frame.setResizable(false);
			client.play();
			if (!client.wantsToPlayAgain()) {
				System.out.println("Game Over");
				break;
			}
		}
	}
}