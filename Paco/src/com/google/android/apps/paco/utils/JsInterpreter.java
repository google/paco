package com.google.android.apps.paco.utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JsInterpreter {

  private android.content.Context androidContext;

  public JsInterpreter(android.content.Context context) {
    super();
    this.androidContext = context;
  }

  public Object doit(String code) {
    // Create an execution environment.
    Context cx = Context.enter();

    // Turn compilation off.
    cx.setOptimizationLevel(-1);

    try {
      // Initialize a variable scope with bindings for
      // standard objects (Object, Function, etc.
      Scriptable scope = cx.initStandardObjects();
      // Set a global variable that holds the activity instance.
      ScriptableObject.putProperty(scope, "TheActivity", Context.javaToJS(this, scope));

      // Evaluate the script.
      return cx.evaluateString(scope, code, "doit:", 1, null);
    } finally {
      Context.exit();
    }
  }

  private static final String RHINO_LOG = "var log = Packages.io.vec.ScriptAPI.log;";

  public static void log(String msg) {
    android.util.Log.i("RHINO_LOG", msg);
  }

  public void runScript(String script) {
    // Get the JavaScript in previous section
    String functionName = "hello";
    Object[] functionParams = new Object[] { "Android" };

    // Every Rhino VM begins with the enter()
    // This Context is not Android's Context
    Context rhino = Context.enter();

    // Turn off optimization to make Rhino Android compatible
    rhino.setOptimizationLevel(-1);
    try {
      Scriptable scope = rhino.initStandardObjects();

      // This line set the javaContext variable in JavaScript
      ScriptableObject.putProperty(scope, "javaContext", Context.javaToJS(androidContext, scope));

      // Note the forth argument is 1, which means the JavaScript source has
      // been compressed to only one line using something like YUI
      rhino.evaluateString(scope, RHINO_LOG + script, "ScriptAPI", 1, null);

      // We get the hello function defined in JavaScript
      Function function = (Function) scope.get(functionName, scope);

      // Call the hello function with params
      NativeObject result = (NativeObject) function.call(rhino, scope, scope, functionParams);
      // After the hello function is invoked, you will see logcat output

      // Finally we want to print the result of hello function
      String foo = (String) Context.jsToJava(result.get("foo", result), String.class);
      log(foo);
    } finally {
      // We must exit the Rhino VM
      Context.exit();
    }
  }

  public static void main(String[] args) {
   System.out.println(new JsInterpreter(null).doit("4 + 5"));
  }
}
