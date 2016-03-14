package net.thenumenorean.othelloai.board;

/**
 * Represents how to value specific squares.
 * 
 * @author Francesco
 *
 */
public enum PositionValue {

	MINIMAL(1,2,3, -1), STANDARD(1,3, 7, -10), AGGRESSIVE(1, 2, 10, -5);

	private int interiorValue;
	private int edgeValue;
	private int cornerValue;
	private int noMoveValue;

	private static int BOARD_SIZE = 7; // 1 less than actual side length

	PositionValue(int interior, int edge, int corner, int noMove) {
		this.interiorValue = interior;
		this.edgeValue = edge;
		this.cornerValue = corner;
		this.noMoveValue = noMove;
	}

	/**
	 * Helper method to call getValueOfLocation(m.X, m.Y)
	 * 
	 * @param m A move to get the value of
	 * @return A positive integer value
	 */
	public int getValueOfLocation(Move m) {
		return getValueOfLocation(m.getX(), m.getY());
	}

	/**
	 * Calculates and returns the value of the given location on the board.
	 * Thus, it will return either the value for an interior square, edge
	 * square, or corner square.
	 * 
	 * @param x X-coord of location
	 * @param y Y-coord of location
	 * @return A positive integer value
	 */
	public int getValueOfLocation(int x, int y) {
		
		if(x < 0 || y < 0)
			return noMoveValue;
		
		if ((x == BOARD_SIZE || x == 0) && (y == BOARD_SIZE || y == 0))
			return cornerValue;

		if ((x > 0 && x < BOARD_SIZE) && (y > 0 && y < BOARD_SIZE))
			return interiorValue;

		return edgeValue;
	}

}
