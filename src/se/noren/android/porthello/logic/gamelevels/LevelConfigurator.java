package se.noren.android.porthello.logic.gamelevels;

import se.noren.android.porthello.logic.ai.OthelloAIConfig;

public class LevelConfigurator {
	
	public static OthelloAIConfig createConfig(int level) {
		OthelloAIConfig c = new OthelloAIConfig();
		switch (level) {
		case 1:
			c.evalFunction  = OthelloAIConfig.RANDOM_EVAL;
			c.searchDepth   = 2;
			c.maxTime       = 2500;
			c.cornerValues  = new int[] {-10, -10, -10};
			c.diamondValues = new int[] {10, 10, 10};			
			break;
		case 2:
			c.evalFunction  = OthelloAIConfig.POSITION_BASED;
			c.searchDepth   = 2;
			c.maxTime       = 2500;
			c.cornerValues  = new int[] {-10, -10, -10};
			c.diamondValues = new int[] {10, 10, 10};			
			break;
		case 3:
			c.evalFunction = OthelloAIConfig.POSITION_BASED;
			c.searchDepth  = 4;
			c.maxTime      = 2500;
			c.cornerValues  = new int[] {1, 1, 1};
			c.diamondValues = new int[] {1, 1, 1};			
			break;
		case 4:
			c.evalFunction = OthelloAIConfig.POSITION_BASED;
			c.searchDepth  = 5;
			c.maxTime      = 2500;
			break;
		case 5:
			c.evalFunction = OthelloAIConfig.MOBILITY_BASED;
			c.searchDepth  = 20;
			c.maxTime      = 5000;
			break;
		case 6:
			c.evalFunction = OthelloAIConfig.MOBILITY_BASED;
			c.searchDepth  = 64;
			c.maxTime      = 9000;
			break;			
		case 7:
			c.evalFunction = OthelloAIConfig.MOBILITY_BASED;
			c.searchDepth  = 64;
			c.maxTime      = 11000;
			break;			
		}

		
		return c;
	}
}

/*
public int     searchDepth            = 7;
public boolean useTranspositionTables = true;
public boolean useAlphaBetaPruning    = true;
public boolean useMoveOrderSorting    = true;
public long    maxTime                = 5000;
public boolean printDebugInfo         = false;
public int     evalFunction           = 0;
public int     midGameStart           = 30;
public int     endGameStart           = 48;
public int[]   cornerValues           = new int[] {200, 35, 5};
public int[]   diamondValues          = new int[] {-35, -15, -4};
public int[]   cornerNeighValues      = new int[] {-5, -3, 0};
public int     ignoreCacheDepth       = 1;
public int     mobilityQuantifier     = 2;
*/