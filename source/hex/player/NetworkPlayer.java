package hex.player;

import hex.preset.Move;
import hex.preset.Player;
import hex.preset.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

/**
 * When leaving the {@link NetworkPlayer} this Player should close all sockets
 * and connections...
 * 
 * @author mlt
 * 
 */
public class NetworkPlayer implements Player {

	/**
	 * Constant for BLUE
	 */
	public static final boolean BLUE = true;

	/**
	 * Constant for RED
	 */
	public static final boolean RED = false;

	/**
	 * Network-Player internal debug-flag
	 */
	private static final boolean DEBUG = true;

	/*
	 * A lot of constants for usage with the server:
	 */
	private static final String MOVE = "[MOVE]";
	private static final String STATUS = "[STATUS]";
	private static final String OPP_MOVE = "[OPP]:[MOVE]";
//	private static final String SERVER_MSG = "[MSG]";
	private static final String SERVER_SYS = "[SYS]";
	private static final String SERVER_SYS_JOINED = "joined";
	private static final String SERVER_SYS_NOGAME = "nogame";
	private static final String SERVER_SYS_GAMECREATED = "gamecreated";
	private static final String SERVER_SYS_OPPJOINED = "oppjoined";

	private int MAX_REQUEST_DELAY = 1000 * 60; // 1 min.

	private volatile Status opponenStatus = null;

	/**
	 * One-Way boolean for identifying if a move was illegal. If it is it will
	 * never turn false again.
	 */
	private volatile boolean giveUpItsIllegal = false;

	/**
	 * The servers address
	 */
	private String serverAddress = null;

	/**
	 * The game-join-code
	 */
	private int joinCode = -1;

	/**
	 * The network-player color - only to use with the above constants.
	 */
	private boolean color;

	/**
	 * The game socket, where the game happens.
	 */
	private Socket gameSocket = null;

	/**
	 * Reader from the network.
	 */
	private BufferedReader inReader = null;

	/**
	 * Writer to network.
	 */
	private PrintStream printStream = null;

	/**
	 * Simples constructor for a DESKTOP network game.
	 * 
	 * @param color
	 * @throws NetworkPlayerException
	 */
	public NetworkPlayer(boolean color) throws NetworkPlayerException {
		// A Toaster is a fancy (self defined) "interface" to provide the user
		// with (mostly error-) messages.
		this.serverAddress = JOptionPane.showInputDialog(null, "ServerAdress: ", "", 1);
		this.color = color;
		this.joinCode = Integer.parseInt(JOptionPane.showInputDialog(null, "Join-Code: ", "", 1));
		initConnection();
	}

