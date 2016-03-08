package net.thenumenorean.othelloai;

import java.util.Iterator;

import net.thenumenorean.othelloai.DecisionTree.DecisionTreeNode;
import net.thenumenorean.othelloai.board.Move;
import net.thenumenorean.othelloai.board.OthelloBoard;
import net.thenumenorean.othelloai.board.OthelloBoard.OthelloSide;
import net.thenumenorean.othelloai.comms.CommLink;
import net.thenumenorean.othelloai.comms.StdCommLink;

public class OthelloAI {
	
	public static final int MAX_THREADS = 20;

	public final OthelloSide LOCAL_SIDE;

	public OthelloBoard board;
	public CommLink link;

	private InputListener inputListener;
	private AIThread aiThread;

	private DecisionTree decisionTree;

	public static void main(String[] args) {

		CommLink link = new StdCommLink();
		OthelloAI ai = new OthelloAI(link, OthelloSide.valueOf(args[1].toUpperCase()));

		// Better to not return this method until all is finished
		try {
			ai.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Creates a new OthelloAI using the given link for communication.
	 * 
	 * @param link
	 *            Link for communications
	 */
	public OthelloAI(CommLink link, OthelloSide local) {

		LOCAL_SIDE = local;

		this.link = link;
		board = new OthelloBoard();
		inputListener = new InputListener(this);
		aiThread = new AIThread(this);
		decisionTree = new DecisionTree(this, OthelloSide.BLACK); //Black always starts games

		// Start all required threads after initializing
		new Thread(inputListener).start();
		new Thread(aiThread).start();

		// Inform host that init is done
		link.sendInitDone();

	}

	/**
	 * Indicates that a move has been made, and that the ideal next move needs
	 * to be altered.
	 * 
	 * This should cause the AI to reevaluate everything it is doing.
	 */
	public void boardChanged(Move m) {
		aiThread.boardChanged(m);
	}

	public Move getBestMove() {

		if (decisionTree.getNextTurnPlayer() != LOCAL_SIDE)
			throw new InternalError("Tried to get best move for non-local side");

		Iterator<DecisionTreeNode> iter = decisionTree.getPossibleNextMoves().iterator();
		DecisionTreeNode minimax = iter.next();
		while (iter.hasNext()) {
			DecisionTreeNode next = iter.next();

			if (minimax.smartValue < next.smartValue)
				minimax = next;
		}

		return minimax.getMove();
	}

	/**
	 * Manages helper threads, and ensures things keep running smoothly.
	 * 
	 * This thread does the following: -Detects a board piece change, and stops
	 * all jobs to give them new tasks and modify the DecisionTree -Gives jobs
	 * to finished threads -Begins/ends computation -Keeps track of detected
	 * strategy
	 * 
	 * @author Francesco
	 *
	 */
	private class AIThread implements Runnable {

		private OthelloAI othelloAI;
		private boolean stop;
		private Move newMove;

		public AIThread(OthelloAI othelloAI) {
			this.othelloAI = othelloAI;
			stop = false;
			newMove = null;
		}

		public void boardChanged(Move m) {
			newMove = m;
			
		}

		@Override
		public void run() {

			while (!stop) {
				
				if(newMove != null) {
					othelloAI.decisionTree.moveOccured(newMove);
				}
				
				
				
				
			}

		}

		/**
		 * Cease execution of the AI thread
		 */
		public void stop() {
			stop = true;
		}
	}

}
