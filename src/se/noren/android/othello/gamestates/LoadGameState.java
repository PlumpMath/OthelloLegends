package se.noren.android.othello.gamestates;

import se.noren.android.gameengine.GameContext;
import se.noren.android.gameengine.GameState;
import se.noren.android.jcpt.JCPTUtils;
import se.noren.android.othello.R;
import se.noren.android.othello.jcpt.ModelLoader;

import com.threed.jpct.Camera;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

public class LoadGameState extends GameState {

	private static boolean isSetup = false;
	
	/*
	 * (non-Javadoc)
	 * @see se.noren.android.gameengine.GameState#initializeGameState(se.noren.android.gameengine.GameContext)
	 */
	
	@Override
	public void initializeGameState(GameContext context) {
		if (!isSetup) {
			isSetup = true;
			setup(context);
		}
	}

	private void loadTexture(int resource, int dimension, String texName, GameContext context) {
		Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(
		          context.activity.getResources().getDrawable(resource)), dimension, dimension));
		TextureManager.getInstance().addTexture(texName, texture);
	}
	
	public void setup(GameContext context) {
		World world = context.world;
		Light sun   = context.sun;

		world.setAmbientLight(100, 100, 100);
		sun.setIntensity(255, 255, 255);

		loadTexture(R.drawable.othl, 256, "othl.png", context);
		loadTexture(R.drawable.brik, 256, "brik.jpg", context);
		loadTexture(R.drawable.blbo, 512, "blbo.png", context);
		loadTexture(R.drawable.wood, 256, "wood.jpg", context);
		loadTexture(R.drawable.brd,  128, "brd.png",  context);
		loadTexture(R.drawable.floo, 128, "floo.png", context);
		
		loadTexture(R.drawable.alph,   8, "alph.png", context);
		loadTexture(R.drawable.high, 256, "high.png", context);
		loadTexture(R.drawable.stat, 256, "stat.png", context);

		String[] separateLoadObjects = {"aboutbrd", "BoardTile", "GameBoard", "scoreboard", "lock", 
				                     "unlock", "Brick", "highscores", "backarrow",
				                     "aboutframe", "legendframe", "highscorefra", "statsbrd", "statsframe"};
		ModelLoader.load3DSModelsAsSeparateWorldObjects(context.activity, world, R.raw.othb, 1.0f, separateLoadObjects);

		JCPTUtils.setupForPicking("aboutbrd", world); 
		JCPTUtils.setupForPicking("highscores", world); 
		JCPTUtils.setupForPicking("scoreboard", world); 
		JCPTUtils.setupForPicking("statsbrd", world); 
		JCPTUtils.setupForPicking("backarrow", world); 
		
		setupTiles(world);
		
		Camera cam = world.getCamera();
		cam.setPosition(0, 100, 0);
		cam.lookAt(SimpleVector.create(0, 200, 0));

		SimpleVector sv = new SimpleVector(new SimpleVector(SimpleVector.ORIGIN));
		sv.z -= 100;
		sv.y -= 40;
		sun.setPosition(sv);
			
		MemoryHelper.compact();
	}
	



	private void setupTiles(World world) {
		/*
		 * Now create 64 tiles out of the 1 loaded.
		 */
		Object3D gameBoard = JCPTUtils.getObjectByName("GameBoard", world);
		Object3D tile = JCPTUtils.getObjectByName("BoardTile", world);
		Object3D brick = JCPTUtils.getObjectByName("Brick", world);
		SimpleVector origin = gameBoard.getOrigin();
		SimpleVector tileOrigin = tile.getOrigin();
		float tileWidth = 0.84f;
		float startx = origin.x;
		float startz = origin.z;
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				Object3D newTile = tile.cloneObject();
				newTile.setName("tile" + x + "_" + z);
				newTile.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				newTile.translate(SimpleVector.create(startx + x * tileWidth, tileOrigin.y, startz + z  * tileWidth));
				world.addObject(newTile);
				
				Object3D newBrick = brick.cloneObject();
				newBrick.setName("gamebrick" + x + "_" + z);
				newBrick.translate(SimpleVector.create(startx + x * tileWidth, tileOrigin.y + .0f, startz + z  * tileWidth));
				newBrick.setVisibility(false);
				world.addObject(newBrick);
				
			}
		}
		
		brick.setVisibility(false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see se.noren.android.gameengine.GameState#update(se.noren.android.gameengine.GameContext, se.noren.android.gameengine.GameEngineInterface, long)
	 */
	@Override
	public void update(GameContext context, long dt) {

		SimpleVector[] cameraPositions = {AboutState.getPreferredCameraPosition(), 
										  HighscoresState.getPreferredCameraPosition(), 
										  ChooseOpponentState.getPreferredCameraPosition() };
			
		SimpleVector[] lookAtPositions = {AboutState.getPreferredCameraLookAt(), 
				                          ChooseOpponentState.getPreferredCameraLookAt(), 
				                          ChooseOpponentState.getPreferredCameraLookAt()};
		
		CameraAnimationState state = new CameraAnimationState(cameraPositions, lookAtPositions, 2000, new ChooseOpponentState());
		context.engine.changeGameState(state);
	}

}
