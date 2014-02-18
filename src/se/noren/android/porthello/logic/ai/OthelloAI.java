package se.noren.android.porthello.logic.ai;

import java.util.ArrayList;
import java.util.HashMap;

import se.noren.android.porthello.bridge.OthelloRenderer;
import se.noren.android.porthello.logic.Board;
import se.noren.android.porthello.logic.Move;
import se.noren.android.porthello.logic.VirtualMove;

public class OthelloAI {

	OthelloRenderer renderer;
	OthelloAIConfig config;
	int  evals = 0;
	int  cacheHits = 0;
	long expireTime = 0;
	boolean timeout = false;
	boolean xtremeDebugging = false;
	
	// Precalculate a good approximation of best move evaluation order for good alpha beta pruning
	int[][] moveOrders = new int[Board.SIZEX * Board.SIZEY][2];
	
	HashMap<Integer, TranspositionTableRecord> transpositionMap1 = new HashMap<Integer, TranspositionTableRecord>(); 
	HashMap<Integer, TranspositionTableRecord> transpositionMap2 = transpositionMap1; 
		
	
	public OthelloAI(OthelloRenderer renderer, OthelloAIConfig config) {
		this.config = config;
		this.renderer = renderer;
		
		if (config.useMoveOrderSorting)
			createWeightedMoveOrderLookupTable();
		else
			createBasicMoveOrderLookupTable();		
	}


	
	/***************************************
	 * START OF EVAL FUNCTIONS
	 **************************************/
	
	private int randomEval(Board board, int stage) {
		if (evals++ % 1000 == 0) {
			if (System.currentTimeMillis() > expireTime)
				timeout = true;
		}

		return (int) (Math.random() * 1000.0 - 500.0);
	}
	

	private int simpleEval(Board board, int stage) {
		int c = 0;
		final int[][] b = board.board;
		for (int i = 0; i < Board.SIZEX; i++) {
    		for (int j = 0; j < Board.SIZEY; j++) {
    			c += b[i][j];
    		}
		}
		if (evals++ % 1000 == 0) {
			if (System.currentTimeMillis() > expireTime)
				timeout = true;
		}

		// Check for end conditions
		if (c == stage)
			return Short.MAX_VALUE;
		if (c == -stage)
			return Short.MIN_VALUE;
		
		if (stage == 64) {
			if (c > 0)
				return Short.MAX_VALUE;
			if (c < 0)
				return Short.MIN_VALUE;
		}
		
		return c;
	}

	
	private int positionBased(Board board, int stage) {
		int c = simpleEval(board, stage);
		c += calculatePositionValues(stage, board.board);
		
		return c;
	}



	private int calculatePositionValues(int stage, final int[][] b) {
		int c = 0;
		int c1 = config.cornerValues[0];
		int c2 = config.diamondValues[0];
		int c3 = config.cornerNeighValues[0];
		
		if (stage > config.midGameStart && stage < config.endGameStart) {
			c1 = config.cornerValues[1];
			c2 = config.diamondValues[1];
			c3 = config.cornerNeighValues[1];
		} else if (stage >= config.endGameStart) {
			c1 = config.cornerValues[2];
			c2 = config.diamondValues[2];
			c3 = config.cornerNeighValues[2];			
		}
		
		if (b[0][0] != Board.EMPTY) {
			c += b[0][0] * c1; 
		} else {
			c += b[1][1] * c2;
			c += b[0][1] * c3;
			c += b[1][0] * c3;
		}

		if (b[7][0] != Board.EMPTY) {
			c += b[7][0] * c1; 
		} else {
			c += b[6][1] * c2;
			c += b[6][0] * c3;
			c += b[7][1] * c3;
		}

		if (b[0][7] != Board.EMPTY) {
			c += b[0][7] * c1; 
		} else {
			c += b[1][6] * c2;
			c += b[0][6] * c3;
			c += b[1][7] * c3;
		}

		if (b[7][7] != Board.EMPTY) {
			c += b[7][7] * c1; 
		} else {
			c += b[6][6] * c2;
			c += b[6][7] * c3;
			c += b[7][6] * c3;
		}
		return c;
	}

	
	private int mobilityBased(Board board, int stage) {
		int score = positionBased(board, stage);
		
		// Assume we have not decided victory
		if (Math.abs(score) < 9000) {
			int c = 0;
			int[][] b = board.board;
			for (int i = 0; i < Board.SIZEX; i++) {
	    		for (int j = 0; j < Board.SIZEY; j++) {
	    			if (b[i][j] == Board.EMPTY) {
	    				if (board.isValidMove(i, j, Board.WHITE)) {
	    					c++;
	    				} 
	    				if (board.isValidMove(i, j, Board.BLACK)) {
	    					c--;
	    				}
	    			}
	    		}
			}
			c *= config.mobilityQuantifier;
			score += c;
		}
			
		
		return score;
	}
	
	
	
