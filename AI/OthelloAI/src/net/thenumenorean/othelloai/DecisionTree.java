package net.thenumenorean.othelloai;

import java.util.concurrent.ConcurrentSkipListSet;

import net.thenumenorean.othelloai.board.Move;
import net.thenumenorean.othelloai.board.OthelloBoard.OthelloSide;

/**
 * Represents everything discovered by the AI
 * @author Francesco
 *
 */
public class DecisionTree {
	
	

	public DecisionTree() {
		
	}
	
	/**
	 * Represents a single move in the tree.
	 * 
	 * @author Francesco
	 *
	 */
	public static class DecisionTreeNode implements Comparable<DecisionTreeNode>{
		
		//Value is tthe 'smart' valu, score is the actual score at this stage
		private int value, score;
		
		private ConcurrentSkipListSet<DecisionTreeNode> children;
		private DecisionTreeNode parent;

		private Move move;

		private OthelloSide side;
		
		public DecisionTreeNode(Move m, OthelloSide side) {
			children = new ConcurrentSkipListSet<DecisionTreeNode>();
			this.move = m;
			this.side = side;
		}

		@Override
		public int compareTo(DecisionTreeNode o) {
			return value - o.value;
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
		 * @return the value
		 */
		public int getValue() {
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(int value) {
			this.value = value;
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

		/**
		 * @return the score
		 */
		public int getScore() {
			return score;
		}

		/**
		 * @param score the score to set
		 */
		public void setScore(int score) {
			this.score = score;
		}
		
	}
	
	

}
