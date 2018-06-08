package com.example.triangle.test;

import com.example.triangle.src.Triangle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TriangleTest  {
  @Test
  public void test1() {
    Triangle triangle = new Triangle();
    int type = triangle.triangle(3, 3, 3);
    assertEquals(Triangle.EQUILATERAL, type);
  }

  @Test
  public void test2() {
    Triangle triangle = new Triangle();
    int type = triangle.triangle(3, 4, 5);
    assertEquals(Triangle.SCALENE, type);
  }

  @Test
  public void test3() {
    Triangle triangle = new Triangle();
    int type = triangle.triangle(2, 4, 4);
    assertEquals(Triangle.ISOSCELES, type);
  }

  @Test
  public void test4() {
    Triangle triangle = new Triangle();
    int type = triangle.triangle(3, 2, 3);
    assertEquals(Triangle.ISOSCELES, type);
  }

  @Test
  public void test5() {
    Triangle triangle = new Triangle();
    int type = triangle.triangle(3, 7, 3);
    assertEquals(Triangle.INVALID, type);
  }

  @Test
  public void test6() {
    Triangle triangle = new Triangle();
    int type = triangle.triangle(3, 3, 1);
    assertEquals(Triangle.ISOSCELES, type);
  }
}
