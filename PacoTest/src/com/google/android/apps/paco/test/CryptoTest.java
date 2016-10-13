package com.google.android.apps.paco.test;

import android.test.AndroidTestCase;
import android.util.Base64;
import android.util.Log;

import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.Crypto;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoTest extends AndroidTestCase {
  private static final long ENCRYPTED_EXPERIMENT_ID = 1L;
  private static final long UNENCRYPTED_EXPERIMENT_ID = 2L;
  private static final long BADKEY_EXPERIMENT_ID = 3L;
  private static final long NONEXISTENT_EXPERIMENT_ID = 4L;

  private Crypto crypto;
  private Method base64ToPublicKey;
  private Method encryptAnswer;
  private KeyPair keyPair;
  private Output output;
  private List<Output> outputList;
  private ExperimentProviderUtil experimentProviderUtil;
  private IvParameterSpec ivParameterSpec;
  private SecretKey secretKey;

  @Before
  public void setUp() throws Exception {
    experimentProviderUtil = new MockExperimentProviderUtil(getContext());
    crypto = new Crypto(experimentProviderUtil);

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    keyPair = keyGen.genKeyPair();
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    byte[] iv = new byte[16];
    secureRandom.nextBytes(iv);
    ivParameterSpec = new IvParameterSpec(iv);

    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(192);
    secretKey = keyGenerator.generateKey();

    output = new Output();
    output.setName("name");
    output.setAnswer("answer");
    outputList = Arrays.asList(output);

    ExperimentDAO dao = new ExperimentDAO();
    dao.setPublicKey(Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.NO_WRAP));
    Experiment experiment = new Experiment();
    experiment.setExperimentDAO(dao);
    experiment.setServerId(ENCRYPTED_EXPERIMENT_ID);
    experimentProviderUtil.insertFullJoinedExperiment(experiment);

    // Make necessary methods accessible
    base64ToPublicKey = crypto.getClass().getDeclaredMethod("base64ToPublicKey", String.class);
    base64ToPublicKey.setAccessible(true);
    encryptAnswer = crypto.getClass().getDeclaredMethod("encryptAnswer", Output.class, SecretKey.class, IvParameterSpec.class);
    encryptAnswer.setAccessible(true);
  }

  @Test
  public void testBase64Decode() throws NoSuchAlgorithmException, InvocationTargetException, IllegalAccessException, UnsupportedEncodingException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
    String encoded64 = Base64.encodeToString(publicKey, Base64.NO_WRAP);

    PublicKey decoded = (PublicKey) base64ToPublicKey.invoke(crypto, encoded64);

    assertArrayEquals(publicKey, decoded.getEncoded());
  }

  @Test
  public void testEncryptAnswer() throws NoSuchAlgorithmException, InvocationTargetException, IllegalAccessException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
    Output encryptedOutput = null;
    try {
      encryptedOutput = (Output) encryptAnswer.invoke(crypto, output, secretKey, ivParameterSpec);
    } catch (Exception e) {
      fail("Exception when trying to encrypt: " + e);
    }

    byte[] decryptedBytes = decryptSymmetric(encryptedOutput.getAnswer(), secretKey, ivParameterSpec);
    String decryptedAnswer = new String(decryptedBytes, "UTF-8");

    assertEquals(decryptedAnswer, "answer");
  }

  @Test
  public void testCompleteEncryption() throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException {
    Event event = new Event();
    event.setResponses(outputList);
    event.setServerExperimentId(ENCRYPTED_EXPERIMENT_ID);

    Event encryptedEvent = crypto.encryptAnswers(event);
    String encryptionKeyEncrypted = null;
    String encryptionIvEncrypted = null;
    String answerEncrypted = null;
    for (Output response: encryptedEvent.getResponses()) {
      if (response.getName().equals(Crypto.ENCRYPTION_KEY))
        encryptionKeyEncrypted = response.getAnswer();
      else if (response.getName().equals(Crypto.ENCRYPTION_IV))
        encryptionIvEncrypted = response.getAnswer();
      else if (response.getName().equals("name"))
        answerEncrypted = response.getAnswer();
    }

    byte[] symmetricKeyBytes = decryptAsymmetric(encryptionKeyEncrypted, keyPair.getPrivate());
    byte[] ivBytes = decryptAsymmetric(encryptionIvEncrypted, keyPair.getPrivate());
    SecretKeySpec privKey = new SecretKeySpec(symmetricKeyBytes, "AES");
    IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

    String decryptedAnswer = new String(decryptSymmetric(answerEncrypted, privKey, ivspec), "UTF-8");

    assertEquals(decryptedAnswer, "answer");
  }

  @Test
  public void testDontEncryptIfNoKey() throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException {
    ExperimentDAO daoUnencrypted = new ExperimentDAO();
    Experiment experimentUnencrypted = new Experiment();
    experimentUnencrypted.setExperimentDAO(daoUnencrypted);
    experimentUnencrypted.setServerId(UNENCRYPTED_EXPERIMENT_ID);
    experimentProviderUtil.insertFullJoinedExperiment(experimentUnencrypted);

    Event event = new Event();
    event.setResponses(outputList);
    event.setServerExperimentId(UNENCRYPTED_EXPERIMENT_ID);

    Event encryptedEvent = crypto.encryptAnswers(event);

    boolean nameStillThere = false;
    for (Output response: encryptedEvent.getResponses()) {
      if (response.getName().equals("name")) {
        // Make sure the answer was not encrypted
        assertEquals(response.getAnswer(), "answer");
        nameStillThere = true;
      }
    }
    assert(nameStillThere);
  }

  @Test
  public void testDontEncryptIfNoCorrespondingExperiment() throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException {
    Event event = new Event();
    event.setResponses(outputList);
    // Experiment ID does not exist in DB, will return null.
    event.setServerExperimentId(NONEXISTENT_EXPERIMENT_ID);

    Event encryptedEvent = crypto.encryptAnswers(event);

    boolean nameStillThere = false;
    for (Output response : encryptedEvent.getResponses()) {
      if (response.getName().equals("name")) {
        // Make sure the answer was not encrypted
        assertEquals(response.getAnswer(), "answer");
        nameStillThere = true;
      }
    }
    assert (nameStillThere);
  }

  @Test
  public void testDontFallbackIfException() throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException {
    ExperimentDAO daoBadKey = new ExperimentDAO();
    daoBadKey.setPublicKey("badkey");
    Experiment experimentBadKey = new Experiment();
    experimentBadKey.setExperimentDAO(daoBadKey);
    experimentBadKey.setServerId(BADKEY_EXPERIMENT_ID);
    experimentProviderUtil.insertFullJoinedExperiment(experimentBadKey);

    Event event = new Event();
    event.setResponses(outputList);
    event.setServerExperimentId(BADKEY_EXPERIMENT_ID);

    Event eventEncrypted = new Event();
    eventEncrypted.setResponses(outputList);
    eventEncrypted.setServerExperimentId(ENCRYPTED_EXPERIMENT_ID);

    List<Event> events = Arrays.asList(event, eventEncrypted);
    List<Event> encryptedEvents = crypto.encryptAnswers(events);

    // Make sure the experiment with the bad key got ignored
    assertEquals(encryptedEvents.size(), 1);
    assertEquals(encryptedEvents.get(0).getExperimentServerId(), ENCRYPTED_EXPERIMENT_ID);
  }

  /**
   * This test makes sure that encrypting the same string twice would yield a different result for
   * different Events. This is important, as we don't want information to leak about two answers
   * that were the same for different events.
   */
  @Test
  public void testNonDeterminism() {
    try {
      Event event1 = new Event();
      event1.setResponses(outputList);
      event1.setServerExperimentId(ENCRYPTED_EXPERIMENT_ID);

      Event event2 = new Event();
      Output output2 = new Output();
      output2.setName(output.getName());
      output2.setAnswer(output.getAnswer());
      event2.setResponses(Arrays.asList(output2));
      event2.setServerExperimentId(ENCRYPTED_EXPERIMENT_ID);

      Event encryptedEvent1 = crypto.encryptAnswers(event1);
      Event encryptedEvent2 = crypto.encryptAnswers(event2);

      String answer1 = null;
      for (Output output : encryptedEvent1.getResponses()) {
        if (output.getName().equals("name"))
          answer1 = output.getAnswer();
      }
      String answer2 = null;
      for (Output output : encryptedEvent2.getResponses()) {
        if (output.getName().equals("name"))
          answer2 = output.getAnswer();
      }

      assertThat(answer1, not(equalTo(answer2)));
    } catch (Exception e) {
      fail("Exception when trying to encrypt: " + e);
    }
  }

  /**
   * Decrypt a byte array using a given private RSA key.
   * @param encrypted The byte array that was encrypted using the corresponding RSA public key
   * @param privateKey A private RSA key.
   * @return The unencrypted byte array.
   */
  private byte[] decryptAsymmetric(String encrypted, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] encryptedBytes = Base64.decode(encrypted, Base64.NO_WRAP);
    byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
    return decryptedBytes;
  }

  private byte[] decryptSymmetric(String encrypted, SecretKey secretKey, IvParameterSpec ivSpec) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
    byte[] encryptedBytes = Base64.decode(encrypted, Base64.NO_WRAP);
    return cipher.doFinal(encryptedBytes);
  }
}