	private void initConnection() throws NetworkPlayerException {
		/*
		 * Open Socket and Streams:
		 */
		try {
			// Try to open the game-socket and the corresponding streams.
			gameSocket = new Socket(serverAddress, 1337);
			inReader = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
			printStream = new PrintStream(gameSocket.getOutputStream());
		} catch (UnknownHostException e) {
			/*
			 * NOTE - Standard procedure on error: close everything and throw
			 * the NetworkPlayerException to let the stuff behind know that
			 * something went wrong.
			 */
			close();
			throw new NetworkPlayerException(
					e,
					("Unknown Host:\n " + "Can't find the given host: " + serverAddress + "\nPlease proof the server-address and your internet connection."));
		} catch (IOException e) {
			close();
			throw new NetworkPlayerException(e, "Error in initiating the connection:\n" + "Socket or I/O-Stream could not been initialized.");
		}

		/*
		 * Connection procedure - highly depended on the server.
		 */
		if (DEBUG) {
			System.out.println("Server welcome msg:");
			System.out.println("________________________________________");
		}
		try {
			/*
			 * READ WELCOME MESSAGE:
			 */
			// Read out t he welcome msg...
			// while (inReader.ready()) { // TODO do this nicer way...
			// String s = inReader.readLine();
			// if (s.startsWith(SERVER_MSG) || "".equals(s)) {
			// System.out.println(s);
			// }
			// }
			System.out.println(inReader.readLine());
			System.out.println(inReader.readLine());
			System.out.println(inReader.readLine());
			System.out.println(inReader.readLine());
			System.out.println(inReader.readLine());
			System.out.println("________________________________________");
			// Welcome MSG was read out -

			/*
			 * BLUE: Connection procedure!
			 */
			if (this.color == BLUE) {
				System.out.println("NetworkPlayer.initConnection()");
				// now we connect with the corresponding code:
				String code = new String(this.joinCode == 0 ? "0000" : ("" + joinCode));
				System.out.println("Join with code: " + code);
				printStream.println(code);
				printStream.flush();

				/*
				 * checking for correct connection:
				 */
				long time = System.currentTimeMillis();
				while (!inReader.ready()) {
					Thread.sleep(30);
					if (System.currentTimeMillis() - time > MAX_REQUEST_DELAY) {
						throw new IOException("Timeout while waiting for conenction");
					}
				}
				String joinedStr = inReader.readLine();
				if (DEBUG) {
					System.out.println(joinedStr);
				}
				if (joinedStr.startsWith(SERVER_SYS + ":" + SERVER_SYS_NOGAME)) {
					// SERVER SAID: NO GAME
					close();
					throw new NetworkPlayerException("No game joined:\n"
							+ "There was no game joined for your request. Maybe there are no open ones for " + code + "?");
				} else if (!joinedStr.startsWith(SERVER_SYS + ":" + SERVER_SYS_JOINED)) {
					close();
					throw new NetworkPlayerException("No game joined: \n" + "There was no game joined - Server answered something unexpected");
				} else {
					if (DEBUG) {
						System.out.println("Joined game:" + joinedStr.replace(SERVER_SYS + ":" + SERVER_SYS_JOINED + ":", ""));
					}
				}
			} else {
				/*
				 * RED: Connection procedure! => open a new game
				 */
				printStream.println(); // => Server impl. just hit enter.
				printStream.flush();
				/*
				 * Now we need the code:
				 */
				String code = inReader.readLine();
				if (DEBUG) {
					System.out.println("Server answered on [ENTER]:\n" + code);
				}
				StringTokenizer tokenizer = new StringTokenizer(code, ":");
				if (tokenizer.countTokens() == 3) {
					String sys = tokenizer.nextToken();
					String gamecreated = tokenizer.nextToken();
					if (!sys.equals(SERVER_SYS) || !gamecreated.equals(SERVER_SYS_GAMECREATED)) {
						// EXCEPTION:
						close();
						throw new NetworkPlayerException("Server does not answer with: [SYS]:gamecreated:<ID> Message was:\n" + sys + ":"
								+ gamecreated);
					}
					// Ok now try to find the id:
					try {
						int id = Integer.parseInt(tokenizer.nextToken());
						if (DEBUG) {
							System.out.println("Your join-code is: " + id);
						}
					} catch (NumberFormatException e) {
						close();
						throw new NetworkPlayerException(e,
								"Number or format of join code is illegal. If this error happens you could proof the servers answer (telnet).");
					}
				} else {
					close();
					throw new NetworkPlayerException(
							"Server does not answer with: [SYS]:gamecreated:<ID>\n"
									+ "Excepted a game-ID but was something else. Number or format of join code is illegal. If this happened you could proof the servers answer (telnet).");
				}
			}
		} catch (IOException e) {
			close();
			throw new NetworkPlayerException(e, "Error on reading the welcome message or join code.");
		} catch (InterruptedException e) {
			close();
			throw new NetworkPlayerException(e, "The system was interupted while waiting for an answer due to the connection.");
		}
	}