	/***************************************
	 * END OF EVAL FUNCTIONS
	 **************************************/
	
	
	
	
	
	
	/***************************************
	 * START SEARCH FUNCTIONS
	 **************************************/
	
	/**
	 * @param b
	 * @param color
	 * @param depth
	 * @return [alphabeta value, x, y, evals]
	 */
	public Move alphabeta(Board b, int color) {
		// Debug.startMethodTracing("othello_profiling");
		Move m = new Move();
		int stage = b.countAllTiles();
		evals = 0;
		
		long startLevelTime = System.currentTimeMillis();
		long startTime      = startLevelTime;
		
		// After expire time we must return result
		expireTime = startLevelTime + config.maxTime;
		int[] lastResult = null;
		int   evalsCompletedLevel = 0;
		int   completedDepths = 0;
		long  timeForCompletedEvals = 0;
		
		//TODO THORW
/*
		if (stage == 28)
			xtremeDebugging = true;
		if (stage == 29)
			System.out.println("EXIT!");
	*/
		
		for (int depth = 1; depth <= config.searchDepth; depth++) {
			if (!timeout) {
				// Do recursion to find best move
				int[] result = alphabeta(b, color, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, stage);
				
				long time = System.currentTimeMillis();
				long dt = time - startLevelTime;
				
				// Store info for possible result
				lastResult            = result;
				evalsCompletedLevel   = evals;
				completedDepths       = depth;
				timeForCompletedEvals = time - startTime;
				
				startLevelTime = time;
				
				if (config.printDebugInfo)
					System.out.println("Depth " + depth + " took " + dt + "ms\tx = " + result[1] + "\ty = " + result[2] + "\talpha = " + result[0] + " evals = " + evals + " cahceHits = " + cacheHits);
				
				if (time > expireTime)
					timeout = true;
			}
		}
		
		// Debug.stopMethodTracing();
		
		/* 
		 * Create result information for analysis
		 * */
		m.totalEvals            = evals;
		m.completedDepths       = completedDepths;
		m.alpha                 = lastResult[0];
		m.x                     = lastResult[1];
		m.y                     = lastResult[2];
		m.timeForCompletedEvals = timeForCompletedEvals;
		m.totalTime             = System.currentTimeMillis() - startTime;
		m.evalsOfCompletedDepth = evalsCompletedLevel;
		return m;
	}
	
	
	private int[] alphabeta(Board b, int color, int depth, int alpha, int beta, int stage) {
		int[]   ret = null;
		boolean updateHash = true;
		TranspositionTableRecord record = null;
	
		
		// If using transposition tables we see if we have evaluated this node before
		if (config.useTranspositionTables) {
			record = (color == 1 ? transpositionMap1.get(b.hashCode()) : transpositionMap2.get(b.hashCode()));
			
			// Have we cached a node evaluation of enough search depth?
			if (record != null && record.depth >= depth) {
				cacheHits++;
				
				// TODO: THROW
				if (!b.equals(record.board)) {
					if (xtremeDebugging)
						System.out.println("BAD CACHE KEY HIT! DANGEROUS!!!! " + record);
/*					System.out.println("Our board: " + b.hashCode());
					System.out.println(b);
					System.out.println("Cached board: " + record.board.hashCode());
					System.out.println(record.board);
					*/
				} else {
					if (xtremeDebugging)
						System.out.println("Found record in cache! " + record);
					
					// Case 1 : If we since earlier know that this node is at MOST upperBound and alpha says
					// that our parent node knows that another move will guarantee at least alpha
					// then there is no use expanding this node further.
					//
					// Case 2 : If upper and lower bounds equal we're pretty sure of this node's 
					// value (exact value)
					if (record.upperBound <= alpha || record.upperBound == record.lowerBound) { 
						ret = new int[] { record.upperBound, record.x, record.y };
						updateHash = false;
					
					// Case 3 : If we have a lower bound already cached for this node and 
					// our parent node supplies us with an upper bound of beta larger than our own 
					// bound, we should prune this node since it won't be chosen anyway.
					} else if (record.lowerBound >= beta) {
						ret = new int[] { record.lowerBound, record.x, record.y };
						updateHash = false;
					}
					
					// TODO: We might add some updates of alpha and beta from records here right?!
				}
			}
		}
		
		
	    // If depth = 0, reached base case of recursion -> Evalutate board!
		// Use correct eval function according to configuration.
		if (ret == null && depth == 0) {
			switch (config.evalFunction) {
				case OthelloAIConfig.MOBILITY_BASED:             
					ret = new int[] { mobilityBased(b, stage), -1, -1, 1};
					break;
				case OthelloAIConfig.POSITION_BASED:             
					ret = new int[] { positionBased(b, stage), -1, -1, 1}; 
					break;
				case OthelloAIConfig.SIMPLE_EVAL:                
					ret = new int[] { simpleEval(b, stage), -1, -1, 1};
					break;
				case OthelloAIConfig.RANDOM_EVAL:                
					ret = new int[] { randomEval(b, stage), -1, -1, 1};
					break;
				
				default: throw new IllegalArgumentException("Bad evaluation function in configuration");
			}
		}

		
		// If can't make a move, let other player try
		if (ret == null && !b.canMakeMove(color)) {
			if (b.canMakeMove(-color)) {
				ret = alphabeta(b, -color, depth, alpha, beta, stage);
			} else {
				// Reached terminal node! No more moves possible!
				int tiles = simpleEval(b, stage);
				ret = new int[] {tiles > 0 ? Short.MAX_VALUE : tiles < 0 ? Short.MIN_VALUE : 0, -1, -1, 0};
			}
		}
		
	
		
		// If we have no results until now, we must recurse further on.
		if (ret == null) {
			int bestx = -1, besty = -1;
			
		    if (color == Board.WHITE) {
		    	// White player
		    	int best = Integer.MIN_VALUE;
		    	ArrayList<VirtualMove> moveOrder = decideMoveOrder(b, color);
		    	for (int k = 0; k < moveOrder.size() && !timeout; k++) {
		    		VirtualMove vm = moveOrder.get(k);
		    		int x = vm.x;
		    		int y = vm.y;

    				Board child = b.copy();
    				child.performMove(x, y, color);
    				 
    				int[] result = alphabeta(child, -color, depth - 1, alpha, beta, stage + 1);
    				int   score = result[0];
    				if (score > best) {
    					best = score;
						bestx = x;
						besty = y;
    				}
				
    				if (score > alpha) 
    					alpha = score;
    				
    				// Beta cut off
    				if (beta <= alpha) {
    					break;
    				}
		    	}
		    	
		        ret = new int[] {best, bestx, besty};
		    
		    } else {
		    	// If black
		    	int best = Integer.MAX_VALUE;
		    	ArrayList<VirtualMove> moveOrder = decideMoveOrder(b, color);
		    	for (int k = 0; k < moveOrder.size() && !timeout; k++) {
		    		VirtualMove vm = moveOrder.get(k);
		    		int x = vm.x;
		    		int y = vm.y;

		    		Board child = b.copy();
    				child.performMove(x, y, color);
    				
    				int[] result = alphabeta(child, -color, depth - 1, alpha, beta, stage + 1);
    				int   score = result[0];
    				if (score < best) {
    					best = score;
						bestx = x;
						besty = y;
    				}
    				if (score < beta) 
    					beta = score;
    				
    				// Alpha cut off
    				if (beta <= alpha) {
    					break;
    				}
		    	}
		    	
		        ret = new int[] {best, bestx, besty};
		    }
		}
		
		// In some way we should have a result by now!
		// TODO: Throw away debug check
		if (ret == null)
			throw new RuntimeException("STRANGE, Should have result now!");
		
		if (xtremeDebugging)
			System.out.println("alphabeta finished for board " + b.hashCode() + " at depth = " + depth + " with value " + ret[0] + " and x = " + ret[1] + " y = " + ret[2]);
		
    	// Cache computations for later reuse
    	if (config.useTranspositionTables && updateHash && depth >= config.ignoreCacheDepth) {
    		int value = ret[0];
    		record = new TranspositionTableRecord();
    		record.depth = (byte) depth;
    		record.x = (byte) ret[1];
    		record.y = (byte) ret[2];
    		
    		// If v <= alpha we had an alpha cut off earlier and we don't know the true value
    		// of the node, but the upper limit is 'value'.
    		if (value <= alpha && color == Board.BLACK)
    			record.upperBound = value;
    		
    		// If alpha < value < beta -> no cutoffs so it is an exact value.
    		if (alpha < value && value < beta) {
    			record.lowerBound = value;
    			record.upperBound = value;
    		}
    		
    		// If value > beta -> we had an beta cutoff but we know that
    		// the lower limit must be value at least.
    		if (value >= beta && color == Board.WHITE)
    			record.lowerBound = value;
    		
    		record.board = b;
    		// TODO: Add logic for inexact results!
    		if (color == 1)
    			transpositionMap1.put(b.hashCode(), record);
    		else
    			transpositionMap2.put(b.hashCode(), record);
    		
    		/*
     		if (transpositionMap1.size() % 5000 == 0) {
				Collection<TranspositionTableRecord> values = transpositionMap1.values();
				int[] d = {0,0,0,0,0,0,0,0,0,0,0,0,0};
				for (TranspositionTableRecord v : values) {
					d[v.depth]++;
				}
				for (int i = 0; i < d.length; i++) {
					System.out.print(i + ": " + d[i] + "  ");					
				}
				System.out.println("Size of cache " + transpositionMap1.size());
			}
    		*/
    	}
		
		
		return ret;
	}
	
