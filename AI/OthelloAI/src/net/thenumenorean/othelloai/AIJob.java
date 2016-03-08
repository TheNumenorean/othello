package net.thenumenorean.othelloai;

import java.util.Stack;

import net.thenumenorean.othelloai.DecisionTree.DecisionTreeNode;
import net.thenumenorean.othelloai.OthelloAI.ThreadCounter;
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
	private ThreadCounter runningThreads;

	public AIJob(OthelloAI othelloAI, OthelloBoard board, PositionValue pv, DecisionTreeNode node, ThreadCounter runningThreads) {
		this.othelloAI = othelloAI;
		this.board = board;
		this.pv = pv;
		this.node = node;
		this.runningThreads = runningThreads;
	}

	@Override
	public void run() {

		/*
		 * This method should calculate the value of the current move, then
		 * propagate it up the line to other moves.
		 */

		OthelloBoard previous = generateNewBoardForMove(board, node);
		OthelloBoard current = previous.copy();
		if(node.getMove().equals(Move.NO_MOVE)) {
			node.baseValue = 0;
			node.smartValue = 0;
		} else {
		current.move(node.getMove(), node.getSide());
		
		int baseValueGained = getValueDifference(previous, current);
		node.baseValue = baseValueGained;
		node.smartValue = baseValueGained;
		}
		if(othelloAI.LOCAL_SIDE == OthelloSide.BLACK)
		{
			node.score = current.countBlack() - current.countWhite();
		}
		else
		{
			node.score = current.countWhite() - current.countBlack();
		}
		
		OthelloSide nextSide = node.getSide().opposite();
		Move[] nextMoves = current.getValidMoves(nextSide);
		for(Move move : nextMoves)
		{
			DecisionTreeNode childNode = othelloAI.decisionTree.new  DecisionTreeNode(move, nextSide);
			node.addChild(childNode);
		}
		
		runningThreads.dec();

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
			Move nextMove = moves.pop();
			if(!tmp.equals(Move.NO_MOVE))
				curr.move(nextMove, side);
			side = side.opposite();
		}

		return curr;
	}

	/**
	 * Computes the difference between the values of two boards.
	 * 
	 * @param prevBoard
	 *            The previous board configuration
	 * @param currBoard
	 *            The current board configuration after a move was played
	 * @return The value gained/lost from the prevBoard to the currBoard.
	 */
	public int getValueDifference(OthelloBoard prevBoard, OthelloBoard currBoard)
	{
		int prevBoardVal = 0;
		int currBoardVal = 0;
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				if(prevBoard.occupied(i, j))
				{
					if((prevBoard.get(OthelloSide.BLACK, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.BLACK) || (prevBoard.get(OthelloSide.WHITE, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.WHITE))
					{// If the tile is yours, add the value of it to the total, otherwise subtract it.
						prevBoardVal += pv.getValueOfLocation(i, j);
					}
					else
					{
						prevBoardVal -= pv.getValueOfLocation(i, j);
					}
				}
				
				if(currBoard.occupied(i, j))
				{
					if((currBoard.get(OthelloSide.BLACK, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.BLACK) || (currBoard.get(OthelloSide.WHITE, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.WHITE))
					{// If the tile is yours, add the value of it to the total, otherwise subtract it.
						currBoardVal += pv.getValueOfLocation(i, j);
					}
					else
					{
						currBoardVal -= pv.getValueOfLocation(i, j);
					}
				}
			}
		}
		return currBoardVal - prevBoardVal;
	}
}
