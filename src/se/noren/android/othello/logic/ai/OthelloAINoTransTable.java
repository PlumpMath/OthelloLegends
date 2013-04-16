package se.noren.android.othello.logic.ai;

import se.noren.android.othello.bridge.OthelloRenderer;
import se.noren.android.othello.logic.Board;

public class OthelloAINoTransTable {

	OthelloRenderer renderer;
	int  evals = 0;
	long expireTime = 0;
	boolean timeout = false;
	
	public OthelloAINoTransTable(OthelloRenderer renderer) {
		this.renderer = renderer;		
	}

	private int minimalisticEvalOld(Board b, int stage) {
		int c = 0;
		for (int i = 0; i < Board.SIZEX; i++) {
    		for (int j = 0; j < Board.SIZEY; j++) {
    			c += b.board[i][j];
    		}
		}
		evals++;
		
		if (evals % 1000 == 0) {
			if (System.currentTimeMillis() > expireTime)
				timeout = true;
		}
		
		return c;
	}

	private int minimalisticEval(Board b, int stage) {
		int c = 0;
		for (int i = 0; i < Board.SIZEX; i++) {
    		for (int j = 0; j < Board.SIZEY; j++) {
    			c += b.board[i][j];
    		}
		}
		evals++;
		
		if (evals % 1000 == 0) {
			if (System.currentTimeMillis() > expireTime)
				timeout = true;
		}

		if (c == -stage)
			return Integer.MIN_VALUE;
		if (c == stage)
			return Integer.MAX_VALUE;

		// Adjust for gamestage, in start and mid game try to minimize
		if (stage < 38) {
			return -c;
		}
		
		return c;
	}
	
	/**
	 * @param b
	 * @param color
	 * @param depth
	 * @return [alphabeta value, x, y, evals]
	 */
	public int[] alphabeta(Board b, int color, int maxDepth, long maxTime) {
		// Debug.startMethodTracing("othello_profiling");
		   
		int stage = b.countAllTiles();
		
		long startTime = System.currentTimeMillis();
		
		// After expire time we must return result
		expireTime = startTime + maxTime;
		int[] lastResult = null;
		
		for (int depth = 1; depth <= maxDepth; depth++) {
			if (!timeout) {
				int[] result = alphabetaRecursive(b, color, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, stage);
				lastResult = result;
				
				long time = System.currentTimeMillis();
				long dt = time - startTime;
				startTime = time;
				
				System.out.println("Depth " + depth + " took " + dt + "ms\tx = " + result[1] + "\ty = " + result[2] + "\talpha = " + result[0] + " evals = " + evals);
				if (time > expireTime)
					timeout = true;
			}
		}
		
		// Debug.stopMethodTracing();
		return lastResult;
	}
	
	
	private int[] alphabetaRecursive(Board b, int color, int depth, int alpha, int beta, int stage) {
		
	    // if depth = 0
		if (depth == 0)
	        return new int[] { minimalisticEval(b, stage), -1, -1, 1};
		
		// If can't make a move, let other player try
		if (!b.canMakeMove(color)) {
			if (b.canMakeMove(-color)) {
				return alphabetaRecursive(b, -color, depth, alpha, beta, stage);
			} else {
				// Reached terminal node! No more moves possible!
				int tiles = b.countTiles(Board.WHITE);
				return new int[] {tiles - (Board.SIZEX * Board.SIZEY / 2) * 10000, -1, -1, 0};
			}
		}
		
		int bestx = -1, besty = -1;
		
	    if (color == Board.WHITE) {
	    	// White player
	    	for (int i = 0; i < Board.SIZEX; i++) {
	    		for (int j = 0; j < Board.SIZEY && !timeout; j++) {
	    			if (b.isValidMove(i, j, color)) {
	    				Board child = b.copy();
	    				child.performMove(i, j, color);
	    				
	    				int[] cutoff = alphabetaRecursive(child, -color, depth - 1, alpha, beta, stage + 1);
	    				if (cutoff[0] > alpha) {
	    					alpha = cutoff[0];
	    					bestx = i;
	    					besty = j;
	    				}
	    				
	    				// Beta cut off
	    				if (beta <= alpha)
	    					return new int[] {alpha, bestx, besty};
	    			}
	    		}
	    	}
	        return new int[] {alpha, bestx, besty};
	    
	    } else {
	    	// If black
	    	for (int i = 0; i < Board.SIZEX; i++) {
	    		for (int j = 0; j < Board.SIZEY && !timeout; j++) {
	    			if (b.isValidMove(i, j, color)) {
	    				Board child = b.copy();
	    				child.performMove(i, j, color);
	    				
	    				int[] cutoff = alphabetaRecursive(child, -color, depth - 1, alpha, beta, stage + 1);
	    				if (cutoff[0] < beta) {
	    					beta = cutoff[0];
	    					bestx = i;
	    					besty = j;
	    				}
	    				
	    				// Alpha cut off
	    				if (beta <= alpha)
	    					return new int[] {beta, bestx, besty};
	    			}
	    		}
	    	}
	        return new int[] {beta, bestx, besty};
	    }
	}
}
