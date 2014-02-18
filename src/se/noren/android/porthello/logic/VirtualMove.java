package se.noren.android.porthello.logic;

public class VirtualMove implements Comparable<VirtualMove> {
	public int  x;
	public int  y;
	public int  value;
	
	public VirtualMove(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	public int compareTo(VirtualMove another) {
		return value - another.value;
	}
}
