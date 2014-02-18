package se.noren.android.porthello.gamestates;

import java.util.List;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;
import se.noren.android.porthello.R;
import se.noren.android.porthello.jcpt.TextureRedrawer;
import se.noren.android.porthello.jcpt.TextureRedrawerListener;
import se.noren.android.porthello.net.HighScore;
import se.noren.android.porthello.net.HighScoreAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class HighscoresState extends GameState {
	
	private PlayGameState onGoingGame = null;
	
	// Lazy mans impl of a mutex to avoid multiple network calls.
	private static boolean redrawOngoing = false;

	public HighscoresState(PlayGameState pstate) {
		onGoingGame = pstate;
	}

	
	public static SimpleVector getPreferredCameraPosition() {
		return new SimpleVector(5, -8, 27);
	}

	public static SimpleVector getPreferredCameraLookAt() {
		return new SimpleVector(5, -7, 41);
	}


	/**
	 * Do proper setup of this game state before it starts
	 * executing. 
	 */
	public void initializeGameState(final GameContext context) {
		final Object3D arrow = JCPTUtils.getObjectByName("backarrow", context.world);
		SimpleVector c = arrow.getCenter();
		arrow.setRotationMatrix(new Matrix());
		arrow.setTranslationMatrix(new Matrix());
		arrow.translate(-c.x, -c.y, -c.z);
		arrow.translate(4.4f, -1.13f, 39.3f);
		arrow.rotateY((float) Math.PI * .5f);
		arrow.setVisibility(true);		
		
		drawHighScoreTable(context);	
		
    	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
    	tracker.trackPageView("/HighScoresState");
	}

	public static void forkOffHighScoreTableUpdate(final GameContext context) {
		new Thread(new Runnable() {
			public void run() {
				drawHighScoreTable(context);
			}
		}).start();
	}

	public static void drawHighScoreTable(final GameContext context) {
		
		if (!redrawOngoing) {
			redrawOngoing = true;
			SharedPreferences settings = context.activity.getSharedPreferences(ChooseOpponentState.PREFS_NAME, 0);
	        final String username = settings.getString("username", "");
	        final String usernameB64 = Base64.encodeToString(username.getBytes(), Base64.DEFAULT).trim();
			
			final List<HighScore> highScores = new HighScoreAdapter().getHighScores();
			
			final Paint paint = TextureRedrawer.getDefaultPaint(context.activity);
			final Paint personalPaint = TextureRedrawer.getPersonalScorePaint(context.activity);
			paint.setTextSize(12);
			personalPaint.setTextSize(12);
			
			new TextureRedrawer(context, "high.png", "highscores", R.drawable.high, 256, 256, paint, new TextureRedrawerListener() {
				public void drawCallback(Canvas canvas, int height, int width) {
					boolean foundPersonal = false;
					int c = 1;
					paint.setTextSize(26);
					canvas.drawText("wall of fame", 20, 30, paint);
					
					System.out.println("Total nr of highscores: " + highScores.size());
					
					paint.setTextSize(12);
					for (HighScore hs : highScores) {
						Paint p = paint;
						if (hs.getOwner().equals(username)) {
							p = personalPaint;
							foundPersonal = true;
						}
						drawHighscoreRecord(c + ". " + hs.getOwner(), hs.getScore() + " p", "lev " + hs.getLevel(), c, canvas, p);						
/*						canvas.drawText(c + ". " + hs.getOwner(), 20, 40 + c * 15, p);
						canvas.drawText(hs.getScore() + " p", 150, 40 + c * 15, p);
						canvas.drawText("lev " + hs.getLevel(), 200, 40 + c * 15, p);*/
						c++;
						
						if (c > 10) {
							canvas.drawText("... ", 20, 37 + c * 15, p);							
							break;
						}
					}
					
					if (!foundPersonal) {
						int ownScore = ownScore(context);
						int p = 1;
						for (HighScore hs : highScores) {
							if (hs.getOwner().equalsIgnoreCase(username) && hs.getScore() == ownScore) {
								
								if (ownScore == 0) {
									drawHighscoreRecord(highScores.size() + ". " + username, 
								            hs.getScore() + " p",
								            "lev " + hs.getLevel(),
								            14,
								            canvas,
								            personalPaint);
									//canvas.drawText(highScores.size() + ". " + username, 20, 40 + c * 15, personalPaint);
								} else {
									// Draw a couple of records before this one..
									System.out.println("int g = Math.max(p - 2, 11): " + Math.max(p - 2, 11));
									for (int g = Math.max(p - 2, 11); g < p; g++) {
										System.out.println("found g: " + g);
										HighScore score = highScores.get(g - 1);
										drawHighscoreRecord(g + ". " + score.getOwner(), 
												score.getScore() + " p",
									            "lev " + score.getLevel(),
									            14 + g - p,
									            canvas,
									            paint);																			
									}
									
									drawHighscoreRecord(p + ". " + username, 
								            hs.getScore() + " p",
								            "lev " + hs.getLevel(),
								            14,
								            canvas,
								            personalPaint);									
									//canvas.drawText(p + ". " + username, 20, 40 + c * 15, personalPaint);									
								}
								//canvas.drawText(hs.getScore() + " p", 150, 40 + c * 15, personalPaint);
								//canvas.drawText("lev " + hs.getLevel(), 200, 40 + c * 15, personalPaint);
								break;
							}
							p++;
						}
					}
				}
			}).draw();
			
			redrawOngoing = false;
		}
	}
	
	private static void drawHighscoreRecord(String col1, String col2, String col3, int row, Canvas canvas, Paint p) {
		canvas.drawText(col1, 20, 37 + row * 15, p);
		canvas.drawText(col2, 150, 37 + row * 15, p);
		canvas.drawText(col3, 200, 37 + row * 15, p);
	}
	
	private static int ownScore(GameContext context) {
		Intent sender = context.activity.getIntent();
		final String username = sender.getExtras().getString("username");
		final String usernameB64 = Base64.encodeToString(username.getBytes(), Base64.DEFAULT).trim();

		final SharedPreferences settings = context.activity.getSharedPreferences(ChooseOpponentState.PREFS_NAME, 0);
		int currentLevel = settings.getInt("currentLevel_" + usernameB64, 1);

		int totalScore = 0;
		for (int c = 1; c <= ChooseOpponentState.NO_OF_LEVELS; c++) {
			if (settings.contains("winsOnLevel_" + usernameB64 + c)
					&& settings.contains("totalGames_" + usernameB64 + c)
					&& settings.contains("bestScoreOnLevel_" + usernameB64 + c)) {
				
				int bestScore = settings.getInt("bestScoreOnLevel_"	+ usernameB64 + c, Integer.MIN_VALUE);
				if (bestScore == Integer.MAX_VALUE) {
					totalScore += 100;
				} else if (bestScore != Integer.MIN_VALUE) {
					totalScore += (bestScore > 0 ? bestScore : 0);
				}
			}

		}
		return totalScore;
	}
	
	@Override
	public void update(GameContext context, long dt) {

	}

	@Override
	public void handleTouchEvent(GameContext context, float x, float y) {
		String touchedObject = touchedObject(context, x, y);
		if (touchedObject != null && touchedObject.startsWith("backarrow")) {
			SimpleVector[] cameraPositions = { context.world.getCamera().getPosition(),
											   new SimpleVector(15, -10, -10),
											   IntroState.getPreferredCameraPosition() };

			SimpleVector[] lookAtPositions = { getPreferredCameraLookAt(),
												getPreferredCameraLookAt(), 
											   IntroState.getPreferredCameraLookAt() };
			
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1000, new IntroState(onGoingGame));
			context.engine.changeGameState(state);
		}
	}
}
