package se.noren.android.othello.logic;

import java.util.ArrayList;
import java.util.List;

public class Board {
	
	public static final int SIZEX = 8;
	public static final int SIZEY = 8;
	
	public static final int EMPTY = 0;
	public static final int BLACK = -1;
	public static final int WHITE = 1;
	
	public int[][] board;
	
	private int     hashValue = 0;
	
	// Can we trust the hashvalue or should it be recalculated if anyone asks?
	private boolean dirtyHash = true;
	
	public Board() {
		board = new int[SIZEX][SIZEY];
	}
	
	public Board copy() {
		Board copy = new Board();
    	for (int i = 0; i < Board.SIZEX; i++)
    		for (int j = 0; j < Board.SIZEY; j++)
    			copy.board[i][j] = board[i][j];
    	
    	return copy;
	}
	
	public void resetBoard(boolean inverse) {
    	for (int i = 0; i < Board.SIZEX; i++)
    		for (int j = 0; j < Board.SIZEY; j++)
    			board[i][j] = EMPTY;
    	
    	board[3][4] = inverse ? BLACK : WHITE;
    	board[4][3] = inverse ? BLACK : WHITE;
    	board[3][3] = inverse ? WHITE : BLACK;
    	board[4][4] = inverse ? WHITE : BLACK;
    	
    	dirtyHash = true;
	}

	private final boolean isWithinBorders(final int x, final int y) {
		if (x < 0 || y < 0 || x >= SIZEX || y >= SIZEY)
			return false;
		return true;
	}
	
	/**
	 * Does this move generate a flipping of tiles in a certain direction?
	 * @param x Current x
	 * @param y Current y
	 * @param dx Movement of flip
	 * @param dy Movement of flip
	 * @param ownColor Color to flip to
	 */	
	private boolean generatesFlippableRow(int x, int y, int dx, int dy, int ownColor, boolean foundFlippable) {		
		if (!isWithinBorders(x, y) || board[x][y] == EMPTY) 
			return false;

		if (foundFlippable) {
			if (board[x][y] == ownColor)
				return true;
		} else {
			if (board[x][y] != -ownColor)
				return false;
		}
		return generatesFlippableRow(x + dx, y + dy, dx, dy, ownColor, true);
	}
	