	/***************************************
	 * END SEARCH FUNCTIONS
	 **************************************/


	
	
	/***************************************
	 * MISCELLANEOUS
	 **************************************/
	
	/**
	 * @return Number of moves to test
	 */
	private ArrayList<VirtualMove> decideMoveOrder(Board b, int color) {
		ArrayList<VirtualMove> moves = new ArrayList<VirtualMove>();
		TranspositionTableRecord record = transpositionMap1.get(b.hashCode());

		if (config.useTranspositionTables && record != null && record.x != -1 && record.y != -1) {
			VirtualMove vm = new VirtualMove(record.x, record.y);
			moves.add(vm);

			for (int k = 0; k < moveOrders.length; k++) {
	    		int x = moveOrders[k][0];
	    		int y = moveOrders[k][1];
				if (!(x == vm.x && y == vm.y) && b.isValidMove(x, y, color)) {
					moves.add(new VirtualMove(x, y));
				}
	    	}
		} else {
			for (int k = 0; k < moveOrders.length; k++) {
	    		int x = moveOrders[k][0];
	    		int y = moveOrders[k][1];
				if (b.isValidMove(x, y, color)) {
					moves.add(new VirtualMove(x, y));
				}
	    	}			
		}
		
		return moves;
	}
	
	
	private void createWeightedMoveOrderLookupTable() {
		int c = 0;
		for (int k = 3; k >= 0; k--)
			for (int xx = k; xx < 8 - k; xx++)
				for (int yy = k; yy < 8 - k; yy++) {
					// Have we added this one before?
					boolean added = false;
					for (int i = 0; i < c; i++) {
						if (moveOrders[i][0] == xx && moveOrders[i][1] == yy)
							added = true;
					}
					
					if (!added) {
						moveOrders[c][0] = xx;
						moveOrders[c][1] = yy;
						c++;
					}
				}
	}

	private void createBasicMoveOrderLookupTable() {
		int c = 0;
		for (int xx = 0; xx < 8; xx++)
			for (int yy = 0; yy < 8; yy++) {
				moveOrders[c][0] = xx;
				moveOrders[c][1] = yy;
				c++;
			}
	}

	
}
