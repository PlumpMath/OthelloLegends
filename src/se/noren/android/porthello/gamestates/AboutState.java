package se.noren.android.porthello.gamestates;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class AboutState extends GameState {

	private PlayGameState onGoingGame = null;
	
	public AboutState(PlayGameState pstate) {
		onGoingGame = pstate;
	}
	
	
	public static SimpleVector getPreferredCameraPosition() {
		return new SimpleVector(-5, -10, -8.8);
	}

	public static SimpleVector getPreferredCameraLookAt() {
		return SimpleVector.create(-14.5f, -7.0f, -11);
	}
	
	/**
	 * Do proper setup of this game state before it starts
	 * executing. 
	 */
	public void initializeGameState(GameContext context) {
		final Object3D arrow = JCPTUtils.getObjectByName("backarrow", context.world);
		SimpleVector c = arrow.getCenter();
		arrow.setTranslationMatrix(new Matrix());
		arrow.setRotationMatrix(new Matrix());
		arrow.translate(-c.x, -c.y, -c.z);
		arrow.translate(-13.5f, -2.4f, -11f);
		arrow.setVisibility(true);
		
    	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
    	tracker.trackPageView("/AboutState");
	}

	@Override
	public void handleTouchEvent(GameContext context, float x, float y) {
		String touchedObject = touchedObject(context, x, y);
		if (touchedObject != null && touchedObject.startsWith("backarrow")) {
			SimpleVector[] cameraPositions = { getPreferredCameraPosition(),
											   new SimpleVector(15, -12, -16),
											   IntroState.getPreferredCameraPosition() };

			SimpleVector[] lookAtPositions = { getPreferredCameraLookAt(),	
											   getPreferredCameraLookAt(), 
											   IntroState.getPreferredCameraLookAt() };
			
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, new IntroState(onGoingGame));
			context.engine.changeGameState(state);
			
			final Object3D arrow = JCPTUtils.getObjectByName("backarrow", context.world);
			arrow.setVisibility(false);
		}
	}
}
