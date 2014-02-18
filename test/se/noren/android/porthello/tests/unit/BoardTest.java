package se.noren.android.porthello.tests.unit;

import junit.framework.TestCase;
import se.noren.android.porthello.logic.Board;

public class BoardTest extends TestCase  {
	
	public void testGetSet() {
		Board b = new Board();
		b.resetBoard(false);
		assertEquals(b.get(0, 0), Board.EMPTY);
		assertEquals(b.get(3, 3), Board.BLACK);
		assertEquals(b.get(4, 4), Board.BLACK);
		assertEquals(b.get(3, 4), Board.WHITE);
		assertEquals(b.get(4, 3), Board.WHITE);
		
		b.put(Board.WHITE, 2, 2);
		assertEquals(b.get(2, 2), Board.WHITE);		
	}
	
	public void testIsValidMove() {
		Board b = new Board();
		b.resetBoard(false);
		assertTrue(b.isValidMove(5, 3, Board.BLACK));
		assertFalse(b.isValidMove(5, 3, Board.WHITE));
		
		assertFalse(b.isValidMove(0, 0, Board.WHITE));
		assertFalse(b.isValidMove(0, 0, Board.BLACK));
	}
	
	public void testPerformMove() {
		Board b = new Board();
		b.resetBoard(false);
		b.performMove(5, 3, Board.BLACK);
		assertEquals(b.get(3, 3), Board.BLACK);
		assertEquals(b.get(4, 4), Board.BLACK);
		assertEquals(b.get(3, 4), Board.WHITE);
		assertEquals(b.get(4, 3), Board.BLACK);
		assertEquals(b.get(5, 3), Board.BLACK);
	}
	
	public void testCountAllTiles() {
		Board b = new Board();
		b.resetBoard(false);
		b.performMove(5, 3, Board.BLACK);
		assertEquals(b.countAllTiles(), 5);
	}
	
	public void testCountTiles() {
		Board b = new Board();
		b.resetBoard(false);
		b.performMove(5, 3, Board.BLACK);
		assertEquals(b.countTiles(Board.BLACK), 4);
		assertEquals(b.countTiles(Board.WHITE), 1);
	}
	
	public void testCopy() {
		Board b = new Board();
		b.resetBoard(false);
		Board copy = b.copy();
		b.performMove(5, 3, Board.BLACK);
		assertEquals(b.countTiles(Board.BLACK), 4);
		assertEquals(b.countTiles(Board.WHITE), 1);
		assertEquals(copy.countTiles(Board.BLACK), 2);
		assertEquals(copy.countTiles(Board.WHITE), 2);		
	}

	public void testHashCode() {
		Board b = new Board();
		b.resetBoard(false);
		Board copy = b.copy();
		assertTrue(b.hashCode() == copy.hashCode());
		b.performMove(5, 3, Board.BLACK);
		assertTrue(b.hashCode() != copy.hashCode());
	}

	public void testEqual() {
		Board b = new Board();
		Board copy = b.copy();
		assertTrue(b != copy);
		assertTrue(b.equals(copy));
	}

}
