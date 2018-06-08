package com.example.triangle.src;


public class Triangle {
    public static int INVALID = 0;
    public static int ISOSCELES = 1;
    public static int SCALENE = 2;
    public static int EQUILATERAL = 3;

    public int triangle(int a, int b, int c) {
        if (a <= 0 || b <= 0 || c <= 0) {
            return INVALID;
        }
        int trian = 0;
        if (a == b) {
            trian = trian + 1;
        }
        if (a == c) {
            trian = trian + 2;
        }
        if (b == c) {
            trian = trian + 3;
        }
        if (trian == 0) {
            if (a + b < c || a + c < b || b + c < a) {
                return INVALID;
            } else {
                return SCALENE;
            }
        }
        if (trian > 3) return EQUILATERAL;
        if (trian == 1 && a + b > c) return ISOSCELES;
        else if (trian == 2 && a + c > b) return ISOSCELES;
        else if (trian == 3 && b + c > a) return ISOSCELES;
        return INVALID;
    }
}
