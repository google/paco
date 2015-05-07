package com.pacoapp.paco.js.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.common.base.Strings;
import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.js.bridge.JavascriptEventLoader;
import com.pacoapp.paco.js.bridge.JavascriptExperimentLoader;
import com.pacoapp.paco.js.bridge.JavascriptLogger;
import com.pacoapp.paco.js.bridge.JavascriptNotificationService;
import com.pacoapp.paco.js.bridge.JavascriptPackageManager;
import com.pacoapp.paco.js.bridge.JavascriptSensorManager;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;

public class AndroidJsInterpreterBuilder {


  private AndroidJsInterpreterBuilder() {

  }

  public static JsInterpreter createInterpreter(Context context, Experiment androidExperiment, ExperimentDAO experiment, ExperimentGroup experimentGroup) {
    JsInterpreter interpreter = new JsInterpreter();
    ExperimentProviderUtil experimentProvider = new ExperimentProviderUtil(context);
    bindLibraries(context, interpreter);
    interpreter.newBind("pacodb", new JavascriptEventLoader(experimentProvider, androidExperiment, experiment, experimentGroup));
    final JavascriptExperimentLoader obj = new JavascriptExperimentLoader(context, experimentProvider, experiment, androidExperiment, experimentGroup);
    interpreter.newBind("experimentLoader", obj);
    interpreter.newBind("notificationService", new JavascriptNotificationService(context, experiment, experimentGroup));
    interpreter.newBind("packageManager", new JavascriptPackageManager(context));
    interpreter.newBind("log", new JavascriptLogger());
    interpreter.newBind("sensors", new JavascriptSensorManager(context));
    return interpreter;

  }

  public static void bindLibraries(Context context, JsInterpreter interpreter) {
//    bindLibrary(context, interpreter, "jquery-1.5.1.min.js");
  }

  public static void bindLibrary(Context context, JsInterpreter interpreter, final String libFileNameInAssets) {
    String jqueryLibString = loadLibJsFileFromAssets(context, libFileNameInAssets);
    if (!Strings.isNullOrEmpty(jqueryLibString)) {
      interpreter.addLibrary(jqueryLibString);
    } else {
      throw new IllegalStateException("Could not bind " + libFileNameInAssets + ". empty file");
    }
  }

  public static String loadLibJsFileFromAssets(Context context, final String libFileNameInAssets) {
    InputStream jsFile = null;
    try {
      AssetManager assets = context.getAssets();
      jsFile = assets.open(libFileNameInAssets);
      return readBytes(jsFile);
    } catch (FileNotFoundException e) {
      Log.e(PacoConstants.TAG, "File not found for " + libFileNameInAssets, e);
    } catch (IOException e) {
      Log.e(PacoConstants.TAG, "IO Error loading " + libFileNameInAssets, e);
    } finally {
      if (jsFile != null) {
        try {
          jsFile.close();
        } catch (IOException e) {
        }
      }
    }
    return null;
  }

  public static String readBytes(InputStream is) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] data = new byte[2048];
    int len = 0;
    while ((len = is.read(data, 0, data.length)) >= 0) {
        bos.write(data, 0, len);
    }
    return new String(bos.toByteArray(), "UTF-8");
  }
}
