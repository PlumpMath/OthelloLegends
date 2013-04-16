package se.noren.android.othello.gamestates;

import java.util.Enumeration;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameEngineInterface;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;
import se.noren.android.othello.R;
import se.noren.android.othello.jcpt.TextureRedrawer;
import se.noren.android.othello.jcpt.TextureRedrawerListener;
import se.noren.android.othello.net.HighScoreAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class ChooseOpponentState extends GameState {
	
	public static String currentUserName = "";
	
	private int currentLevel = 1;
	
	public static final String PREFS_NAME = "OthelloLegendsPrefs";
	public static final int NO_OF_LEVELS = 7;
	

	
	public static SimpleVector getPreferredCameraPosition() {
		return new SimpleVector(-2, -12, 22);
	}

	public static SimpleVector getPreferredCameraLookAt() {
		return new SimpleVector(-10, -12, 22);
	}

	
	@Override
	public void initializeGameState(final GameContext context) {

        Intent sender = context.activity.getIntent();
        final String username = sender.getExtras().getString("username");
        final String usernameB64 = Base64.encodeToString(username.getBytes(), Base64.DEFAULT).trim();

		
		/*
		 * First remove all old locks
		 */
		boolean found = false;
		do {
			found = false;
			Enumeration<Object3D> objects = context.world.getObjects();
			while (!found && objects.hasMoreElements()) {
				Object3D o = objects.nextElement();
				if (o.getName().startsWith("levelunlock_") || o.getName().startsWith("levellock_")) {
					context.world.removeObject(o);
					found = true;
				}
			}
		} while (found);

        final SharedPreferences settings = context.activity.getSharedPreferences(PREFS_NAME, 0);
        currentLevel = settings.getInt("currentLevel_" + usernameB64, 1);
        System.out.println("Current level: " + currentLevel);

		final Object3D lock = JCPTUtils.getObjectByName("lock", context.world);
		final Object3D unlock = JCPTUtils.getObjectByName("unlock", context.world);
				
		final Paint paint = TextureRedrawer.getDefaultPaint(context.activity);
		new TextureRedrawer(context, "blbo.png", "scoreboard", R.drawable.blbo, 512, 512, paint, new TextureRedrawerListener() {
			public void drawCallback(Canvas canvas, int height, int width) {
				
				int totalScore = 0;
		        for (int c = 1; c <= NO_OF_LEVELS ; c++) {
		        	String bestTxt = "";
		        	if (settings.contains("winsOnLevel_" + usernameB64 + c) && 
		        		settings.contains("totalGames_" + usernameB64 + c) &&
		        		settings.contains("bestScoreOnLevel_" + usernameB64 + c)
		        		) {
		        		int bestScore = settings.getInt("bestScoreOnLevel_" + usernameB64 + c, Integer.MIN_VALUE);
		        		if (bestScore == Integer.MAX_VALUE) {
		        			bestTxt = "Perfect!";
		        			totalScore += 100;
		        		} else if (bestScore == Integer.MIN_VALUE) {
		        			bestTxt = "None played!";
		        		} else {
		        			bestTxt = (bestScore > 0 ? "+" : "") + bestScore + "p";
		        			totalScore += (bestScore > 0 ? bestScore : 0);
		        		}        		
		        	} else {
		        		bestTxt = "None played";
		        	}

		        	canvas.drawText(bestTxt, 330, 136 + (NO_OF_LEVELS - c) * 57, paint);
		        	
					if (c < currentLevel) {
						Object3D newUnLock = unlock.cloneObject();
						newUnLock.setName("levelunlock_" + c);
						newUnLock.translate(7.0f, c * -1.3f, 0);
						context.world.addObject(newUnLock);
						newUnLock.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
					}
					if (c == currentLevel) {
						Object3D newLock = lock.cloneObject();
						newLock.setName("levellock_" + c);
						newLock.translate(7.0f, c * -1.3f, 0);				
						context.world.addObject(newLock);
						newLock.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
									
					}
		        }
		        
		        canvas.drawText(totalScore + "p", 207, 74, paint);
				
		        
		        final int score = totalScore;
		        new Thread(new Runnable() {
					public void run() {
						HighScoreAdapter highScoreAdapter = new HighScoreAdapter();
						highScoreAdapter.postHighScore(username, score, currentLevel);
					}
				}).start();
			}
		}).draw();	
		
    	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
    	tracker.trackPageView("/ChooseOpponentState");
	}
	
	
	
	public void update(GameContext context, GameEngineInterface gameEngine, long dt) {
		World world = context.world;
		
		// Animate the locks
		long millis = System.currentTimeMillis();
		int trans = (int) ((Math.sin(2.0 * Math.PI * millis / 2000.0) + 1) * 4.0) + 9;
		
		setLockTransparency(world, trans);
	}

	private void setLockTransparency(World world, int transp) {
		for (int i = 1; i < currentLevel; i++) {
			Object3D lock = JCPTUtils.getObjectByName("levelunlock_" + i, world);
			if (lock != null) {
				lock.setTransparency(transp);
			} else {
				System.out.println("Wtf1?");
			}
		}
		Object3D lock = JCPTUtils.getObjectByName("levellock_" + currentLevel, world);
		if (lock != null) {
			lock.setTransparency(transp);
		} else {
			System.out.println("Wtf1?");
		}
	}
	
	public void handleTouchEvent(GameContext context, float x, float y) {
		String touchedObject = touchedObject(context, x, y);
		if (touchedObject != null && (touchedObject.startsWith("levelunlock_") || touchedObject.startsWith("levellock_"))) {
			int level = Integer.parseInt(touchedObject.substring(touchedObject.indexOf("_") + 1).substring(0, 1));	
			SimpleVector[] cameraPositions = {context.world.getCamera().getPosition(), new SimpleVector(30, -10, 5), new SimpleVector(-0.1, -10.2, -2)};
			SimpleVector[] lookAtPositions = {JCPTUtils.getObjectByName("scoreboard", context.world).getCenter(), new SimpleVector(-0.1, 0, -0.5)};
			CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 1500, new PlayGameState(context, level));
			context.engine.changeGameState(state);			
			setLockTransparency(context.world, -1);
		}
	}
}
