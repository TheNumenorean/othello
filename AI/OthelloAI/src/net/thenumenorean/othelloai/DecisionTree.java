package net.thenumenorean.othelloai;

import java.util.concurrent.ConcurrentSkipListSet;

import net.thenumenorean.othelloai.board.Move;
import net.thenumenorean.othelloai.board.OthelloBoard.OthelloSide;

/**
 * Represents everything discovered by the AI
 * 
 * @author Francesco
 *
 */
public class DecisionTree {

	private ConcurrentSkipListSet<DecisionTreeNode> nextMoves;
	private OthelloSide currentTurn;

	public DecisionTree(OthelloSide currentTurn) {
		this.currentTurn = currentTurn;
		nextMoves = new ConcurrentSkipListSet<DecisionTreeNode>();
	}

	public ConcurrentSkipListSet<DecisionTreeNode> getPossibleNextMoves() {
		return nextMoves;
	}

	/**
	 * Check that the given move is valid. If so, reduce the tree to just
	 * children of the passed move, since we dont care about the others anymore.
	 * 
	 * The given move is assumed to be a move by the current player.
	 * 
	 * @param m
	 *            The move that occured.
	 */
	public void moveOccured(Move m) {

		DecisionTreeNode node = null;
		for (DecisionTreeNode tmp : nextMoves) {
			if (tmp.move.equals(m)) {
				node = tmp;
				break;
			}
		}

		if (node == null)
			throw new InternalError("Move passed that isnt a valid option");

		nextMoves = node.children;
		currentTurn = currentTurn.opposite();

	}

	/**
	 * Represents a single move in the tree.
	 * 
	 * @author Francesco
	 *
	 */
	public static class DecisionTreeNode implements Comparable<DecisionTreeNode> {

		/**
		 * the value this specific move gets
		 */
		public int baseValue;

		/**
		 * the value dependent on possible future moves
		 */
		public int smartValue;

		/**
		 * technical score at this point in the game
		 */
		public int score;

		private ConcurrentSkipListSet<DecisionTreeNode> children;
		private DecisionTreeNode parent;

		private Move move;

		private OthelloSide side;

		public DecisionTreeNode(Move m, OthelloSide side) {
			children = new ConcurrentSkipListSet<DecisionTreeNode>();
			this.move = m;
			this.side = side;

			baseValue = Integer.MIN_VALUE;
			smartValue = Integer.MIN_VALUE;
			score = Integer.MIN_VALUE;
		}

		@Override
		public int compareTo(DecisionTreeNode o) {
			return smartValue - o.smartValue;
		}

		public DecisionTreeNode getParent() {
			return parent;
		}

		public void setParent(DecisionTreeNode parent) {
			this.parent = parent;
		}

		/**
		 * @return the children
		 */
		public ConcurrentSkipListSet<DecisionTreeNode> getChildren() {
			return children;
		}

		/**
		 * @return the move
		 */
		public Move getMove() {
			return move;
		}

		public void addChild(DecisionTreeNode n) {
			n.setParent(this);
			children.add(n);
		}

		/**
		 * @return the side
		 */
		public OthelloSide getSide() {
			return side;
		}

	}

}
