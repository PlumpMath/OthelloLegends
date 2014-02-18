package se.noren.android.porthello.logic.ai;

public class OthelloAIConfig {
	
	public static final int SIMPLE_EVAL                = 0;
	public static final int POSITION_BASED             = 1;
	public static final int MOBILITY_BASED             = 2;
	public static final int RANDOM_EVAL                = 3;
	
	
	public String  name                   = "noname";
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
	
}
