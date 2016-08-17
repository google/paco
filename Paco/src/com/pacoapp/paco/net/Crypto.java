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

/**
 * This class provides all end-to-end crypto functionality in Paco. It allows an experiment provider
 * to store all data on the server encrypted with their own public RSA key, so we are not able to
 * read the data.
 */

public class Crypto {
  private ExperimentProviderUtil experimentProviderUtil;

  /**
   * Constructor
   * @param context The application context
   */
  public Crypto(Context context) {
    experimentProviderUtil = new ExperimentProviderUtil(context);
  }

  /**
   * Encrypt all answers for the responses given in the event lists. Every event's Experiment is
   * checked to see whether it provides a public key. If so, the answers for the event are encrypted
   * @param events The events we would like to encrypt
   * @return The same events as were passed to the function
   * @throws NoSuchAlgorithmException If the RSA algorithm is not supported on the device
   * @throws NoSuchPaddingException If padding is not supported for RSA on the device
   */
  public List<Event> encryptAnswers(List<Event> events) throws NoSuchAlgorithmException, NoSuchPaddingException {
    ArrayList<Event> encryptedEvents = new ArrayList();
    for (Event event : events) {
      encryptedEvents.add(encryptAnswers(event));
    }
    return encryptedEvents;
  }

  /**
   * Encrypt all answers for the responses in an event, if the event belongs to an experiment that
   * provides a public key. If there is no key for an experiment, the answers are left unencrypted.
   * @param event The event we would like to encrypt
   * @return The same event as was passed to the function
   * @throws NoSuchAlgorithmException If the RSA algorithm is not supported on the device
   * @throws NoSuchPaddingException If padding is not supported for RSA on the device
   */
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

  /**
   * Encrypt a single response's answer using the provided public key
   * @param response The response for which to encrypt the answer
   * @param publicKey The corresponding experiment's public key
   * @return The same response as was passed to the function
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   */
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

  /**
   * Converts a provided BASE64 encoded public key to the corresponding RSA key
   * @param publicKeyString A BASE64 encoded public key
   * @return A RSA Public Key
   */
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
