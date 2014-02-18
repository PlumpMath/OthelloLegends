package se.noren.android.porthello.logic;

import java.util.ArrayList;
import java.util.List;

import se.noren.android.porthello.bridge.OthelloRenderer;
import se.noren.android.porthello.logic.ai.OthelloAI;
import se.noren.android.porthello.logic.ai.OthelloAIConfig;

/**
 * Keep state of Othello game.
 * @author Johan
 */
public class Othello {

	Board           board         = null;
	OthelloRenderer renderer      = null;
	int             humanColor    = Board.WHITE;
	int 			compColor     = -humanColor;
	int				currentPlayer = Board.WHITE;
	OthelloAIConfig config        = null;
	
	boolean         gameOver      = false;
	
	public Othello(OthelloRenderer renderer, OthelloAIConfig conf) {
		this.renderer = renderer;
		config = conf;
	}
	
    public void newGame() {
    	board = new Board();
    	board.resetBoard(false);
    	renderer.drawGameBoard(board, null);
    	gameOver = false;
    }
	
    /**
     * Returns a list of coordinates of moves the computer has done.
     * @return
     */
    public List<Move> computerMove() {
    	List<Move> moveList = new ArrayList<Move>();
    	Board oldBoard = board.copy();
    	
    	while (!gameOver && currentPlayer == compColor) {

    		// Do actual AI
        	OthelloAI othelloAI = new OthelloAI(renderer, config);
        	Move move = othelloAI.alphabeta(board, compColor);
        	int x = move.x;
        	int y = move.y;
        	board.performMove(x, y, compColor);
    		moveList.add(move);
        	
    		if (board.canMakeMove(humanColor)) {
    			currentPlayer = humanColor;
    		} else {
    			if (!board.canMakeMove(compColor)) {
    				currentPlayer = Board.EMPTY;
    				gameOver = true;
    	    		renderer.showGameOverInformation("Game over!", board);		    				
    			}
    		}

    		renderer.moveMade(move, oldBoard);
    		renderer.drawGameBoardASync(board, move);
    	}
    	
    	return moveList;
    }
    
    private void computerAsyncMove() {
    	
    	renderer.changeProgressBarStatus(true);
    	
    	Runnable runnable = new Runnable() {
			public void run() {				
				computerMove();
		    	renderer.changeProgressBarStatusASync(false);
			}
		};
		new Thread(runnable).start();
    	

    }

    /**
     * @param x
     * @param y
     * @return Is a human move ok at this state of the game?
     */
    public boolean humanMoveAcceptable(int x, int y) {
    	if (currentPlayer != humanColor)
    		return false;
    	
    	if (!board.isValidMove(x, y, humanColor))
    		return false;
    	
    	return true;
    }

    
    
    public boolean humanMove(int x, int y) {
    	if (currentPlayer != humanColor)
    		return false;
    	
    	if (!board.isValidMove(x, y, humanColor))
    		return false;
    	
    	board.performMove(x, y, humanColor);

    	if (board.canMakeMove(compColor)) {
    		currentPlayer = compColor;
    	} else if (board.canMakeMove(humanColor)) {
    		currentPlayer = humanColor;    		
    	} else {
    		currentPlayer = Board.EMPTY;    
    		gameOver = true;
    	}
    	
    	renderer.drawGameBoard(board, null);
    	
    	if (currentPlayer == compColor) 
    		computerAsyncMove();
    	
    	if (gameOver) {
    		renderer.showGameOverInformation("Game over!", board);		    				
    	}
    	
    	return true;
    }
	
	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public int getHumanColor() {
		return humanColor;
	}

	public void setHumanColor(int humanColor) {
		this.humanColor = humanColor;
		this.compColor  =  -humanColor;
	}

	public int getCompColor() {
		return compColor;
	}

	public void setCompColor(int compColor) {
		this.compColor  = compColor;
		this.humanColor = -compColor;
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(int currentPlayer) {
		this.currentPlayer = currentPlayer;
	}
	
}
