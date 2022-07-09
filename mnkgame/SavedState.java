package mnkgame;

import java.util.Random;
import java.util.*;

class SavedState {
   private MNKCell[] cells;
   private boolean turn;

   // constructor
   public SavedState(MNKCell[] cells, boolean turn) {
      this.cells = cells;
      this.turn = turn;
   }

   // getter  
   public MNKCell[] getCells() { return cells; }
   public boolean getTurn() { return turn; }

   // setter
   public void setBoard(MNKCell[] cells) { this.cells = cells; }
   public void setTurn(boolean turn) { this.turn = turn; }

}