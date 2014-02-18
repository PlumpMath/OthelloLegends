package se.noren.android.porthello.gamestates;

import java.util.List;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;
import se.noren.android.porthello.logic.Board;

import com.threed.jpct.Object3D;

public class AnimateTilesGameState extends GameState {

	public final static long ANIM_LENGTH = 500;
	
	PlayGameState gameState = null;
	boolean humanMove = true;
	List<int[]> flippedTiles = null;
	int movex;
	int movey;
	long animStart = 0;
	long currentAnimTime = 0;
	
	public AnimateTilesGameState(GameContext context, PlayGameState gameState, boolean humanMove, List<int[]> flippedTiles, int movex, int movey) {
		this.gameState = gameState;
		this.humanMove = humanMove;
		this.flippedTiles = flippedTiles;
		this.movex = movex;
		this.movey = movey;
	}
	
	@Override
	public void initializeGameState(GameContext context) {
		animStart = System.currentTimeMillis();
		currentAnimTime = animStart;
		PlayGameState.setTile(movex, movey, humanMove ? Board.WHITE : Board.BLACK, context);
	}
	
	@Override
	public void update(GameContext context, long dt) {
		currentAnimTime += dt;
		float percentage = (currentAnimTime - animStart) / (float) ANIM_LENGTH;

		if (currentAnimTime > animStart + ANIM_LENGTH) {
			/*
			 * Stop animation, finish to 180 degrees.
			 */
			
			System.out.println("Stop animation!!!");
			context.engine.changeGameState(gameState);
			gameState.performMoveAfterTileAnimation(humanMove, movex, movey);
			percentage = 1.0f;
		}
		
		System.out.println("animation update, currentAnimTime " + currentAnimTime);
		
		for (int[] tile : flippedTiles) {
			rotateTile(tile[0], tile[1], humanMove ? Board.WHITE : Board.BLACK, context, percentage);
		}
	}

	private void rotateTile(int x, int y, int postRotationColor, GameContext context, float percentageDone) {
		
		Object3D tile = JCPTUtils.getObjectByName("gamebrick" + x + "_" + y, context.world);
		
		System.out.println("Rotating tile " + x + " " + y + " done = " + percentageDone);
		
		if (postRotationColor == Board.EMPTY) 
			return;
					
		tile.setVisibility(true);
		tile.getRotationMatrix().setIdentity();
		if (postRotationColor == Board.WHITE) {
			tile.rotateZ((float) Math.PI * percentageDone);
		} else {
			tile.rotateZ((float) Math.PI * (1.0f + percentageDone));
		}
	}	
}
