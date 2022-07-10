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

/**
 * Software player only a bit smarter than random.
 * <p>
 * It can detect a single-move win or loss. In all the other cases behaves
 * randomly.
 * </p>
 */
public class SignoraCarla implements MNKPlayer {
	private Random rand;
	private MNKBoard B;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
	private long startingTime;
	private double powLimit;

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

		// system.out.println("--Prima parte (controllo se posso vincere in una mossa)--");
		startingTime = System.currentTimeMillis();
		if (MC.length > 0) {
			MNKCell c = MC[MC.length - 1]; // Recupero l'ultima mossa da MC
			B.markCell(c.i, c.j); // salvo l'ultima mossa in MNKBoard
		}
		// Se c'è una sola possibile mossa, la restituisco
		if (FC.length == 1) {
			B.markCell(FC[0].i, FC[0].j);
			return FC[0];
		}
		// Controllo se posso vincere in una mossa
		for (MNKCell d : FC) {
			// Se sta finendo il tempo, restituisco una cella a caso
			if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (97.0 / 100.0)) {
				MNKCell c = FC[rand.nextInt(FC.length)];
				B.markCell(c.i, c.j);
				return c;
			} else if (B.markCell(d.i, d.j) == myWin) {
				return d;
			} else {
				B.unmarkCell();
			}
		}
		// system.out.println("--Seconda parte (controllo se l'avversario vince in una mossa)--");
		/* 
		* Contollo se posso perdere in una sola mossa, selezionando una cella a caso e
		* controllando se l'avversario può vincere.
		* Nel caso possa vincere, seleziono la sua posizione di vincita
		*/
		MNKCell c = FC[0]; // cella a caso
		B.markCell(c.i, c.j); // seleziono la cella

		for (int k = 1; k < FC.length; k++) {
			// Se sta finendo il tempo, restituisco la cella presa a caso
			if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (97.0 / 100.0)) {
				return c;
			} else {
				MNKCell d = FC[k];
				if (B.markCell(d.i, d.j) == yourWin) {
					B.unmarkCell(); // Cancello la mossa dell'avversario
					B.unmarkCell(); // cancello la mia mossa
					B.markCell(d.i, d.j); // seleziono la sua posizione di vincita
					return d;
				} else {
					B.unmarkCell();
				}
			}
		}
		B.unmarkCell();
		// Controllo se l'avversario vincerebbe in FC[0], segnando FC[1] come mia mossa
		c = FC[1];
		B.markCell(c.i, c.j);
		MNKCell d = FC[0];
		if (B.markCell(d.i, d.j) == yourWin) {
			B.unmarkCell();
			B.unmarkCell();
			B.markCell(d.i, d.j);
		} else {
			B.unmarkCell();
		}
		B.unmarkCell();
		// system.out.println("--Terza parte (alphaBeta)--");

		int maxDepth = GetMaxDepth(FC.length);
		int move = 0, bestScore = Integer.MIN_VALUE, score = bestScore;
		for (int i = 0; i < B.getFreeCells().length; i++) {
			if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (97.0 / 100.0)) { // timeout
				// system.out.println("timeout");
				break;
			} else {
				c = FC[i];
				B.markCell(c.i, c.j);
				score = IterativeDeepening(B, false, maxDepth);
				B.unmarkCell();
				if (score > bestScore) {
					bestScore = score;
					move = i;
				}
			}
		}
		B.markCell(FC[move].i, FC[move].j);
		return FC[move];
	}

	public int IterativeDeepening(MNKBoard board, boolean isMaximizing, int maxDepth) {
		int eval = 0;
		int lastEval = 0;
		for (int d = 0; d <= maxDepth; d++) {
			if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (97.0 / 100.0)) {
				// system.out.println("timeout");
				return lastEval;
			} else {
				lastEval = eval;
				eval = alphaBeta(board, isMaximizing, d, GetMaxDepth(board.getFreeCells().length), Integer.MIN_VALUE, Integer.MAX_VALUE);
			}
		}
		return eval;
	}

	public int alphaBeta(MNKBoard board, boolean isMaximizing, int depth, int maxDepth, int alpha, int beta) {
		if ((System.currentTimeMillis() - startingTime) / 1000.0 > TIMEOUT * (97.0 / 100.0)) { // se sforo il tempo
			return 0;
		}
		int eval;
		// se facendo una mossa si ottiene la vittoria o se facendo una mossa si riempie la tabella
		if (!board.gameState().equals(MNKGameState.OPEN)) {// se è una foglia
			return Evaluate(board.gameState(), depth, maxDepth);
		} else if (depth >= maxDepth) { // se ho superato maxDepth
			MNKCell[] MC = B.getMarkedCells();
			int temp = EvaluateCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
			if (!isMaximizing)
				return temp;
			else
				return -temp;
		}

		// se è il turno max
		if (isMaximizing) {
			eval = Integer.MIN_VALUE;// eval = -infinito
			// per ogni possibile mossa che posso fare
			MNKCell[] FC = board.getFreeCells();
			for (MNKCell d : FC) {
				board.markCell(d.i, d.j);
				eval = Math.max(eval, alphaBeta(board, false, depth + 1, maxDepth, alpha, beta));
				board.unmarkCell();
				alpha = Math.max(eval, alpha);
				if (beta <= alpha) {// beta cutoff
					break;
				}
			}
			return eval;
		}
		else { // se è il turno min
			eval = Integer.MAX_VALUE; // eval = infinito
			// per ogni possibile mossa che posso fare
			MNKCell[] FC = board.getFreeCells();
			for (MNKCell d : FC) {
				board.markCell(d.i, d.j);
				eval = Math.min(eval, alphaBeta(board, true, depth + 1, maxDepth, alpha, beta));
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
			ret = ((B.K * B.K) - 1);
		} else if (state.equals(yourWin)) { // vittoria avversario
			ret = -((B.K * B.K) - 1);
		} else { // pareggio
			ret = 0;
		}
		return ret;
	}

	private int GetMaxDepth(int len) {
		long n = len, counter = len;
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

	private int EvaluateCell(int i, int j) {
		MNKCellState[][] board = B.B;
		MNKCellState s = board[i][j];
		int n, value = 0, rate = 0;
		if (s == MNKCellState.FREE)
			return 0;
		// Horizontal check
		n = 1;
		value = 0;
		for (int k = 1; j - k >= 0 && (board[i][j - k] == s || board[i][j - k] == MNKCellState.FREE); k++) {// k<=j
			if (board[i][j - k] == s)
				value++;
			n++; // backward check
		}
		for (int k = 1; j + k < B.N && (board[i][j + k] == s || board[i][j + k] == MNKCellState.FREE); k++) {
			if (board[i][j + k] == s)
				value++;
			n++; // forward check
		}
		if (n >= B.K) {
			rate = ((B.N + 1) - B.K) + value;
		}
		// ----------------------------------------------------------------------------------------------------------
		// Vertical check
		n = 1;
		value = 0;
		for (int k = 1; i - k >= 0 && (board[i - k][j] == s || board[i - k][j] == MNKCellState.FREE); k++) {
			if (board[i - k][j] == s) {
				value++;
			}
			n++; // backward check
		}
		for (int k = 1; i + k < B.M && (board[i + k][j] == s || board[i + k][j] == MNKCellState.FREE); k++) {
			if (board[i + k][j] == s) {
				value++;
			}
			n++; // forward check
		}
		if (n >= B.K)
			rate += ((B.M + 1) - B.K) + value;
		// ----------------------------------------------------------------------------------------------------------
		// Diagonal check
		n = 1;
		value = 0;
		for (int k = 1; i - k >= 0 && j - k >= 0
				&& (board[i - k][j - k] == s || board[i - k][j - k] == MNKCellState.FREE); k++) {
			if (board[i - k][j - k] == s) {
				value++;
			}
			n++; // backward check
		}
		for (int k = 1; i + k < B.M && j + k < B.N
				&& (board[i + k][j + k] == s || board[i + k][j + k] == MNKCellState.FREE); k++) {
			if (board[i + k][j + k] == s) {
				value++;
			}
			n++; // forward check
		}
		if (n >= B.K)
			rate += ((Math.min(B.N, B.M) + 1) - B.K) + value;
		// ----------------------------------------------------------------------------------------------------------
		// Anti-diagonal check
		n = 1;
		value = 0;
		for (int k = 1; i - k >= 0 && j + k < B.N
				&& (board[i - k][j + k] == s || board[i - k][j + k] == MNKCellState.FREE); k++) {
			if (board[i - k][j + k] == s) {
				value++;
			}
			n++; // backward check
		}
		for (int k = 1; i + k < B.M && j - k >= 0
				&& (board[i + k][j - k] == s || board[i + k][j - k] == MNKCellState.FREE); k++) {
			if (board[i + k][j - k] == s) {
				value++;
			}
			n++; // backward check
		}
		if (n >= B.K)
			rate += ((Math.min(B.N, B.M) + 1) - B.K) + value;
		return rate;
	}

	public String playerName() {
		return "SignoraCarla";
	}
}
