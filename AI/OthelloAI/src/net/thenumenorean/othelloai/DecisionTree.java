package net.thenumenorean.othelloai;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.thenumenorean.othelloai.board.Move;
import net.thenumenorean.othelloai.board.OthelloBoard.OthelloSide;

/**
 * Represents everything discovered by the AI
 * 
 * @author Francesco
 *
 */
public class DecisionTree {

	private ConcurrentLinkedQueue<DecisionTreeNode> nextMoves;
	private OthelloSide currentTurn;
	private OthelloAI othelloAI;

	public DecisionTree(OthelloAI othelloAI, OthelloSide currentTurn) {
		this.othelloAI = othelloAI;
		this.currentTurn = currentTurn;
		nextMoves = new ConcurrentLinkedQueue<DecisionTreeNode>();
	}

	public ConcurrentLinkedQueue<DecisionTreeNode> getPossibleNextMoves() {
		return nextMoves;
	}

	/**
	 * Check that the given move is valid. If so, reduce the tree to just
	 * children of the passed move, since we don't care about the others
	 * anymore.
	 * 
	 * The given move is assumed to be a move by the current player.
	 * 
	 * @param m
	 *            The move that occurred.
	 */
	public void moveOccured(Move m) {

		Iterator<DecisionTreeNode> iter = nextMoves.iterator();
		while(iter.hasNext()) {
			DecisionTreeNode test = iter.next();
			if(test.move.equals(m)) {
				nextMoves = test.children;
				
				Iterator<DecisionTreeNode> nextMovesIter = nextMoves.iterator();
				while(nextMovesIter.hasNext()) {
					nextMovesIter.next().setParent(null);
				}
				
				test.setParent(null);
				currentTurn = currentTurn.opposite();
				return;
			}
		}

		throw new InternalError("Move passed that isnt a valid option: " + m);


	}

	public OthelloSide getNextTurnPlayer() {
		return currentTurn;
	}

	/**
	 * Represents a single move in the tree.
	 * 
	 * @author Francesco
	 *
	 */
	public class DecisionTreeNode implements Comparable<DecisionTreeNode> {

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

		private ConcurrentLinkedQueue<DecisionTreeNode> children;
		private DecisionTreeNode parent;
		private Move move;

		private OthelloSide side;
		
		public boolean beingProcessed;

		public DecisionTreeNode(Move m, OthelloSide side) {
			children = new ConcurrentLinkedQueue<DecisionTreeNode>();
			this.move = m;
			this.side = side;

			baseValue = Integer.MIN_VALUE;
			smartValue = Integer.MIN_VALUE;
			score = Integer.MIN_VALUE;
			beingProcessed = false;
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
		public ConcurrentLinkedQueue<DecisionTreeNode> getChildren() {
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

		/*
		 * @Override public boolean equals(Object o) { return
		 * ((DecisionTreeNode)o).move.equals(move) &&
		 * ((DecisionTreeNode)o).parent.equals(parent); }
		 */

		public void recalculateSmartValue() {

			// Should only occur if children havent been added yet.
			if (children.isEmpty()) {
				return;
			}

			Iterator<DecisionTreeNode> iterator = children.iterator();
			int minimax = iterator.next().smartValue;
			while (iterator.hasNext()) {
				int next = iterator.next().smartValue;
				
				if(next == Integer.MIN_VALUE)
					continue;
				
				if (side == DecisionTree.this.othelloAI.LOCAL_SIDE) {
					// CHild nodes are the enemies, so we assume worst case, or
					// most negative

					if (minimax > next)
						minimax = next;

				} else if (minimax < next) {
					minimax = next;
				}
			}
			int oldSV = smartValue;

			smartValue = minimax + baseValue;

			if (smartValue != oldSV && parent != null)
				parent.recalculateSmartValue();
		}
		
		@Override
		public String toString() {
			String out = "";
			
			DecisionTreeNode tmp = this;
			while (tmp != null) {
				out += "->" + tmp.getMove();
				tmp = tmp.getParent();
			}
			return out;
		}

	}

}
