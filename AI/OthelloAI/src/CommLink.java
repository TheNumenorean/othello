/**
 * Interface standardizing communications to an Othello server.
 * 
 * @author Francesco
 *
 */
public interface CommLink {

	/**
	 * Send that the initialization process is done to the server.
	 */
	public void sendInitDone();

	/**
	 * Send a move to the server.
	 * 
	 * @param m
	 *            Move to send
	 */
	public void sendMove(Move m);

	/**
	 * Block until a move is received or an error occurs, and return the move
	 * (or throw the error).
	 * 
	 * @return A Move
	 */
	public Move receiveMove();

}
