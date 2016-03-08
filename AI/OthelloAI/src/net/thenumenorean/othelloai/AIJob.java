package net.thenumenorean.othelloai;

import java.util.Stack;

import net.thenumenorean.othelloai.DecisionTree.DecisionTreeNode;
import net.thenumenorean.othelloai.board.Move;
import net.thenumenorean.othelloai.board.OthelloBoard;
import net.thenumenorean.othelloai.board.OthelloBoard.OthelloSide;
import net.thenumenorean.othelloai.board.PositionValue;

/**
 * Runs computation algorithms for the AI.
 * 
 * @author Francesco
 *
 */
public class AIJob implements Runnable {

	private OthelloAI othelloAI;
	private OthelloBoard board;
	private PositionValue pv;
	private DecisionTreeNode node;

	public AIJob(OthelloAI othelloAI, OthelloBoard board, PositionValue pv, DecisionTreeNode node) {
		this.othelloAI = othelloAI;
		this.board = board;
		this.pv = pv;
		this.node = node;
	}

	@Override
	public void run() {

		/*
		 * This method should calculate the value of the current move, then
		 * propagate it up the line to other moves.
		 */

		OthelloBoard current = generateNewBoardForMove(board, node);
		
		

	}

	/**
	 * Creates a new OthelloBoard with the given board as a template, then does
	 * all the moves leading from the top of the DecisionTree to the given node
	 * to the board.
	 * 
	 * The given move will not have been done on the board, only its
	 * predecessors.
	 * 
	 * @param base
	 *            Board to copy
	 * @param node
	 *            Node to iterate moves from
	 * @return A new OthelloBoard
	 */
	public static OthelloBoard generateNewBoardForMove(OthelloBoard base, DecisionTreeNode node) {

		Stack<Move> moves = new Stack<Move>();
		OthelloSide side = node.getSide();

		DecisionTreeNode tmp = node;
		while (tmp.getParent() != null) {
			tmp = tmp.getParent();
			moves.push(tmp.getMove());
			side = side.opposite();
		}

		OthelloBoard curr = base.copy();
		while (!moves.isEmpty()) {
			curr.move(moves.pop(), side);
			side = side.opposite();
		}

		return curr;
	}

}
