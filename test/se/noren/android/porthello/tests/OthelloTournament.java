package se.noren.android.porthello.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.noren.android.porthello.bridge.OthelloRenderer;
import se.noren.android.porthello.logic.Board;
import se.noren.android.porthello.logic.Move;
import se.noren.android.porthello.logic.Othello;
import se.noren.android.porthello.logic.ai.OthelloAIConfig;

public class OthelloTournament implements OthelloRenderer {

	List<OthelloAIConfig> players = new ArrayList<OthelloAIConfig>();
	Map<OthelloAIConfig, Integer> pointMap = new HashMap<OthelloAIConfig, Integer>();
	
	public OthelloTournament() {
	}
	
	
	public void addPlayer(OthelloAIConfig p) {
		System.out.println(p.name + " registrered.");
		players.add(p);
	}
	
	private void addPoints(OthelloAIConfig c, int points) {
		if (pointMap.containsKey(c)) {
			pointMap.put(c, pointMap.get(c) + points);
		} else {
			pointMap.put(c, points);
		}
	}
	
	public void allAgainstAll() {
		for (int i = 0; i < players.size(); i++) {
			for (int j = i + 1; j < players.size(); j++) {
				OthelloAIConfig p1 = players.get(i);
				OthelloAIConfig p2 = players.get(j);
				System.out.println();
				System.out.println(p1.name + " versus " + p2.name + ". Starting home game...");
				// HOME GAME
				GameResult gameResult = playGame(p1, p2, false);
				Board b = gameResult.board;
				int bl = b.countTiles(Board.BLACK);
				int wh = b.countTiles(Board.WHITE);
				
				if (bl > wh) {
					addPoints(p2, 2);
				} else if (bl < wh) {
					addPoints(p1, 2);
				} else {
					addPoints(p1, 1);
					addPoints(p2, 1);
				}
				System.out.println(p1.name + ": " + wh + "\t" + p2.name + ": " + bl);
				printStats(gameResult);
				

				// AWAY GAME
				System.out.println(p1.name + " versus " + p2.name + ". Starting away game...");
				GameResult gameResult2 = playGame(p2, p1, false);
				b = gameResult2.board;
				bl = b.countTiles(Board.BLACK);
				wh = b.countTiles(Board.WHITE);
				if (bl > wh) {
					addPoints(p1, 2);
				} else if (bl < wh) {
					addPoints(p2, 2);
				} else {
					addPoints(p1, 1);
					addPoints(p2, 1);
				}
				System.out.println(p1.name + ": " + bl + "\t" + p2.name + ": " + wh);
				printStats(gameResult);
			}
		}
		
		System.out.println("------------------------------------");
		System.out.println("Tournament completed - score sheet");
		for (OthelloAIConfig p1 : players) {
			System.out.println(p1.name + ": \t" + pointMap.get(p1));
		}
	}
	
	private void printStats(GameResult gameResult) {
		int[] completedDepths        = {0, 0};
		int[] evalsOfCompletedDepth  = {0, 0};
		long[] timeForCompletedEvals = {0, 0};
		int[] totalEvals             = {0, 0};
		long[] totalTime             = {0, 0};
		long[] moves                 = {0, 0};

		for (Move m : gameResult.moves) {
			completedDepths[s(m.color)] += m.completedDepths;
			evalsOfCompletedDepth[s(m.color)] += m.evalsOfCompletedDepth;
			timeForCompletedEvals[s(m.color)] += m.timeForCompletedEvals;
			totalEvals[s(m.color)] += m.totalEvals;
			totalTime[s(m.color)] += m.totalTime;
			moves[s(m.color)]++;
		}
		
		System.out.println("WHITE  -  BLACK");
		System.out.println("Completed depths/move:      " + completedDepths[0] / (float) moves[0] + "\t\t" + completedDepths[1] / (float) moves[1]);
		System.out.println("Evals of compl depths/move: " + evalsOfCompletedDepth[0] / moves[0] + "\t\t" + evalsOfCompletedDepth[1] / moves[1]);
		System.out.println("Time  of compl depths/move: " + timeForCompletedEvals[0] / moves[0] + "\t\t" + timeForCompletedEvals[1] / moves[1]);
		System.out.println("Total evals/move:           " + totalEvals[0] / moves[0] + "\t\t" +  totalEvals[1] / moves[1] );
		System.out.println("Total time/move:            " + totalTime[0] / moves[0] + "\t\t" + totalTime[1] / moves[1]);
		
		
	}


	private int s(int n) {
		if (n < 0)
			return 0;
		return 1;
	}

	/**
	 * @param args
	 * @return [board, list of moves]
	 */
	public GameResult playGame(OthelloAIConfig p1, OthelloAIConfig p2, boolean verbose) {
		List<Move> allMoves = new ArrayList<Move>();
		Othello p1white = new Othello(this, p1);
		Othello p2black = new Othello(this, p2);
		p1white.setCompColor(Board.WHITE);
		p2black.setCompColor(Board.WHITE);
		Board   b  = new Board();
		b.resetBoard(false);
		
		p1white.setBoard(b.copy());
		p2black.setBoard(b.copy());
		p2black.getBoard().resetBoard(true);
		
		
		while (b.canMakeMove(Board.BLACK) || b.canMakeMove(Board.WHITE)) {
			p1white.setCurrentPlayer(p1white.getCompColor());
			List<Move> moves = p1white.computerMove();
			for (Move m : moves) {
				p2black.getBoard().performMove(m.x, m.y, p2black.getHumanColor());
				b.performMove(m.x, m.y, Board.WHITE);
				if (verbose) {
					System.out.println("Player 1 (white) plays [" + m.x + ", " + m.y + "] - stage " + b.countAllTiles() + "   W: " + b.countTiles(Board.WHITE) + " B: " + b.countTiles(Board.BLACK));
					System.out.println(b);
				}
				m.color = Board.WHITE;
				allMoves.add(m);
			}
			
			if (b.canMakeMove(Board.BLACK)) {
				p2black.setCurrentPlayer(p2black.getCompColor());
				moves = p2black.computerMove();
				for (Move m : moves) {
					p1white.getBoard().performMove(m.x, m.y, p1white.getHumanColor());
					b.performMove(m.x, m.y, Board.BLACK);
					if (verbose) {
						System.out.println("Player 2 (black) plays [" + m.x + ", " + m.y + "] - stage " + b.countAllTiles() + "   W: " + b.countTiles(Board.WHITE) + " B: " + b.countTiles(Board.BLACK));
						System.out.println(b);
					}
					m.color = Board.BLACK;
					allMoves.add(m);
				}
			}
		}
		
		GameResult gameResult = new GameResult();
		gameResult.board = b;
		gameResult.moves = allMoves;
		
		return gameResult;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void changeProgressBarStatus(boolean show) {
	}

	public void changeProgressBarStatusASync(boolean show) {
	}

	public void drawGameBoard(Board b, Move m) {
//		System.out.println(b);
	}

	public void drawGameBoardASync(Board b, Move m) {
//		drawGameBoard(b);
	}

	public void showGameOverInformation(String info, Board b) {
		System.out.println(info);
	}

	public void updateProgressBarAsync(int prgress) {		
	}


	public void moveMade(Move m, Board oldBoard) {
		// TODO Auto-generated method stub
		
	}

}