	@Override
	public Move request() throws NetworkPlayerException {
		if (DEBUG) {
			System.out.println("NetworkPlayer.requestMove()");
		}

		// If the opponent made an illegal move we doing so that he was given
		// up...
		if (giveUpItsIllegal) {
			if (DEBUG) {
				// TODO - find out what happen - especially in the end.
				System.err.println("Unexcepted Status:\n" + "The opponent send an illegal status?!");
			}
			return null;
		}

		// If no connection was made ... Exceptions and notifications should be
		// thrown. => this should not happen:
		if (gameSocket == null || inReader == null) {
			System.err.println("NetworkPlayer.requestMove() - socket or reader null!");
			return null;
		}

		long startRequestTime = System.currentTimeMillis();
		String streamString = null;

		try {
			/*
			 * Reading from the stream until a move is in it.
			 */
			while ((streamString = inReader.readLine()) == null || !streamString.startsWith(OPP_MOVE)) {
				if (streamString != null) {
					if (streamString.contains(SERVER_SYS_OPPJOINED)) {
						if (DEBUG) {
							System.out.println("The opponent joined the game.");
						}
					}

					// // LOCAL DEBUG
					// if (true) {
					// if (streamString.startsWith(SERVER_MSG)) {
					// Output.debug(streamString);
					// } else {
					// Output.debug("Unexpected server answer (Debug):");
					// }
					// }
				}
				// Wait if the stream is empty but only MAX_REQUEST_DELAY long
				Thread.sleep(50);
				if (System.currentTimeMillis() - startRequestTime > MAX_REQUEST_DELAY) {
					close();
					throw new NetworkPlayerException("The opponent was not answering since: " + MAX_REQUEST_DELAY / 1000
							+ " seconds. Game is over now.");
				}
			}
		} catch (IOException e) {
			close();
			 throw new NetworkPlayerException(e, "Error on requesting a move"+ "Could not read the line with the move from the network.");
		} catch (InterruptedException e) {
			close();
			 throw new NetworkPlayerException(e,"Error on requesting a move" + "The thread requesting a move from the network got interrupted.");
		}

		// Ok the strings starts with something looking like a move:
		return stringToMoveAndStatus(streamString); // <- never null
	}

	@Override
	public void confirm(Status boardStatus) throws Exception {
		if (DEBUG) {
			System.out.println("NetworkPlayer.confirm()");
			System.out.println("Own: " + boardStatus);
			System.out.println("Opponent: " + opponenStatus);
		}

		if (boardStatus.getValue() != opponenStatus.getValue()) {
			// TODO think about if this is sufficient
			giveUpItsIllegal = true;
			// TODO ILLEGAL ! next move is null and we update illegal ...
		}

		// if (board.isEnd()) {
		// sendEnd();
		// }
	}

	@Override
	public void update(Move opponentMove, Status boardStatus) throws Exception {
		if (DEBUG) {
			System.out.println("NetworkPlayer.update() ILLEGAL: " + giveUpItsIllegal);
			System.out.println(opponentMove + " Status: " + boardStatus);
		}

		if (giveUpItsIllegal) {
			printStream.println(MOVE + "illegal");
			printStream.flush();
		} else {
			printStream.println(moveAndStatusToString(opponentMove, boardStatus));
			printStream.flush();
		}

		// if (this.playerBoard.isEnd()) {
		// sendEnd();
		// }
	}

	private void close() {
		try {
			if (gameSocket != null) {
				this.gameSocket.close();
			}
			if (inReader != null) {
				this.inReader.close();
			}
			if (printStream != null) {
				this.printStream.println("Closing on error!");
				this.printStream.flush();
				this.printStream.close();
			}
		} catch (IOException e) {
			// we've done our best ^^
			e.printStackTrace();
		}
	}

