package se.noren.android.porthello.gamestates;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class IntroState extends GameState {

	private PlayGameState onGoingGame = null;
	
	private static boolean oneTimeUpdateOfHighScores = false;
	
	public IntroState(PlayGameState pstate) {
		onGoingGame = pstate;
	}		

	@Override
	public void initializeGameState(GameContext context) {
		if (onGoingGame != null) {
			JCPTUtils.setupForPicking("GameBoard", context.world); 
		}
		Object3D arrow = JCPTUtils.getObjectByName("backarrow", context.world);
		arrow.setVisibility(false);
	}
	
	@Override
	public void update(GameContext context, long dt) {
		
		if (!oneTimeUpdateOfHighScores) {
			// Preload highscore table
			oneTimeUpdateOfHighScores = true;
			HighscoresState.forkOffHighScoreTableUpdate(context);
		}
		
		World world = context.world;
		
		/*
		 * Transparency animation
		 */
		Object3D aboutframe = JCPTUtils.getObjectByName("aboutframe", world);
		Object3D legendframe = JCPTUtils.getObjectByName("legendframe", world);
		Object3D highscoreframe = JCPTUtils.getObjectByName("highscorefra", world);
		Object3D statsframe = JCPTUtils.getObjectByName("statsframe", world);
	
		long millis = System.currentTimeMillis();
		int trans = (int) ((Math.sin(2.0 * Math.PI * millis / 2000.0) + 1) * 4.0) + 9;
		aboutframe.setTransparency(trans);
		legendframe.setTransparency(trans);
		highscoreframe.setTransparency(trans);
		statsframe.setTransparency(trans);
		
		/*
		 * If we have a current game!
		 */
		if (onGoingGame != null) {
			Object3D gameBoard = JCPTUtils.getObjectByName("GameBoard", world);
			gameBoard.setTransparency(trans);			
		} 

	}

	public static SimpleVector getPreferredCameraPosition() {
		return SimpleVector.create(25, -7, -14);
	}

	public static SimpleVector getPreferredCameraLookAt() {
		return SimpleVector.create(0, -7.0f, 5f);
	}

	
	private void opaqueFrames(World world) {
		JCPTUtils.getObjectByName("aboutframe", world).setTransparency(-1);
		JCPTUtils.getObjectByName("legendframe", world).setTransparency(-1);
		JCPTUtils.getObjectByName("highscorefra", world).setTransparency(-1);
		JCPTUtils.getObjectByName("statsframe", world).setTransparency(-1);
	}
	
	@Override
	public void handleTouchEvent(GameContext context, float x, float y) {
		String touchedObject = touchedObject(context, x, y);
		
		if (touchedObject != null && touchedObject.startsWith("aboutbrd")) {
			/*
			 * Go to next game state!
			 */
			SimpleVector[] cameraPositions = {context.world.getCamera().getPosition(), new SimpleVector(30, -10, 5), AboutState.getPreferredCameraPosition()};
			SimpleVector[] lookAtPositions = {JCPTUtils.getObjectByName("aboutbrd", context.world).getOrigin(), AboutState.getPreferredCameraLookAt()};
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, new AboutState(onGoingGame));
			context.engine.changeGameState(state);
			opaqueFrames(context.world);
			return;
		}
		
		if (touchedObject != null && touchedObject.startsWith("scoreboard")) {
			/*
			 * Go to next game state!
			 */
			SimpleVector[] cameraPositions = {context.world.getCamera().getPosition(), new SimpleVector(30, -10, 5), ChooseOpponentState.getPreferredCameraPosition()};
			SimpleVector[] lookAtPositions = {JCPTUtils.getObjectByName("aboutbrd", context.world).getOrigin(), ChooseOpponentState.getPreferredCameraLookAt()};
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, new ChooseOpponentState());
			context.engine.changeGameState(state);
			opaqueFrames(context.world);
			return;
		}
		
		if (touchedObject != null && touchedObject.startsWith("highscores")) {
			/*
			 * Go to next game state!
			 */
			SimpleVector[] cameraPositions = {context.world.getCamera().getPosition(), new SimpleVector(30, -10, 5), HighscoresState.getPreferredCameraPosition()};
			SimpleVector[] lookAtPositions = {JCPTUtils.getObjectByName("aboutbrd", context.world).getOrigin(), 
												HighscoresState.getPreferredCameraLookAt()};
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, new HighscoresState(onGoingGame));
			context.engine.changeGameState(state);			
			opaqueFrames(context.world);
			return;
		}

		if (touchedObject != null && touchedObject.startsWith("statsbrd")) {
			/*
			 * Go to next game state!
			 */
			SimpleVector[] cameraPositions = {context.world.getCamera().getPosition(), new SimpleVector(30, -10, 5), StatisticsState.getPreferredCameraPosition()};
			SimpleVector[] lookAtPositions = {JCPTUtils.getObjectByName("aboutbrd", context.world).getOrigin(), StatisticsState.getPreferredCameraLookAt()};
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, new StatisticsState(onGoingGame));
			context.engine.changeGameState(state);			
			opaqueFrames(context.world);
			return;
		}

		
		if (touchedObject != null && touchedObject.startsWith("GameBoard") && onGoingGame != null) {
			/*
			 * Resume game!
			 */
			SimpleVector[] cameraPositions = {context.world.getCamera().getPosition(), new SimpleVector(30, -10, 5), PlayGameState.getPreferredCameraPosition()};
			SimpleVector[] lookAtPositions = {JCPTUtils.getObjectByName("scoreboard", context.world).getCenter(), PlayGameState.getPreferredCameraLookAt()};
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, onGoingGame);
			context.engine.changeGameState(state);
			opaqueFrames(context.world);
			return;
		}
	}
}
