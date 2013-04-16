package se.noren.android.othello.gamestates;

import java.util.ArrayList;
import java.util.List;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;
import se.noren.android.othello.bridge.OthelloRenderer;
import se.noren.android.othello.logic.Board;
import se.noren.android.othello.logic.Move;
import se.noren.android.othello.logic.Othello;
import se.noren.android.othello.logic.ai.OthelloAIConfig;
import se.noren.android.othello.logic.gamelevels.LevelConfigurator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class PlayGameState extends GameState implements OthelloRenderer {

	private int           level;
	private Othello       othello = null;
	private GameContext   context;
	private boolean       gameOver = false;
	
	/* Are we out in an animation state waiting for it to return? */
	private boolean       waitingForAnimationToComplete = false;
	
	public PlayGameState(GameContext context, int level) {
		this.level = level;
		this.context = context;
		OthelloAIConfig c = LevelConfigurator.createConfig(level);
		othello = new Othello(this, c);
    	othello.newGame();
    	
    	/*
    	 * Clear move cache
    	 */
    	StatisticsState.moves = new ArrayList<Move>();
	}

	public static SimpleVector getPreferredCameraPosition() {
		return new SimpleVector(-0.1, -10.2, -2);
	}

	public static SimpleVector getPreferredCameraLookAt() {
		return new SimpleVector(-0.1, 0, -0.5);
	}
	
	@Override
	public void initializeGameState(GameContext context) {
		final Object3D arrow = JCPTUtils.getObjectByName("backarrow", context.world);
		SimpleVector c = arrow.getCenter();
		arrow.setTranslationMatrix(new Matrix());
		arrow.setRotationMatrix(new Matrix());
		arrow.translate(-c.x, -c.y, -c.z);
		arrow.translate(0f, 0f, -5f);
		arrow.rotateX((float) Math.PI * .5f);
		arrow.rotateY((float) Math.PI * .5f);
		arrow.setVisibility(true);				
		
		JCPTUtils.removePicking("GameBoard", context.world);
		JCPTUtils.getObjectByName("GameBoard", context.world).setTransparency(-1);
		
		/*
		 * We might potentially return from animation state
		 */
		waitingForAnimationToComplete = false;
		drawGameBoard(othello.getBoard(), null);
		
	}

	
	@Override
	public void update(GameContext context, long dt) {
	}

	private void clickedTile(int x, int y) {
		System.out.println("Tile "  + x + "  " + y + " clicked");
		
		/*
		 * If this move is valid and it the human turn, we animate the flipped tiles before sending the move 
		 * into the AI.
		 */
		if (othello.humanMoveAcceptable(x, y)) {
			List<int[]> flippedTiles = othello.getBoard().flippedTilesForMove(x, y, Board.WHITE, othello.getBoard());
			AnimateTilesGameState animateState = new AnimateTilesGameState(context, this, true, flippedTiles, x, y);
			waitingForAnimationToComplete = true;
			
			/*
			 * Change game state for animation
			 */
			context.engine.changeGameState(animateState);
		}
	}
	
	/**
	 * Callback from the animation state to tell that we can continue after animation.
	 * @param humanMove
	 * @param x
	 * @param y
	 */
	public void performMoveAfterTileAnimation(boolean humanMove, int x, int y) {
		if (humanMove)
			othello.humanMove(x, y);
		
		drawGameBoard(othello.getBoard(), null);
	}
	
	@Override
	public void handleTouchEvent(GameContext context, float x, float y) {
		String touchedObject = touchedObject(context, x, y);
		if (touchedObject != null && touchedObject.startsWith("tile")) {
			int xx = Integer.parseInt(touchedObject.substring(4, 5));
			int yy = Integer.parseInt(touchedObject.substring(6));
			
			clickedTile(xx, yy);
			return;
		}
		
		if (touchedObject != null && touchedObject.startsWith("backarrow")) {
			SimpleVector[] cameraPositions = { getPreferredCameraPosition(), new SimpleVector(15, -10, -15), IntroState.getPreferredCameraPosition()};

			SimpleVector[] lookAtPositions = { getPreferredCameraLookAt(), SimpleVector.ORIGIN, IntroState.getPreferredCameraLookAt() };
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, new IntroState(this));
			context.engine.changeGameState(state);

			Object3D arrow = JCPTUtils.getObjectByName("backarrow", context.world);
			arrow.setVisibility(false);
			return;
		}

	}

	
	
	
	public static void setTile(int x, int y, int color, GameContext context) {
		Object3D tile = JCPTUtils.getObjectByName("gamebrick" + x + "_" + y, context.world);
		if (color == Board.EMPTY) {
			tile.setVisibility(false);
		} else {
			tile.setVisibility(true);
			if (color == Board.BLACK) {
				tile.getRotationMatrix().setIdentity();
			} else {
				tile.getRotationMatrix().setIdentity();
				tile.rotateZ((float) Math.PI);
			}
		}
	}
	
	
	
	
	
	
	/*****************************
	 * STUFF FROM OTHELLO ENGINE!
	 ******************************/
	
	
	public void changeProgressBarStatus(boolean show) {
		// TODO Auto-generated method stub
		
	}

	public void changeProgressBarStatusASync(boolean show) {
		// TODO Auto-generated method stub
		
	}

	public void drawGameBoard(Board b, Move move) {
		if (!waitingForAnimationToComplete) {
			System.out.println(b);
	    	for (int i = 0; i < Board.SIZEX; i++) {
	    		for (int j = 0; j < Board.SIZEY; j++) {
	    			int col = 0;
	    			if (b.board[i][j] == Board.EMPTY) {
	    				if (b.isValidMove(i, j, othello.getCurrentPlayer())) {
	        			//	col = R.drawable.empty_possible_small;    					
	    				} else {
	        				col = Board.EMPTY;			    					
	    				}
	    			} else {
	    				col = b.board[i][j];
	    			}
	    			setTile(i, j, col, context);
	    		}
	    	}
		}
	}

	public void drawGameBoardASync(Board b, Move move) {
		drawGameBoard(b, move);
	}

	public void showGameOverInformation(String info, Board board) {

		int diff = board.countTiles(Board.WHITE) - board.countTiles(Board.BLACK);
		
		/*
		 * Check for perfect game!
		 */
		if (board.countTiles(Board.BLACK) == 0) {
			diff = Integer.MAX_VALUE;
		}
	
		updateSavedStats(level, diff);
		
		SimpleVector[] cameraPositions = {context.world.getCamera().getPosition(), new SimpleVector(30, -10, 5), ChooseOpponentState.getPreferredCameraPosition()};
		SimpleVector[] lookAtPositions = {SimpleVector.ORIGIN, ChooseOpponentState.getPreferredCameraLookAt()};
		CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 2000, new ChooseOpponentState());
		context.engine.changeGameState(state);
		
		System.out.println("ASking for change to ChooseOpponent");
		
		gameOver = true;
	}

	
	public void updateProgressBarAsync(int prgress) {
		// TODO Auto-generated method stub
		
	}
	
	
	
    private void updateSavedStats(int level, int score) {
        Intent sender = context.activity.getIntent();
        final String username = sender.getExtras().getString("username");
        final String usernameB64 = Base64.encodeToString(username.getBytes(), Base64.DEFAULT).trim();
        System.out.println("b64: " + usernameB64 + ".");
    	
        SharedPreferences settings = context.activity.getSharedPreferences(ChooseOpponentState.PREFS_NAME, 0);
        Editor edit = settings.edit();
        
    	int currLevel = settings.getInt("currentLevel_" + usernameB64, 1);
    	if (level == currLevel && score > 0) {
    		currLevel++;
    		edit.putInt("currentLevel_" + usernameB64, currLevel);
    	}
    	
    	int wins = settings.getInt("winsOnLevel_" + usernameB64 + level, 0);
    	if (score > 0) {
        	wins++;
    	} 
    	edit.putInt("winsOnLevel_" + usernameB64 + level, wins);

       	int totalGames = settings.getInt("totalGames_" + usernameB64 + level, 0);
       	totalGames++;
       	edit.putInt("totalGames_" + usernameB64 + level, totalGames);
    	 
		int bestScore = settings.getInt("bestScoreOnLevel_" + usernameB64 + level, Integer.MIN_VALUE);
		if (score > bestScore) {
	       	edit.putInt("bestScoreOnLevel_" + usernameB64 + level, score);	
		}
		
		/*
		 * Set the rest of the levels values as well
		 */
		for (int i = currLevel + 1; i <= ChooseOpponentState.NO_OF_LEVELS ; i++) {
			edit.putInt("totalGames_" + usernameB64 + i, 0);
	    	edit.putInt("winsOnLevel_" + usernameB64 + i, 0);
	       	edit.putInt("bestScoreOnLevel_" + usernameB64 + i, Integer.MIN_VALUE);	
		}
			
		
		edit.commit();
		
    	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
    	tracker.trackEvent("OthelloLegends", "GameFinished", "Level_" + level, score);
    	tracker.dispatch();

    }

    /**
     * Callback from the AI
     */
	public void moveMade(Move m, Board preMoveBoard) {
		System.out.println("move made");
		StatisticsState.moves.add(m);

		if (!gameOver) {
			/*
			 * Create animation sequence for computer move.
			 * Try to undo
			 */
			List<int[]> flippedTiles = preMoveBoard.flippedTilesForMove(m.x, m.y, Board.BLACK, preMoveBoard);
			AnimateTilesGameState animateState = new AnimateTilesGameState(context, this, false, flippedTiles, m.x, m.y);
			waitingForAnimationToComplete = true;
			
			
			/*
			 * Change game state for animation
			 */
			context.engine.changeGameState(animateState);
	
			/*
			 * Potential place to sleep thread to wait for animation.
			 */
			try {
				Thread.sleep(AnimateTilesGameState.ANIM_LENGTH);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
	
}
