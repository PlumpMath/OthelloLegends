package se.noren.android.jcpt;

import java.util.Enumeration;

import se.noren.android.othello.R;
import android.content.res.Resources;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Object3D;
import com.threed.jpct.PolygonManager;
import com.threed.jpct.Texture;
import com.threed.jpct.World;

public class JCPTUtils {

	private static Texture font = null;
	
	public static Object3D getObjectByName(String prfix, World w) {
		Enumeration<Object3D> objects = w.getObjects();
		while (objects.hasMoreElements()) {
			Object3D o = objects.nextElement();
			if (o.getName().startsWith(prfix)) {
				return o;
			}
		}
		return null;
	}
	
	public static void printUVCoords(Object3D o) {
		PolygonManager pm = o.getPolygonManager();
		for (int i = 0; i < pm.getMaxPolygonID(); i++) {
		   for (int p = 0; p < 3; p++) {
		      System.out.println("UV: " + i + "/" + p + ": " + pm.getTextureUV(i, p));
		   }
		}

	}
	
	public static void blitNumber(int number, int x, int y, FrameBuffer fb, Resources res) {
		if (font == null) {
			font = new Texture(res.openRawResource(R.raw.numbers));
			font.setMipmap(false);
		}
		
		String sNum = Integer.toString(number);

		for (int i = 0; i < sNum.length(); i++) {
			char cNum = sNum.charAt(i);
			int iNum = cNum - 48;
			fb.blit(font, iNum * 5, 0, x, y, 5, 9, FrameBuffer.TRANSPARENT_BLITTING);
			x += 5;
		}
	}
	
	public static void setupForPicking(String name, World world) {
		Object3D obj = JCPTUtils.getObjectByName(name, world);
		obj.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		obj.strip();
		obj.build();
	}
	
	public static void removePicking(String name, World world) {
		Object3D obj = JCPTUtils.getObjectByName(name, world);
		obj.setCollisionMode(Object3D.COLLISION_CHECK_NONE);
		obj.strip();
		obj.build();
	}

}