	/**
	 * Is putting a tile on a specific place legal?
	 * @param x X
	 * @param y Y 
	 * @param color Players color
	 * @return Ok?
	 */
	public boolean isValidMove(int x, int y, int color) {
		if (!isWithinBorders(x, y) || board[x][y] != EMPTY)
			return false;
		
		// Search in all directions for a tile to flip!
		// Here figure out the eight traversal directions
		for (int dx = -1; dx <= 1 ; dx++) {
			for (int dy = -1; dy <= 1 ; dy++) {
				// Base case, dx = dy = 0 is not valid
				if (!(dx == 0 && dy == 0)) {
					if (generatesFlippableRow(x + dx, y + dy, dx, dy, color, false))
						return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Performs a move, this operation ASSUMES that a
	 * check against isValidMove() has been made for 
	 * legality of move.  
	 * @param x Movement X
	 * @param y Movement Y
	 * @param color Which player
	 */
	public void performMove(int x, int y, int color) {
		// Search in all directions for a tile to flip!
		// Here figure out the eight traversal directions
		for (int dx = -1; dx <= 1 ; dx++) {
			for (int dy = -1; dy <= 1 ; dy++) {
				// Base case, dx = dy = 0 is not valid
				if (!(dx == 0 && dy == 0)) {
					if (generatesFlippableRow(x + dx, y + dy, dx, dy, color, false)) {
						// So it's legal, flip all tiles in this direction!
						flipRow(x + dx, y + dy, dx, dy, color);
					}
				}
			}
		}
		
		board[x][y] = color;
		dirtyHash = true;
	}

	/**
	 * Assumes boundary checks earlier and check that this is a valid flip
	 * of row. simply performs the flips.
	 * @param x Current x
	 * @param y Current y
	 * @param dx Movement of flip
	 * @param dy Movement of flip
	 * @param ownColor Color to flip to
	 */
	private void flipRow(int x, int y, int dx, int dy, int ownColor) {		
		if (board[x][y] == ownColor) 
			return;
		
		// Flip this tile
		board[x][y] = ownColor;
		
		// Recurse
		flipRow(x + dx, y + dy, dx, dy, ownColor);
		dirtyHash = true;
	}

	/**
	 * Can a certain player make a move?
	 * @param color Color of player
	 * @return Can make move?
	 */
	public boolean canMakeMove(int color) {
		for (int i = 0; i < Board.SIZEX; i++)
    		for (int j = 0; j < Board.SIZEY; j++)
    			if (isValidMove(i, j, color))
    				return true;
		
		return false;
	}
	
	public int countTiles(int color) {
		int c = 0;
		for (int i = 0; i < Board.SIZEX; i++)
    		for (int j = 0; j < Board.SIZEY; j++)
    			if (board[i][j] == color)
    				c++;
    	return c;
	}
	
	public int countAllTiles() {
		int c = 0;
		for (int i = 0; i < Board.SIZEX; i++)
    		for (int j = 0; j < Board.SIZEY; j++)
    			if (board[i][j] != EMPTY)
    				c++;
    	return c;
	}
	
	
	public void put(int value, int x, int y) {
		board[x][y] = value;
	}
	
	public int get(int x, int y) {
		return board[x][y];
	}
	
	/**
	 * @param movex
	 * @param movey
	 * @param color
	 * @return A list of tiles which are flipped by a certain move
	 */
	public List<int[]> flippedTilesForMove(int movex, int movey, int color, Board originalBoard) {
		System.out.println("flippedTilesForMove() " + movex + " " + movey + " col: " + color);
		System.out.println("Old board" + toString());
		Board copy = originalBoard.copy();
		ArrayList<int[]> list = new ArrayList<int[]>();
		
		if (!copy.isValidMove(movex, movey, color))
			return list; 
		
		
		copy.performMove(movex, movey, color);

		System.out.println("new board" + copy.toString());
		
		for (int x = 0; x < Board.SIZEX; x++)
			for (int y = 0; y < Board.SIZEY; y++) {
				if (originalBoard.board[x][y] != Board.EMPTY && originalBoard.board[x][y] != copy.get(x, y)) {
					System.out.println("Found flipped " + x + " " + y);
					list.add(new int[] {x, y});
				}
			}
		return list;
	}
	
	@Override
	public int hashCode() {
		if (dirtyHash) {
			StringBuffer sb = new StringBuffer(128);
			for (int i = 0; i < Board.SIZEX; i++)
	    		for (int j = 0; j < Board.SIZEY; j++) {
	    			// TODO: VERY TEMPORAY, FIX WITH ZOBRIST KEYS! 
	    			sb.append(board[i][j]);
	    		}
	    				
	    	hashValue = sb.toString().hashCode();
	    	dirtyHash = false;
		}
		
		return hashValue;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Board))
			return false;
		
		Board b = (Board) o;
		if (b == this || b.board == this.board)
			return true;
		
		for (int i = 0; i < Board.SIZEX; i++)
    		for (int j = 0; j < Board.SIZEY; j++) 
    			if (b.board[i][j] != board[i][j])
    				return false;
		
		return true;
	}

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String newline = System.getProperty("line.separator");
		for (int i = 0; i < Board.SIZEY; i++) {
			sb.append("|");
			for (int j = 0; j < Board.SIZEX; j++) {
				sb.append(board[j][i] == Board.EMPTY ? " " : board[j][i] == Board.WHITE ? "W" : "B");
			}
			sb.append("|" + newline);
		}
		return sb.toString();
	}
}
