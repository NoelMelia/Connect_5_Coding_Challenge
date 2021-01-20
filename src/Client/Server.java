package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Sockets provide the communication mechanism between two computers using TCP.
 * A client program creates a socket on its end of the communication and
 * attempts to connect that socket to a server.
 * 
 * When the connection is made, the server creates a socket object on its end of
 * the communication. The client and the server can now communicate by writing
 * to and reading from the socket. The java.net.Socket class represents a
 * socket, and the java.net.ServerSocket class provides a mechanism for the
 * server program to listen for clients and establish connections with them.
 * 
 * Main Server Class; Listening on a port for clients; If there is a client,
 * starts a new Thread and goes back to listening for further clients. --
 * 
 * This a server application that uses the Socket class to listen for clients on
 * a port number specified by a command-line argument
 * 
 * @author Noel Melia
 * @version 1.9
 * 
 *          NOTE: https://github.com/wcyuan/java-networking; Website helped in
 *          process
 */
public class Server {
	/**
	 * Main Class Runs the application.
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.ConnectToServer(1234);
	}

	/**
	 * ConnectToServer() - Connects to Server with a port. Listens to Clients and
	 * pairs them on correct address and port number. Connects 2 Players and sets
	 * the current player to begin and begins game.
	 * 
	 * @param port is the port number entered
	 */
	private void ConnectToServer(int port) {
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(port);
			System.out.println("Listening on IP Address: " + listener.getInetAddress());
			System.out.println("Listening on Port: " + listener.getLocalSocketAddress());
			System.out.println("Waiting on 2 Players to Join... ");
			while (true) {
				Game game = new Game();
				Game.Player playerX = game.new Player(listener.accept(), 'X');
				Game.Player playerO = game.new Player(listener.accept(), 'O');
				playerX.setOpponent(playerO);
				playerO.setOpponent(playerX);

				game.currentPlayer = playerX;
				playerX.start();
				playerO.start();
				System.out.println("------------------------Connect 5 Game------------------");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

/**
 * Game class that sets up the Game Settings and talks to the Server in what
 * rules and moves are being made.
 */
class Game {
	/**
	 * Player board with 54 spaces on the board which is 9 x 6. All the spaces are
	 * null at the begin of Game.
	 */
	private Player[] board = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null };

	Player currentPlayer;

	/**
	 * The method checks to see each state if there is a winner with 5 spaces with
	 * vertical, horizontal, AscendingDiagonal and DescendingDiagonal on the board.
	 * 
	 * @return True or False depending on If the player has won or lost.
	 */
	public boolean hasWinner() {

		// Horizontal
		for (int col = 0; col < 54; col += 9) { // column
			for (int row = 0; row < 5; row++) { // row
				System.out.println("Hor " + board[row + col]);
				if (board[row + col] != null && board[row + col] == board[row + col + 1]
						&& board[row + col] == board[row + col + 2] && board[row + col] == board[row + col + 3]
						&& board[row + col] == board[row + col + 4]) {

					System.out.println("Horizontal win.");
					return true;

				}
			}
		}
		// Vertical
		for (int row = 0; row < 5; row++) {
			for (int col = 0; col < 9; col++) {

				if (board[row + col] != null && board[row + col] == board[row + col + 9]
						&& board[row + col] == board[row + col + 18] && board[row + col] == board[row + col + 27]
						&& board[row + col] == board[row + col + 36]) {

					System.out.println("Vertical win.");
					return true;
				}
			}
		}
		// AscendingDiagonal
		for (int row = 27; row < 54; row += 9) {
			for (int col = 0; col < 5; col++) {

				if (board[row + col] != null && board[row + col] == board[(row - 9) + col + 1]
						&& board[(row - 9) + col + 1] == board[row - 18 + col + 2]
						&& board[(row - 18) + col + 2] == board[(row - 27) + col + 3]
						&& board[(row - 27) + col + 3] == board[(row - 36) + col + 4]) {
					System.out.println("Accending Diagonal.");
					return true;
				}
			}
		}
		// DescendingDiagonal
		for (int row = 27; row < 54; row += 9) {
			for (int col = 4; col < 9; col++) {
				if (board[row + col] != null && board[row + col] == board[(row - 9) + col - 1]
						&& board[(row - 9) + col - 1] == board[(row - 18) + col - 2]
						&& board[(row - 18) + col - 2] == board[(row - 27) + col - 3]
						&& board[(row - 27) + col - 3] == board[(row - 36) + col - 4]) {
					System.out.println("Descending Diagonal.");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether there are no more empty squares.
	 */
	public boolean boardFull() {
		for (int i = 0; i < board.length; i++) {
			if (board[i] == null) {
				return false;// At least one cell is not filled
			}
		}

		// All cells are filled
		return true;
	}

	/**
	 * This method is called by each player as they make a move.
	 * 
	 * @param location is the space on board
	 * @param player   for which player has the move
	 */
	public synchronized int legalMove(int location, Player player) {
		int minlocation = (location % 9) + 9 * 5;

		for (int i = minlocation; i >= location; i -= 9) {
			if (player == currentPlayer && board[i] == null) {
				board[i] = currentPlayer;
				currentPlayer = currentPlayer.opponent;
				currentPlayer.otherPlayerMoved(i);
				return i;
			}
		}
		return -1;

	}

	/**
	 * Class for the helper threads in this Multi-threaded server application. Reads
	 * and Write to the client if the Player has made a move and also has the
	 * socket.
	 * 
	 */
	class Player extends Thread {
		char mark;
		Player opponent;
		Socket socket;
		BufferedReader input;
		PrintWriter output;

		/**
		 * Player Constructor that handles the socket and player character Thread.
		 * Displays a message to Welcome the Player if connected successfully.
		 * 
		 * @param socket accepts the client
		 * @param mark states the character
		 */
		public Player(Socket socket, char mark) {
			this.socket = socket;
			this.mark = mark;
			try {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);
				output.println("BEGIN " + mark);
				output.println("MESSAGE Waiting for other player to connect!!!");
				String command = input.readLine();
				System.out.println("Player has Joined, " + command);
			} catch (IOException e) {
				System.out.println("Player died: " + e);
			}
		}

		/**
		 * Accepts notification of who the opponent is.
		 */
		public void setOpponent(Player opponent) {
			this.opponent = opponent;
		}

		/**
		 * Handles the otherPlayerMoved message.
		 */
		public void otherPlayerMoved(int location) {
			output.println("OPPONENT_MOVED " + location);
			output.println(hasWinner() ? "DEFEAT" : boardFull() ? "TIE" : "");
		}

		/**
		 * The run method of this thread.
		 */
		public void run() {
			try {

				// The thread is only started after everyone connects.
				output.println("MESSAGE All players connected");

				// Tell the first player that it is her turn.
				if (mark == 'X') {
					output.println("MESSAGE Your move");
				}

				// Repeatedly get commands from the client and process them.
				while (true) {
					String command = input.readLine();
					if (command.startsWith("MOVE")) {
						int location = Integer.parseInt(command.substring(5));
						int validlocation = legalMove(location, this);
						if (validlocation != -1) {
							output.println("VALID_MOVE" + validlocation);
							output.println(hasWinner() ? "VICTORY" : boardFull() ? "TIE" : "");
						} else {
							output.println("MESSAGE Other Players Move");
						}
					} else if (command.startsWith("QUIT")) {
						System.out.println(command + " Player Exited. Game Over.");
						return;
					}
				}

			} catch (IOException e) {
				System.out.println("Player died: " + e);
			} finally {
				if (opponent != null && opponent.output != null) {
					opponent.output.println("You Win");
				}
				try {
					socket.close();
					System.out.println("Server/Client Side Connection Closed. ");

				} catch (IOException e) {
					System.out.println("Player disconnected: " + e);
					System.exit(1);
				}
			}
		}

	}
}