package se.noren.android.othello.gamestates;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

public class CameraAnimationState extends GameState {

	SimpleVector[] cameraPositions = null;
	SimpleVector[] lookAtPositions = null;
	GameState      nextGameState = null;
	long           animationDuration = 0;
	long           startTime = 0;
	
	public CameraAnimationState(SimpleVector[] cameraPositions, SimpleVector[] lookAtPositions, long animationDuration, GameState nextGameState) {
		this.cameraPositions = cameraPositions;
		this.lookAtPositions = lookAtPositions;
		this.animationDuration = animationDuration;
		this.nextGameState = nextGameState;
	}
	
	/*
	 * (non-Javadoc)
	 * @see se.noren.android.gameengine.GameState#initializeGameState(se.noren.android.gameengine.GameContext)
	 */
	@Override
	public void initializeGameState(GameContext context) {
		startTime = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * @see se.noren.android.gameengine.GameState#update(se.noren.android.gameengine.GameContext, long)
	 */
	@Override
	public void update(GameContext context, long dt) {
		// TODO: Maybe make this a bezier or similar to make smoother corners.
		long time = System.currentTimeMillis() - startTime;
		if (time >= animationDuration) {
			/*
			 * Finished!
			 */

			/*
			 * Make sure we finish in correct positions!
			 */
			Camera cam = context.world.getCamera();
			cam.setPosition(cameraPositions[cameraPositions.length - 1]);
			cam.lookAt(lookAtPositions[lookAtPositions.length - 1]);

			context.engine.changeGameState(nextGameState);
		} else {
			float completed = (float) time / animationDuration;
			int camIndex  = (int) (completed * cameraPositions.length - 1);
			int lookIndex = (int) (completed * lookAtPositions.length - 1);
			
			/*
			 * First calculate position of camera.
			 * 
			 * dx is how far [0, 1] the current interpolation step has gone 
			 * (between 2 interpolation points)
			 */
			float a = camIndex / (cameraPositions.length - 1.0f);
			float b = (camIndex  + 1.0f) / (cameraPositions.length - 1.0f);
			float dx = (completed - a) / (b - a);
			
			SimpleVector p1 = cameraPositions[Math.max(0, camIndex)];
			SimpleVector p2 = cameraPositions[camIndex + 1];
			
			SimpleVector newCamPos = new SimpleVector();
			newCamPos.x = p1.x + (p2.x - p1.x) * dx;
			newCamPos.y = p1.y + (p2.y - p1.y) * dx;
			newCamPos.z = p1.z + (p2.z - p1.z) * dx;
			
			/*
			 * Now calculate position of look at.
			 * 
			 * dx is how far [0, 1] the current interpolation step has gone 
			 * (between 2 interpolation points)
			 */
			a = lookIndex / (lookAtPositions.length - 1.0f);
			b = (lookIndex  + 1.0f) / (lookAtPositions.length - 1.0f);
			dx = (completed - a) / (b - a);
			
			SimpleVector l1 = lookAtPositions[Math.max(lookIndex, 0)];
			SimpleVector l2 = lookAtPositions[Math.min(lookIndex + 1, lookAtPositions.length - 1)];
			
			SimpleVector newCamlookAt = new SimpleVector();
			newCamlookAt.x = l1.x + (l2.x - l1.x) * dx;
			newCamlookAt.y = l1.y + (l2.y - l1.y) * dx;
			newCamlookAt.z = l1.z + (l2.z - l1.z) * dx;
			
			/*
			 * Update camera
			 */
			Camera cam = context.world.getCamera();
			cam.setPosition(newCamPos);
			cam.lookAt(newCamlookAt);
			System.out.println("Interpolated - pos:" + newCamPos + "  lookat: " + newCamlookAt);
		}
		
	}

}
