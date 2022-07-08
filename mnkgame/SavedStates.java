package mnkgame;

import java.util.Random;
import java.util.*;

class SavedStates {
   private MNKGameState state;
   private int eval;
   private boolean turn;

   // constructor
   public SavedStates(MNKGameState state, int eval, boolean turn) {
      this.state = state;
      this.eval = eval;
      this.turn = turn;
   }

   // getter  
   public MNKGameState getState() { return state; }
   public int getEval() { return eval; }
   public boolean getTurn() { return turn; }

   // setter
   public void setState(MNKGameState state) { this.state = state; }
   public void setEval(int eval) { this.eval = eval; }
   public void setTurn(boolean turn) { this.turn = turn; }

@Override 
   public boolean equals (Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            SavedStates gameState = (SavedStates) object;
            if (this.state == gameState.getState() && this.turn == gameState.getTurn()) {
                result = true;
            }
        }
        return result;
   }
}