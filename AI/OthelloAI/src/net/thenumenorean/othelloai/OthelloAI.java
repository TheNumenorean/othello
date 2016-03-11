package net.thenumenorean.othelloai;

import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.thenumenorean.othelloai.DecisionTree.DecisionTreeNode;
import net.thenumenorean.othelloai.board.Move;
import net.thenumenorean.othelloai.board.OthelloBoard;
import net.thenumenorean.othelloai.board.OthelloBoard.OthelloSide;
import net.thenumenorean.othelloai.board.PositionValue;
import net.thenumenorean.othelloai.comms.CommLink;
import net.thenumenorean.othelloai.comms.StdCommLink;

public class OthelloAI {

	public static final int MAX_THREADS = 20;

	public final OthelloSide LOCAL_SIDE;

	public OthelloBoard board;
	public CommLink link;

	private InputListener inputListener;
	private AIThread aiThread;

	public DecisionTree decisionTree;

	public static void main(String[] args) {

		System.err.println("Starting...");

		CommLink link = new StdCommLink();
		OthelloAI ai = new OthelloAI(link, OthelloSide.valueOf(args[0].toUpperCase()));

		ai.run();

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
		decisionTree = new DecisionTree(this, OthelloSide.BLACK); // Black
																	// always
																	// starts
																	// games

	}

	public void run() {
		// Start all required threads after initializing
		new Thread(inputListener).start();
		Thread t = new Thread(aiThread);

		Move[] nextMoves = board.getValidMoves(OthelloSide.BLACK);
		for (Move move : nextMoves) {
			DecisionTreeNode childNode = decisionTree.new DecisionTreeNode(move, OthelloSide.BLACK);
			decisionTree.getPossibleNextMoves().add(childNode);
			System.err.println("Start moves " + move);
		}

		t.start();

		// Inform host that init is done
		link.sendInitDone();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		System.err.println("Getting best move...");

		if (decisionTree.getNextTurnPlayer() != LOCAL_SIDE)
			throw new InternalError("Tried to get best move for non-local side");

		System.err.print("Checking: ");
		Iterator<DecisionTreeNode> iter = decisionTree.getPossibleNextMoves().iterator();
		DecisionTreeNode minimax = iter.next();
		while (iter.hasNext()) {
			DecisionTreeNode next = iter.next();

			System.err.print("{" + minimax.smartValue + ":" + next.smartValue + "}" + next.checked);

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
		private ThreadCounter runningThreads;

		public AIThread(OthelloAI othelloAI) {
			this.othelloAI = othelloAI;
			stop = false;
			newMove = null;
			runningThreads = new ThreadCounter();
		}

		public void boardChanged(Move m) {
			newMove = m;

		}

		@Override
		public void run() {

			DecisionTreeSearch searcher = new DecisionTreeSearch(decisionTree.getPossibleNextMoves());
			;
			(new Thread(searcher)).start();

			while (!stop) {

				if (searcher.finished) {
					// System.err.println("Restarting searcher...");

					(new Thread(searcher)).start();

				}

				if (newMove != null) {
					searcher.stop();
					searcher = new DecisionTreeSearch(decisionTree.getPossibleNextMoves());
					othelloAI.decisionTree.moveOccured(newMove);
					newMove = null;
					(new Thread(searcher)).start();

				}

				System.err.println(searcher.discovered.size());
				if (!searcher.discovered.isEmpty() && runningThreads.val() < MAX_THREADS) {
					runningThreads.inc();

					(new Thread(new AIJob(othelloAI, board, PositionValue.MINIMAL, searcher.discovered.poll(),
							runningThreads))).start();

				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		/**
		 * Cease execution of the AI thread
		 */
		@SuppressWarnings("unused")
		public void stop() {
			stop = true;
		}
	}

	/**
	 * Searches through the given list, adding
	 * 
	 * @author Francesco
	 *
	 */
	private class DecisionTreeSearch implements Runnable {

		public ConcurrentLinkedQueue<DecisionTreeNode> discovered;
		public boolean finished;

		private boolean stop;
		private ConcurrentLinkedQueue<DecisionTreeNode> init;

		public DecisionTreeSearch(ConcurrentLinkedQueue<DecisionTreeNode> start) {
			discovered = new ConcurrentLinkedQueue<DecisionTreeNode>();
			this.init = start;
			finished = false;
			stop = false;

			if (start.isEmpty())
				System.err.println("empty");
		}

		@Override
		public void run() {
			addChildren(init);
			finished = true;
		}

		private void addChildren(ConcurrentLinkedQueue<DecisionTreeNode> start) {

			if (stop)
				return;

			Stack<ConcurrentLinkedQueue<DecisionTreeNode>> next = new Stack<ConcurrentLinkedQueue<DecisionTreeNode>>();

			Iterator<DecisionTreeNode> iter = start.iterator();
			DecisionTreeNode curr;
			while (iter.hasNext()) {

				if (stop)
					return;

				curr = iter.next();
				System.err.println("NOd: " + curr.toString() + " " + curr.checked);
				if (!curr.checked) {
					curr.checked = true;
					discovered.add(curr);
				} else
					next.push(curr.getChildren());
			}

			while (!next.isEmpty()) {
				if (stop)
					return;

				addChildren(next.pop());
			}
		}

		public void stop() {
			stop = true;
		}

	}

	/**
	 * Simple class to hold an integer which can keep track of the quantity of
	 * things while being passed around.
	 * 
	 * A mutable int.
	 * 
	 * @author Francesco
	 *
	 */
	public static class ThreadCounter {

		private int i;

		public ThreadCounter() {
			i = 0;
		}

		synchronized public void inc() {
			i++;
			System.err.println("Incr to " + i);
		}

		synchronized public void dec() {
			i--;
			System.err.println("Decr to " + i);
		}

		public int val() {
			return i;
		}

	}
}
