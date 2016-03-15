

import java.util.Stack;

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

	public AIJob(OthelloAI othelloAI, OthelloBoard board, PositionValue pv, DecisionTreeNode node,
			ThreadCounter runningThreads) {
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

		node.beingProcessed = true;

		// System.err.println("Starting job on " + node.getMove());
		Thread.currentThread().setName("AIJob-" + node.getMove() + "-" + Thread.currentThread().getId());

		// The thread must finish to decrease the counter or there wont be any
		// new threads.
		try {
			OthelloBoard previous = updateBoardForMove(board, node);
			OthelloBoard current = previous.copy();
			if (node.getMove().equals(Move.NO_MOVE)) {// Eventually replace with
														// pv
														// value for no move
														// case.
				node.baseValue = 0;
				node.smartValue = 0;
			} else {
				current.move(node.getMove(), node.getSide());

				int baseValueGained = getValueDifference(previous, current);
				if (othelloAI.LOCAL_SIDE != node.getSide()) {
					baseValueGained *= -1;
				}
				node.baseValue = baseValueGained;
				node.smartValue = baseValueGained;
			}
			if (othelloAI.LOCAL_SIDE == OthelloSide.BLACK) {
				node.score = current.countBlack() - current.countWhite();
			} else {
				node.score = current.countWhite() - current.countBlack();
			}

			OthelloSide nextSide = node.getSide().opposite();
			Move[] nextMoves = current.getValidMoves(nextSide);
			for (Move move : nextMoves) {
				DecisionTreeNode childNode = othelloAI.decisionTree.new DecisionTreeNode(move, nextSide);
				node.addChild(childNode);
			}
			
			if(node.getParent() != null)
				node.getParent().recalculateSmartValue();
			
		} catch (Exception s) {
			s.printStackTrace();
		} finally {

			runningThreads.dec();
			node.beingProcessed = false;
		}

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
	public static OthelloBoard updateBoardForMove(OthelloBoard base, DecisionTreeNode node) {

		Stack<Move> moves = new Stack<Move>();
		OthelloSide side = node.getSide();

		DecisionTreeNode tmp = node;
		while (tmp.getParent() != null) {
			tmp = tmp.getParent();
			moves.push(tmp.getMove());
			side = side.opposite();
		}

		while (!moves.isEmpty()) {
			Move nextMove = moves.pop();
			if (!tmp.equals(Move.NO_MOVE))
				base.move(nextMove, side);
			side = side.opposite();
		}

		return base;
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
	public int getValueDifference(OthelloBoard prevBoard, OthelloBoard currBoard) {
		int prevBoardVal = 0;
		int currBoardVal = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (prevBoard.occupied(i, j)) {
					if ((prevBoard.get(OthelloSide.BLACK, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.BLACK)
							|| (prevBoard.get(OthelloSide.WHITE, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.WHITE)) {// If
																														// the
																														// tile
																														// is
																														// yours,
																														// add
																														// the
																														// value
																														// of
																														// it
																														// to
																														// the
																														// total,
																														// otherwise
																														// subtract
																														// it.
						prevBoardVal += pv.getValueOfLocation(i, j);
					} else {
						prevBoardVal -= pv.getValueOfLocation(i, j);
					}
				}

				if (currBoard.occupied(i, j)) {
					if ((currBoard.get(OthelloSide.BLACK, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.BLACK)
							|| (currBoard.get(OthelloSide.WHITE, i, j) && othelloAI.LOCAL_SIDE == OthelloSide.WHITE)) {// If
																														// the
																														// tile
																														// is
																														// yours,
																														// add
																														// the
																														// value
																														// of
																														// it
																														// to
																														// the
																														// total,
																														// otherwise
																														// subtract
																														// it.
						currBoardVal += pv.getValueOfLocation(i, j);
					} else {
						currBoardVal -= pv.getValueOfLocation(i, j);
					}
				}
			}
		}
		return currBoardVal - prevBoardVal;
	}
}
