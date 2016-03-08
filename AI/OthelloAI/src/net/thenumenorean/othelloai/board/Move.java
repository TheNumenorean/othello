package net.thenumenorean.othelloai.board;

/**
 * Represents a square on the Othello board.
 * 
 * @author Francesco
 *
 */
public class Move {
	
	public static final Move NO_MOVE = new Move(-1, -1);

	private int x, y;

	/**
	 * Creates a new Move with the given coordinates.
	 * 
	 * @param x
	 *            X-coordinate of move.
	 * @param y
	 *            Y-coordinate of move.
	 * 
	 */
	public Move(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Get the X value for this move. If not a move, returns -1.
	 * 
	 * @return X coordinate of move
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the Y value for this move. If not a move, returns -1.
	 * 
	 * @return Y coordinate of move
	 */
	public int getY() {
		return y;
	}
	
	@Override
	public boolean equals(Object m) {
		
		if(!(m instanceof Move))
			return false;
		
		return (((Move) m).getX() == this.getX()) && (((Move) m).getY() == this.getY());
	}

}