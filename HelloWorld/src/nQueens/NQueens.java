package nQueens;

import java.util.*;
import java.util.concurrent.*;

public class NQueens implements Runnable {
	public static final int BOARD_SIZE = 5;
	public static final int MAX_ROWS = BOARD_SIZE / 2 + BOARD_SIZE % 2;
	public static final int DISPLAY_LIMIT = 1;
	public static final int MAX_THREADS = MAX_ROWS;
	private Set<String> solutions;
	private int start, end, n;
	private boolean addMirror;
	
	/**
	 * Constructor
	 * @param n Board size
	 * @param solutions All valid solutions as n length String
	 * @param start The first row on the first column to place a queen
	 * @param end The last row on the first column to place a queen
	 */
	NQueens(int n, Set<String> solutions, int start, int end) {
		this.solutions = solutions;
		this.start = start;
		this.end = end;
		this.n = n;
	}
	
	public static void main(String args[]) {
		Set<String> solutions = Collections.synchronizedSet(new HashSet<>());
		List<Thread> threads = new ArrayList<>();
		
		// Create as many threads as MAX_THREADS
		long startTime = System.nanoTime();
		Thread t;
		for (int i = 0; i < MAX_ROWS; i += MAX_ROWS / MAX_THREADS) {
			t = new Thread(new NQueens(BOARD_SIZE, solutions, i, Math.min(i + MAX_ROWS / MAX_THREADS, MAX_ROWS)));
			t.start();
			threads.add(t);
		}
		// Wait for all threads to finish executing
		try {
			for (int i = 0; i < threads.size(); i++) {
				threads.get(i).join();
			}
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted.");
		}
		
		// Put all solutions in a list
/*		List<List> allStr = new ArrayList<>(2500000);
		Iterator i = solutions.iterator();
		while (i.hasNext()) {
			allStr.add(getStringList((String) i.next()));
		}*/
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / 1000000;
		System.out.println("found " + solutions.size() + " solutions in " + duration + " milliseconds\n");
		
		// Print solution(s)
		Iterator i = solutions.iterator();
		int k = 0;
		while (i.hasNext() && k < DISPLAY_LIMIT) {
			String solution = (String) i.next();
			for (int r = 0; r < BOARD_SIZE; r++) {
				for (int c = 0; c < BOARD_SIZE; c++) {
					if (r == solution.charAt(c)) {
						System.out.print("Q ");
					} else {
						System.out.print(". ");
					}
				}
				System.out.println("");
			}
			System.out.println("");
			k++;
		}
		
	}
	
	public static List<String> getStringList(String solution) {
		List<String> ret = new ArrayList<>(BOARD_SIZE);
		// up to N = 30
		StringBuilder dots = new StringBuilder("..............................");
		dots.setLength(BOARD_SIZE);
		for (int i = 0; i < BOARD_SIZE; i++) {
			dots.setCharAt((int) solution.codePointAt(i), 'Q');
			ret.add(dots.toString());
			dots.setCharAt((int) solution.codePointAt(i), '.');
		}
		return ret;
	}
	
	@Override
	public void run() {
		// True indicates the index column is attacked by a queen
		boolean[] rank = new boolean[n];
		// Diagonal running SW to NE
		boolean[] dne = new boolean[2 * n];
		// Diagonal running NW to SE
		boolean[] dse = new boolean[2 * n];

		// Iterate through column between start and end in the first row
		for (int c = start; c < end; c++) {
			StringBuilder s = new StringBuilder((char) c + "");
			rank[c] = true;
			dne[c] = true;
			dse[n - c] = true;
			addMirror = !(c == n / 2 && n % 2 == 1);
			bruteForce(1, s, solutions, rank, dne, dse);
			rank[c] = false;
			dne[c] = false;
			dse[n - c] = false;
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
	private void bruteForce(int r, StringBuilder solution, Set<String> solutions, boolean[] rank, boolean[] dne, boolean[] dse) {
    	// String was chosen instead of array since HashSet does not determine uniqueness based on array elements
		if (r == n) {
			solutions.add(solution.toString());
			
			// Mirror about Y axis
			if(addMirror) {
				StringBuilder mirror = new StringBuilder(solution);
				for (int i = 0; i < n; i++) {
					mirror.setCharAt(i, (char) (n - mirror.codePointAt(i) - 1));
				}
				solutions.add(mirror.toString());
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
				solution.appendCodePoint(c);
				bruteForce(r + 1, solution, solutions, rank, dne, dse);
				rank[c] = false;
				dne[r + c] = false;
				dse[r - c + n] = false;
				solution.setLength(r);
			}
		}
    }

}