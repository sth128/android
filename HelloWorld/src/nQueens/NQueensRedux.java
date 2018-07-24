package nQueens;

import java.util.*;
import java.util.concurrent.*;

public class NQueensRedux implements Runnable {
	public static final int BOARD_SIZE = 15;
	public static final int MAX_ROWS = BOARD_SIZE / 2 + BOARD_SIZE % 2;
	public static final int DISPLAY_LIMIT = 1;
	public static final int MAX_THREADS = MAX_ROWS;
	private int start, end, n;
	private boolean addMirror;
	private BlockingQueue<String> q;
	private final StringBuilder letters = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	
	/**
	 * Constructor
	 * @param n Board size
	 * @param start The first row on the first column to place a queen
	 * @param end The last row on the first column to place a queen
	 * @param q the BlockingQueue used for solution output
	 */
	NQueensRedux(int n, int start, int end, BlockingQueue<String> q) {
		this.start = start;
		this.end = end;
		this.n = n;
		this.q = q;
	}
	
	public static void main(String args[]) {
		List<Thread> threads = new ArrayList<>();
		BlockingQueue<String> q = new ArrayBlockingQueue<>(500000);
		Writer writer = new Writer(q, BOARD_SIZE);
		Thread writerThread = new Thread(writer);
		long startTime = System.nanoTime();
		
		writerThread.start();
		// Create as many threads as MAX_THREADS
		for (int i = 0; i < MAX_ROWS; i += MAX_ROWS / MAX_THREADS) {
			Thread t = new Thread(new NQueensRedux(BOARD_SIZE, i, Math.min(i + MAX_ROWS / MAX_THREADS, MAX_ROWS), q));
			t.start();
			threads.add(t);
		}
		// Wait for all threads to finish executing
		try {
			for (int i = 0; i < threads.size(); i++) {
				threads.get(i).join();
			}
			q.put("EXIT");
			writerThread.join();
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted.");
		}
		
		long duration = (System.nanoTime() - startTime) / 1000000;
		System.out.println("found " + writer.getCount() + " solutions in " + duration + " milliseconds\n");
	}
	
	@Override
	public void run() {
		// True indicates the index column is attacked by a queen
		boolean[] rank = new boolean[n];
		// Diagonal running SW to NE
		boolean[] dne = new boolean[2 * n];
		// Diagonal running NW to SE
		boolean[] dse = new boolean[2 * n];
		StringBuilder s = new StringBuilder();

		// Iterate through column between start and end in the first row
		for (int c = start; c < end; c++) {
			s.append(letters.charAt(c));
			rank[c] = true;
			dne[c] = true;
			dse[n - c] = true;
			addMirror = !(c == n / 2 && n % 2 == 1);
			bruteForce(1, s, rank, dne, dse);
			rank[c] = false;
			dne[c] = false;
			dse[n - c] = false;
			s.setLength(0);
		}
	}
	
	/**
	 * Recursive algorithm to do a DFS on all solutions to n-queens
	 * @param r Row to place a queen in
	 * @param solution String representing queen positions so far. Index is row, codePoint is column
	 * @param solutions HashSet of valid solutions
	 * @param rank Attacked columns at row r not accounting for diagonals
	 * @param dne Attacked diagonal running SW to NE
	 * @param dse Attacked diagonal running NW to SE
	 */
	public void bruteForce(int r, StringBuilder solution, boolean[] rank, boolean[] dne, boolean[] dse) {
    	// String was chosen instead of array since HashSet does not determine uniqueness based on array elements
		if (r == n) {
			try {
				q.put(solution.toString());
				// Mirror about Y axis
				if(addMirror) {
					StringBuilder mirror = new StringBuilder(solution);
					for (int i = 0; i < n; i++) {
						mirror.setCharAt(i, letters.charAt(letters.charAt(n) - solution.charAt(i) - 1));
					}
					q.put(mirror.toString());
				}
			} catch (Exception e) {
			}
			
			return;
		}
    	// Go thru every column and if a queen can be placed, recurse for next row
		for (int c = 0; c < n; c++) {
			if (!rank[c] && !dne[r + c] && !dse[r - c + n]) {
				rank[c] = true;
				dne[r + c] = true;
				dse[r - c + n] = true;
				// cast c to a char and append it to the solution string
				solution.append(letters.charAt(c));
				bruteForce(r + 1, solution, rank, dne, dse);
				rank[c] = false;
				dne[r + c] = false;
				dse[r - c + n] = false;
				solution.setLength(r);
			}
		}
    }
}