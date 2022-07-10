package mnkgame;

import java.util.Arrays;

class SavedState {
   private Integer[] cells;

   // constructor
   public SavedState(MNKCell[] c) {
      // System.out.println("CREATO");
      this.cells = toHash(c);
   }

   // getter
   public Integer[] getCells() {
      return cells;
   }

   private Integer[] toHash(MNKCell[] c) {
      Integer[] temp = new Integer[c.length];
      for (int i = 0; i < c.length; i++) {
         temp[i] = c[i].hashCode();
      }
      Arrays.sort(temp);
      return temp;
   }

   @Override
   public boolean equals(Object obj) {
      // System.out.println("EQUALS--------------");
      // same instance
      if (obj == this) {
         return true;
      }
      // null
      if (obj == null) {
         return false;
      }
      // type
      if (!getClass().equals(obj.getClass())) {
         return false;
      }
      // cast and compare state
      SavedState other = (SavedState) obj;
      // Arrays.sort(other.cells);
      // if (Arrays.equals(this.cells, other.cells))
      // System.out.println(this.cells + ", " + other.cells + " " + true);
      // Set<MNKCell[]> set = new HashSet<MNKCell[]>(other.cells);
      return (Arrays.equals(this.cells, other.cells));
   }
}