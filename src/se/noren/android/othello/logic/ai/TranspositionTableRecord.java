package se.noren.android.othello.logic.ai;

import se.noren.android.othello.logic.Board;

public class TranspositionTableRecord {
	
	// Lowest possible value for this node
	public int lowerBound = Integer.MIN_VALUE;
	// Highest possible value for this node
	public int upperBound = Integer.MAX_VALUE;
	// How deep was the search?
	public byte depth = 0;
	// Best move x
	public byte x;
	// Best move y
	public byte y;
	
	// TODO: THROW AWAY!!!
	public Board board = null;
	
	@Override
	public String toString() {
		return "TranspositionTableRecord: d = " + depth + " x = " + x + " y = " + y + " lowbound" + lowerBound + " upbound " + upperBound;
	}
}
