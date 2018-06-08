package reu.hom;

public class Utils {
  public static int randRange(int startInc, int endExc) {
    int range = endExc - startInc;
    return ((int) (Math.random() * range)) + startInc;
  }
}
