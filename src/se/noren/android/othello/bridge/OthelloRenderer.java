package se.noren.android.othello.bridge;

import se.noren.android.othello.logic.Board;
import se.noren.android.othello.logic.Move;

/**
 * Renderer Othello games 
 * @author Johan
 */
public interface OthelloRenderer {

	/**
	 * Draw a game board to device
	 * @param b Board
	 */
	public void drawGameBoard(Board b, Move move);

	public void drawGameBoardASync(Board b, Move move);

	public void updateProgressBarAsync(int prgress);
	
	public void changeProgressBarStatus(final boolean show);

	public void changeProgressBarStatusASync(final boolean show);
	
	public void showGameOverInformation(final String info, final Board b);
	
	public void moveMade(Move m, Board preMoveBoard);

}
