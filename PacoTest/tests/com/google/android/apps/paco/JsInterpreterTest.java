package com.google.android.apps.paco;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.pacoapp.paco.js.interpreter.JsInterpreter;

public class JsInterpreterTest {

  private JsInterpreter interpreter;

  @Before
  public void before() {
    interpreter = new JsInterpreter();
  }

  @After
  public void after() {
    interpreter = null;
  }

  @Test
  public void testSimpleAddition() {
    Double result = (Double)interpreter.eval("4 + 5");
    assertEquals(9, result.intValue());
  }

  @Test
  public void testSimpleAdditionReal() {
    Double result = (Double)interpreter.eval("4.1 + 5.2");
    assertEquals(9.3, result.doubleValue(), .01);
  }


  @Test
  public void testSimpleStringReturn() throws Exception {
    String result = (String)interpreter.eval("function f() { return \"hello\"; } f();");
    assertEquals("hello", result);

  }

  @Test
  public void testFunctionCall() throws Exception {
    Double result = (Double)interpreter.eval("function f() { return 4 + 5; } f();");
    assertEquals(9, result.intValue());
  }

  @Test
  public void testBindInt() throws Exception {
    interpreter.bind("i", 4);
    Double result = (Double)interpreter.eval("i + 5");
    assertEquals(9, result.intValue());
  }

  @Test
  public void testInjectVariableString() throws Exception {
    interpreter.bind("i", "hello");
    String result = (String)interpreter.eval("i + 5");
    assertEquals("hello5", result);
  }

  @Test
  public void testBindIntoSubObject() throws Exception {

      Pair pair = new Pair();
      pair.config("apple", "pie");
      Object paco = interpreter.eval("paco = {}");
      interpreter.bind(paco, "pair", pair);
      String result = (String)interpreter.eval("paco.pair.getSecond()");
      assertEquals("pie", result);
  }

  @Test
  public void testBindObject() throws Exception {
      ArrayList arrayList = new ArrayList();
      arrayList.add("Hello");

      interpreter.bind("lst",  arrayList);

      interpreter.eval("lst.add('world')");
      assertEquals(2, arrayList.size());
  }

  @Test
  public void testBindObjectAndSetValue() throws Exception {
      ArrayList arrayList = new ArrayList();
      arrayList.add("Hello");

      interpreter.bind("lst",  arrayList);

      interpreter.eval("lst.add('world')");
      assertEquals(2, arrayList.size());

      Pojo po = new Pojo(5, "bananas");
      interpreter.bind("pojo", po);

      interpreter.eval("lst.add(pojo.getY());");
      assertEquals(3, arrayList.size());
      assertEquals("bananas", arrayList.get(2));
  }

}