	// private void sendEnd() throws NetworkPlayerException {
	// System.out.println("NetworkPlayer.sendEnd()");
	// try {
	// this.printStream.println("null");
	// this.printStream.flush();
	// this.printStream.close();
	// this.inReader.close();
	// } catch (IOException e) {
	// Output.debug("Error sending the end of game",
	// "Could not send the correct ending of this game into the network.");
	// close();
	// throw new NetworkPlayerException(e);
	// }
	// }

	/* *******************************************************
	 * Convert String into Move and Move into String
	 * *******************************************************
	 */

	private Status stringToStatus(String streamString) throws NetworkPlayerException {
		if (DEBUG) {
			System.out.println("NetworkPlayer.stringToStatus()");
		}
		Status status = new Status(Status.OK);
		int idx;
		if ((idx = streamString.indexOf(STATUS)) != -1) {
			try {
				status.setValue(Integer.parseInt(streamString.subSequence(idx + STATUS.length(), streamString.length()).toString().trim()));
			} catch (NumberFormatException e) {
				close();
				throw new NetworkPlayerException("Could not parse Status from message: " + streamString);
			}
		} else {
			close();
			throw new NetworkPlayerException("No Status in message... :" + streamString);
		}

		if (DEBUG) {
			System.out.println(status);
		}
		return status;
	}

	private Move stringToMoveAndStatus(final String streamString) throws NetworkPlayerException {
		Move move = null;
		if (DEBUG) {
			System.out.println("Network-Stream-String:\n" + streamString);
		}

		// Cut out the moves x- and y-coordinates:
		// maybe there is no status on illegal or null, we can't cut off here:
		int idx = streamString.indexOf(STATUS);
		String prefix = streamString;
		if (idx != -1) {
			prefix = streamString.substring(0, idx);
		}
		String suffix = prefix.replace(OPP_MOVE, "");

		if (DEBUG) {
			System.out.println("Move: " + suffix);
		}

		/*
		 * Extract the move:
		 */
		StringTokenizer tokenizer = new StringTokenizer(suffix, ";");
		if (tokenizer.countTokens() == 2) {
			try {
				move = new Move(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
			} catch (NumberFormatException e) {
				close();
				 throw new NetworkPlayerException(e,"Unsupported message comming form network" + "Could not parse the string form the network into a move.");
			}
		} else {
			// Inspect for null or illegal - move
			if (tokenizer.hasMoreElements()) {
				String msg = tokenizer.nextToken();
				if (msg.startsWith("illegal")) {
					if (DEBUG) {
						System.err.println("Opponent said: illegal move" +"The opponent gave up, because it was an illegal move. ");
					}
					return null;
				} else if (msg.startsWith("null")) {
					if (DEBUG) {
						System.out.println("Opponent gave up"+"The opponent gave up");	
					}
					return null;
				} else {
					System.err.println("Token while extracting the move: ");
					System.err.println(msg);
				}
			}
			
			if (DEBUG) {
				System.err.println("Unsupported message comming form network"+ "Please proof the server (telnet)");	
			}
			
			close();
			// throw new NetworkPlayerException(
			// "Unsupported message comming form network");
		}
		// TODO check boardimplementation -- only when confirm is called

		opponenStatus = stringToStatus(streamString);

		return move;
	}

	private String moveAndStatusToString(Move move, Status status) {
		// TODO send null-move
		if (DEBUG) {
			System.out.println("NetworkPlayer.moveToString()");
		}

		if (move == null) {
			return MOVE + ":" + "null";
		}

		// x;y
		String s = new StringBuilder().append(MOVE).append(move.getX()).append(";").append(move.getY()).append(STATUS).append(status.getValue())
				.toString();
		if (DEBUG) {
			System.out.println(s);
		}
		return s;
	}

	public class NetworkPlayerException extends Exception {
		private static final long serialVersionUID = 1L;

		public NetworkPlayerException(String s) {
			super(s);
		}

		public NetworkPlayerException(Exception e) {
			super(e);
		}

		public NetworkPlayerException(Exception e, String string) {
			super(string, e);
		}
	}
}
