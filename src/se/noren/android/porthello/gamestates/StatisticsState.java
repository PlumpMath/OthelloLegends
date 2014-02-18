package se.noren.android.porthello.gamestates;

import java.util.ArrayList;
import java.util.List;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;
import se.noren.android.porthello.R;
import se.noren.android.porthello.jcpt.TextureRedrawer;
import se.noren.android.porthello.jcpt.TextureRedrawerListener;
import se.noren.android.porthello.logic.Move;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class StatisticsState extends GameState {
	
	public static List<Move> moves = new ArrayList<Move>();
	
	private PlayGameState onGoingGame = null;

	public StatisticsState(PlayGameState pstate) {
		onGoingGame = pstate;
	}
	
	
	public static SimpleVector getPreferredCameraPosition() {
		return new SimpleVector(-.8, -10, -7);
	}

	public static SimpleVector getPreferredCameraLookAt() {
		return new SimpleVector(-.5, -22.7, -8.4);
	}

	/**
	 * Do proper setup of this game state before it starts
	 * executing. 
	 */
	public void initializeGameState(GameContext context) {
		final Object3D arrow = JCPTUtils.getObjectByName("backarrow", context.world);
		SimpleVector c = arrow.getCenter();
		arrow.setRotationMatrix(new Matrix());
		arrow.setTranslationMatrix(new Matrix());
		arrow.translate(-c.x, -c.y, -c.z);
		arrow.translate(1.3f, -16.1f, -13.7f);
		arrow.rotateY((float) Math.PI * -.5f);
		arrow.setVisibility(true);		
		
		final Paint paint = TextureRedrawer.getDefaultPaint(context.activity, "fonts/commodore.ttf");
		paint.setTextSize(12);
		new TextureRedrawer(context, "stat.png", "statsbrd", R.drawable.stat, 256, 256, paint, new TextureRedrawerListener() {
			public void drawCallback(Canvas canvas, int height, int width) {
				int c = 1;
				paint.setTextSize(18);
				canvas.drawText("Computer stats", 20, 30, paint);
				
				paint.setTextSize(12);

				canvas.drawText("Move", 20, 40, paint);
				canvas.drawText("Depth", 110, 40, paint);
				canvas.drawText("Evals", 150, 40, paint);
				canvas.drawText("Estim", 205, 40, paint);

				for (int i = moves.size() - 1; i >= 0; i--) {
					Move m = moves.get(i);
					canvas.drawText(i + ". [" + m.x + "," + m.y + "]", 20, 40 + c * 15, paint);
					canvas.drawText(m.completedDepths + "", 110, 40 + c * 15, paint);
					canvas.drawText(m.totalEvals + "", 150, 40 + c * 15, paint);
					String txt = "";
					if (m.alpha == Integer.MAX_VALUE) {
						txt = "Winning!";
					} else if (m.alpha == Integer.MIN_VALUE) {
						txt = "Loosing.";
					} else {
						txt = m.alpha + "";
					}
					canvas.drawText(txt, 205, 40 + c * 15, paint);
					c++;
					
					if (c > 15) {
						break;
					}
				}
			}
		}).draw();	
		
    	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
    	tracker.trackPageView("/StatisticsState");
	}

	@Override
	public void handleTouchEvent(GameContext context, float x, float y) {
		String touchedObject = touchedObject(context, x, y);
		if (touchedObject != null && touchedObject.startsWith("backarrow")) {
			SimpleVector[] cameraPositions = { context.world.getCamera().getPosition(),
					new SimpleVector(15, -15, -20),
					IntroState.getPreferredCameraPosition()};

			SimpleVector[] lookAtPositions = { SimpleVector.ORIGIN,
					SimpleVector.ORIGIN, IntroState.getPreferredCameraLookAt() };
			
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 2000, new IntroState(onGoingGame));
			context.engine.changeGameState(state);
		}
	}
}
