/*
 *  Copyright (C) 2021 Pietro Di Lena
 *  
 *  This file is part of the MNKGame v2.0 software developed for the
 *  students of the course "Algoritmi e Strutture di Dati" first 
 *  cycle degree/bachelor in Computer Science, University of Bologna
 *  A.Y. 2020-2021.
 *
 *  MNKGame is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This  is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this file.  If not, see <https://www.gnu.org/licenses/>.
 */

package mnkgame;

import java.util.Random;
import java.util.*;

/**
 * Software player only a bit smarter than random.
 * <p>
 * It can detect a single-move win or loss. In all the other cases behaves
 * randomly.
 * </p>
 */
public class SignoraCarla implements MNKPlayer  {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
	private long startingTime;
	private double powLimit;

	private Set<SavedStates> winStates = new HashSet<SavedStates> (); 
	private Set<SavedStates> loseStates = new HashSet<SavedStates> (); 

	/**
	 * Default empty constructor
	 */
	public SignoraCarla() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		// New random seed for each game
		rand = new Random(System.currentTimeMillis());
		B = new MNKBoard(M, N, K);
		myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
		yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
		TIMEOUT = timeout_in_secs;
		startingTime = System.currentTimeMillis();
		powLimit = timeout_in_secs * Math.pow(10, 9);
		
	}

	/**
	 * Selects a position among those listed in the <code>FC</code> array.
	 * <p>
	 * Selects a winning cell (if any) from <code>FC</code>, otherwise
	 * selects a cell (if any) that prevents the adversary to win
	 * with his next move. If both previous cases do not apply, selects
	 * a random cell in <code>FC</code>.
	 * </p>
	 */
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		// MNKCell c = new MNKCell(0, 0);
		System.out.println("--Prima parte (controllo se posso vincere in una mossa)--");
		startingTime = System.currentTimeMillis();
		if (MC.length > 0) {
			MNKCell c = MC[MC.length - 1]; // Recover the last move from MC
			B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
			// System.out.println("c.i : " + c.i + ", c.j : " + c.j);
		}
		// If there is just one possible move, return immediately
		if (FC.length == 1) {
			B.markCell(FC[0].i, FC[0].j);
			return FC[0];
		}
		// Check whether there is single move win
		for (MNKCell d : FC) {
			// If time is running out, select a random cell
			if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (99.0 / 100.0)) {
				System.out.println("Running out of time!");
				MNKCell c = FC[rand.nextInt(FC.length)];
				// System.out.println("mark "+c.i+", "+c.j);
				B.markCell(c.i, c.j);
				return c;
			} else if (B.markCell(d.i, d.j) == myWin) {
				System.out.println("myWin");
				return d;
			} else {
				System.out.println("Unmark");
				B.unmarkCell();
			}
		}
		System.out.println("--Seconda parte (controllo se l'avversario vince in una mossa)--");
		// Check whether there is a single move loss:
		// 1. mark a random position
		// 2. check whether the adversary can win
		// 3. if he can win, select his winning position
		int pos = rand.nextInt(FC.length);
		MNKCell c = FC[0]; // random move
		B.markCell(c.i, c.j); // mark the random position
		for (int k = 1; k < FC.length; k++) {
			// If time is running out, return the randomly selected cell
			if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT*10 * (99.0 / 100.0)) {
				System.out.println("Running out of time!");
				return c;
			} else {
				MNKCell d = FC[k];
				if (B.markCell(d.i, d.j) == yourWin) {
					System.out.println("yourWin");
					B.unmarkCell(); // undo adversary move
					B.unmarkCell(); // undo my move
					B.markCell(d.i, d.j); // select his winning position
					return d; // return his winning position
				} else {
					System.out.println("Unmark");
					B.unmarkCell(); // undo adversary move to try a new one
				}
			}
		}
		B.unmarkCell();
		// check if adversary would win in FC[0], marking FC[1] as our move
		c = FC[1];
		B.markCell(c.i, c.j);
		MNKCell d = FC[0];
		if (B.markCell(d.i, d.j) == yourWin) {
			B.unmarkCell();
			B.unmarkCell();
			B.markCell(d.i, d.j);
		}else{
			B.unmarkCell();
		}
		B.unmarkCell();

		System.out.println("--Terza parte (alphaBeta)--");
		
		int maxDepth = GetMaxDepth(FC.length);
		int move = 0, bestScore = Integer.MIN_VALUE, score = bestScore;
		for (int i = 0; i < B.getFreeCells().length;) {

			if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (99.0 / 100.0)) { // se ho sforato il
																									// timeout
				// DEBUG OUTPUT
				 System.out.println("OVERTIME EVITATO");
				break;
			} else {
				c = FC[i];
				B.markCell(c.i, c.j);
				score = alphaBeta(B, false, 1, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
				B.unmarkCell();
				if (score > bestScore) {
					bestScore = score;
					move = i;
				}
			}
		}
		B.markCell(FC[move].i, FC[move].j);
		return FC[move];
		
		// c = FC[0]; // random move
		//B.markCell(c.i, c.j);
		//return c;

	}

	/*
	 * public enum MNKGameState {
	 * OPEN,
	 * DRAW,
	 * WINP1,
	 * WINP2
	 * }
	 */
	public int alphaBeta(MNKBoard board, boolean isMaximizing, int depth, int maxDepth, int alpha, int beta) {
		if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (99.0 / 100.0)) { // se sforo il tempo
			return 0;
		}
		int eval;
		// se facendo una mossa si ottiene la vittoria o se facendo una mossa si riempie
		// la tabella
		if (!board.gameState().equals(MNKGameState.OPEN) || depth >= maxDepth) {//(if depth == 0 or isLeaf(T))
			return Evaluate(board.gameState(), depth, maxDepth);
		}
		// se è il turno max
		if (isMaximizing) { //(else if playerA==true)
			eval = Integer.MIN_VALUE;// eval = -infinito
			// per ogni possibile mossa che posso fare
			MNKCell[] FC = board.getFreeCells();
			// System.out.println("FC:" + FC);
			for (MNKCell d : FC) {
				board.markCell(d.i, d.j);
				eval = Math.max(eval, alphaBeta(board, false, depth - 1, maxDepth, alpha, beta));
				board.unmarkCell();
				alpha = Math.max(eval, alpha);
				if (beta <= alpha) {// beta cutoff
					break;
				}
			}
			return eval;
		}
		// se è il turno min
		else {
			eval = Integer.MAX_VALUE; // eval = infinito
			// per ogni possibile mossa che posso fare
			MNKCell[] FC = board.getFreeCells();
			// System.out.println("FC:" + FC);
			for (MNKCell d : FC) {
				board.markCell(d.i, d.j);
				eval = Math.min(eval, alphaBeta(board, true, depth - 1, maxDepth, alpha, beta));
				board.unmarkCell();
				beta = Math.min(eval, beta);
				if (beta <= alpha) {// alpha cutoff
					break;
				}
			}
			return eval;
		}
	}

	private int Evaluate(MNKGameState state, int depth, int maxDepth) {

		int ret;
	    if (state.equals(myWin)) { // vittoria bot
			ret = depth == 0 ? 100 : 100 / depth;
			//winStates.add(state);
		} else if (state.equals(yourWin)) { // vittoria avversario
			ret = depth == 0 ? -100 : -100 / depth;
			//loseStates.add(state);
		} else if (state.equals(MNKGameState.DRAW)) { // pareggio
			ret = 0;
		} else { // profondità di esplorazione raggiunta
			ret = 0;
			// DEBUG OUTPUT
			// System.out.println("PROFONDITA' RAGGIUNTA");
		}
		return ret;
	}

	private int GetMaxDepth(int len) {
		long n = len, counter = n;
		int ret = 1;
		do {
			n = n - 1;
			if (counter * n <= powLimit) {
				ret++;
			}
			counter = counter * n;
		} while (counter < powLimit && n > 1);
		return ret;
	}

	private void SaveStatus(MNKGameState state, int eval, boolean turn, boolean win){
		if(win) winStates.add(new SavedStates(state, eval, turn));
		else loseStates.add(new SavedStates(state, eval, turn));
	}

	private int CheckStatus(MNKGameState state, boolean turn){ //return -1 if state is not in set, 0 if it is a lose state, 1 if it is a win state
		SavedStates s = new SavedStates(state, 0, turn);
		if(winStates.contains(state)) return 1;
		if(loseStates.contains(state)) return 0;
		return -1;
	}

	public String playerName() {
		return "SignoraCarla";
	}
}
