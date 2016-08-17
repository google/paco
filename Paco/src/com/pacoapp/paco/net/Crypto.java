package com.pacoapp.paco.net;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Crypto {
  private ExperimentProviderUtil experimentProviderUtil;

  public Crypto(Context context) {
    experimentProviderUtil = new ExperimentProviderUtil(context);
  }

  public List<Event> encryptAnswers(List<Event> events) throws NoSuchAlgorithmException, NoSuchPaddingException {
    ArrayList<Event> encryptedEvents = new ArrayList();
    for (Event event : events) {
      encryptedEvents.add(encryptAnswers(event));
    }
    return encryptedEvents;
  }

  public Event encryptAnswers(Event event) throws NoSuchPaddingException, NoSuchAlgorithmException {
    long experimentId = event.getExperimentServerId();
    Experiment experiment = experimentProviderUtil.getExperimentByServerId(experimentId);
    String publicKeyString = experiment.getExperimentDAO().getPublicKey();
    if (publicKeyString == "") {
      Log.v(PacoConstants.TAG, "No public key for experiment " + experiment.getExperimentDAO().getTitle());
      return event;
    }

    Log.v(PacoConstants.TAG, "Using public key for experiment " + experiment.getExperimentDAO().getTitle() + ". Key string is " + publicKeyString);
    PublicKey publicKey = base64ToPublicKey(publicKeyString);

    ArrayList<Output> encryptedResponses = new ArrayList();
    for (Output answer : event.getResponses()) {
      encryptedResponses.add(encryptAnswer(answer, publicKey));
    }
    event.setResponses(encryptedResponses);

    return event;
  }

  private Output encryptAnswer(Output response, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException {
    Cipher cipher = Cipher.getInstance("RSA");
    try {
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      String answer = response.getAnswer();
      String encryptedAnswer = cipher.doFinal(answer.getBytes()).toString();
      response.setAnswer(encryptedAnswer);
    } catch (InvalidKeyException e) {
      // TODO: inform the experiment organizer about the key being invalid
      Log.e(PacoConstants.TAG, "Invalid key for experiment " + e);
      // Return unencrypted
    } catch (BadPaddingException e) {
      Log.e(PacoConstants.TAG, "Bad padding for answer " + e);
    } catch (IllegalBlockSizeException e) {
      Log.e(PacoConstants.TAG, "Illegal block size for answer " + e);
    }
    return response;
  }

  private PublicKey base64ToPublicKey(String publicKeyString) {
    try{
      byte[] byteKey = Base64.decode(publicKeyString.getBytes(), Base64.DEFAULT);
      X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
      KeyFactory kf = KeyFactory.getInstance("RSA");

      return kf.generatePublic(X509publicKey);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    return null;
  }
}
