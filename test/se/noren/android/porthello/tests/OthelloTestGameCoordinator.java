package se.noren.android.porthello.tests;

import junit.framework.TestCase;
import se.noren.android.porthello.logic.Board;
import se.noren.android.porthello.logic.ai.OthelloAIConfig;

public class OthelloTestGameCoordinator extends TestCase  {

	public OthelloTestGameCoordinator() {
	}
	
	
	
	/*
	public void testDepthMatters() {
		OthelloAIConfig c1 = new OthelloAIConfig();
		OthelloAIConfig c2 = new OthelloAIConfig();
		c1.useMoveOrderSorting = false;
		c2.useMoveOrderSorting = false;
		c1.searchDepth = 8;
		c2.searchDepth = 2;
		
		Object[] results = firstPlayerAssumedToWin(c1, c2);
		Board b = (Board) results[0];
		
		assertTrue("Player 1 assumed to win", b.countTiles(Board.WHITE) > b.countTiles(Board.BLACK));
	}
*/
	/*
	public void testMoveOrderingMatters() {
		OthelloAIConfig c1 = new OthelloAIConfig();
		OthelloAIConfig c2 = new OthelloAIConfig();
		c1.searchDepth = 8;
		c2.searchDepth = 8;
		c1.useMoveOrderSorting = false;
		c2.useMoveOrderSorting = false;
		
		Object[] results = firstPlayerAssumedToWin(c1, c2);
		Board b = (Board) results[0];
		
		assertTrue("Player 1 assumed to win", b.countTiles(Board.WHITE) > b.countTiles(Board.BLACK));
	}
	*/
	
	public void atestPositionBasedEvalBeatsSimpleFunction() {
		OthelloAIConfig c1 = new OthelloAIConfig();
		OthelloAIConfig c2 = new OthelloAIConfig();
		c1.searchDepth = 20;
		c2.searchDepth = 20;
		c1.useMoveOrderSorting = false;
		c2.useMoveOrderSorting = false;
//		c1.useMoveOrderSorting = true;
//		c2.useMoveOrderSorting = true;
		c1.evalFunction = OthelloAIConfig.POSITION_BASED;
		c2.evalFunction = OthelloAIConfig.POSITION_BASED;
		c1.useTranspositionTables = true;
		c2.useTranspositionTables = true;
//		c1.useTranspositionTables = false;
//		c2.useTranspositionTables = false;
		c1.ignoreCacheDepth = 3;
		c2.ignoreCacheDepth = 3;
		c1.maxTime = 10000;
		c2.maxTime = 10000;
		c1.printDebugInfo = true;
		c2.printDebugInfo = true;
		
		GameResult gameResult = new OthelloTournament().playGame(c1, c2, true);
		Board b = gameResult.board;
		
		assertTrue("Player 1 assumed to win", b.countTiles(Board.WHITE) > b.countTiles(Board.BLACK));
	}
	
	public void testTourament() {
		OthelloTournament tournament = new OthelloTournament();

		for (int i = 6; i < 8; i++) {		
/*			OthelloAIConfig c1 = new OthelloAIConfig();
			c1.evalFunction = OthelloAIConfig.SIMPLE_EVAL;
			c1.searchDepth = 9;
			c1.name = "Simple" + c1.searchDepth;
			c1.maxTime = 5000;
*/			
			OthelloAIConfig c2 = new OthelloAIConfig();
			c2.evalFunction = OthelloAIConfig.POSITION_BASED;
			c2.searchDepth = i;
			c2.name = "P" + c2.searchDepth;
			c2.maxTime = 3000;

			
			for (int j = 1; j < 2; j++) {
				for (int k = 1; k < 12; k += 3) {
					OthelloAIConfig c3 = new OthelloAIConfig();
					c3.evalFunction = OthelloAIConfig.MOBILITY_BASED;
					c3.searchDepth = i;
					c3.ignoreCacheDepth = j;
					c3.maxTime = c2.maxTime;
					c3.mobilityQuantifier = k;
					c3.name = "M" + c2.searchDepth + "I" + c3.ignoreCacheDepth + "Q" + c3.mobilityQuantifier;
					tournament.addPlayer(c3);
				}
			}

			tournament.addPlayer(c2);

		}
		tournament.allAgainstAll();
	}
	

}
