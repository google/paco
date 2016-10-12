package com.pacoapp.paco.js.interpreter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class JsInterpreter {

  private static Logger Log = LoggerFactory.getLogger(JsInterpreter.class);

  public static class RestrictedContextFactory extends ContextFactory {
    private static final Set<String> ALLOWED_CLASS_NAMES =
              ImmutableSet.of("java.util.ArrayList",
                              com.pacoapp.paco.model.Experiment.class.getName(),
                              com.pacoapp.paco.model.Event.class.getName(),
                              com.google.android.apps.paco.SignalSchedule.class.getName(),
                              com.google.android.apps.paco.SignalTime.class.getName(),
                              com.google.android.apps.paco.Trigger.class.getName(),
                              com.google.android.apps.paco.SignalingMechanism.class.getName(),
                              com.pacoapp.paco.model.Output.class.getName(),
                              com.pacoapp.paco.js.bridge.JavascriptEventLoader.class.getName(),
                              com.pacoapp.paco.js.bridge.JavascriptExperimentLoader.class.getName()
                              );
    @Override
    protected Context makeContext() {
      Context context = super.makeContext();

      context.setClassShutter(new ClassShutter() {
          @Override
          public boolean visibleToScripts(String className) {
            if (true) return true;
            // TODO restrict this to just the specific classes scripts need
            // e.g.
            if (className.startsWith("com.google.android.apps.paco.")) {
              return true;
            }

            if (className.startsWith("com.pacoapp.paco.")) {
              return true;
            }
            if (className.startsWith("com.google.paco.")) {
              return true;
            }

            // Check against the remaining libraries.
            return ALLOWED_CLASS_NAMES.contains(className);
          }
        });
      context.getWrapFactory().setJavaPrimitiveWrap(false);

      return context;
    }
  }

  private ScriptableObject rootScope;
  private Object securityDomain;
  private RestrictedContextFactory contextFactory;
  private Context context;

  public JsInterpreter() {
    super();
    contextFactory = new RestrictedContextFactory();
    securityDomain = null;
      context = contextFactory.enterContext();
      // Turn compilation off. Necessary for Android.
      context.setOptimizationLevel(-1);
      rootScope = context.initStandardObjects(null, true);
      //      String prepCodeForPacoJs = "var window = window || {};\nfunction alert(msg) { log.error(msg); };\n";
//    context.evaluateString(rootScope, prepCodeForPacoJs, "init", 1, securityDomain);

  }

  public Object eval(String code) {
    try {
//      Context context = contextFactory.enterContext();
//      context.setOptimizationLevel(-1);
      return context.evaluateString(rootScope, code, "doit:", 1, securityDomain);
    } catch (JavaScriptException jse) {
      Log.error("JSE: " + jse.getMessage() + ". " + jse.getScriptStackTrace());
      throw jse;
    } catch (RhinoException re) {
      // log
      Log.error("JSE: " + re.getMessage() + ". "  + re.getScriptStackTrace());
      throw new IllegalStateException(re);
//    } finally {
//      context.exit();
    }
  }

  public void exit() {
    context.exit();
  }

  public void addLibrary(String lib) {
    context.evaluateString(rootScope, lib, "doit:", 1, securityDomain);
  }
  public void newBind(String name, Object obj) {
    ScriptableObject.putProperty(rootScope, name, context.javaToJS(obj, rootScope));
  }

  public void bind(String name, Object value) {
    rootScope.put(name, rootScope, value);
  }

  public void bind(Object object, String name, Object value) {
    try {
      ScriptableObject scriptable = (ScriptableObject) object;
      contextFactory.enterContext();
      scriptable.defineProperty(name, value, ScriptableObject.CONST);
    } catch (JavaScriptException e) {
      // log
      throw e;
    } catch (RhinoException e) {
      // log
      throw new IllegalStateException(String.format("Error binding: %s.", name), e);
    } finally {
      Context.exit();
    }
  }

  public Scriptable getValue(String string) {
    if (rootScope != null) {
      return (Scriptable) rootScope.get(string, rootScope);
    }
    return null;
  }

  public Object callFunction(String script, String functionName, Object... args)
      throws IOException, NoSuchMethodException {
    Reader reader = new StringReader(script);
    return callFunction(reader, functionName, args);
  }

  public Object callFunction(Reader reader, String functionName, Object... args)
      throws IOException, NoSuchMethodException {
    try {
      Context context = contextFactory.enterContext();
      context.evaluateReader(rootScope, reader, null, 0, null);
      Object functionObj = rootScope.get(functionName, rootScope);
      if (!(functionObj instanceof Function)) {
        throw new NoSuchMethodException(String.format("Function %s is not a function or doesn't exist.", functionName));
      }
      Function function = (Function) functionObj;
      return function.call(context, rootScope, rootScope, args);
    } catch (JavaScriptException e) {
      throw e;
    } catch (RhinoException e) {
      throw new IllegalStateException(String.format("Error calling function: %s.", functionName),
          e);
    } finally {
      Context.exit();
    }
  }
}
