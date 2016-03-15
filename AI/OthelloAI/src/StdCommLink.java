/**
 * 
 */


import java.util.Scanner;

/**
 * Implementation of CommLink that uses Stdin and Stdout to communicate with a
 * othello server.
 * 
 * @author Francesco
 *
 */
public class StdCommLink implements CommLink {

	private Scanner scan;

	public StdCommLink() {
		scan = new Scanner(System.in);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.thenumenorean.othelloai.CommLink#sendMove()
	 */
	@Override
	public void sendMove(Move m) {
		System.out.println(m.getX() + " " + m.getY());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.thenumenorean.othelloai.CommLink#receiveMove()
	 */
	@Override
	public Move receiveMove() {

		String[] in = scan.nextLine().trim().split(" ");

		if (in.length < 3)
			throw new InternalError("Input move too short");

		int x = -1, y = -1;
		try {
			x = Integer.parseInt(in[0]);
			y = Integer.parseInt(in[1]);
		} catch (NumberFormatException e) {
			throw new InternalError("Input move not using integers");
		}

		return new Move(x, y);
	}

	@Override
	public void sendInitDone() {
		System.out.println();

	}

}
