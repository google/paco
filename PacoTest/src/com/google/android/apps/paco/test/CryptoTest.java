package com.google.android.apps.paco.test;

import android.test.AndroidTestCase;
import android.util.Base64;
import android.util.Log;

import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.Crypto;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoTest extends AndroidTestCase {
  private Crypto crypto;
  private Method base64ToPublicKey;
  private Method encryptAnswer;
  private KeyPair keyPair;
  private Output output;

  @Before
  public void setUp() throws Exception {
    crypto = new Crypto(getContext());

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    keyPair = keyGen.genKeyPair();

    output = new Output();
    output.setName("name");
    output.setAnswer("answer");

    // Make necessary methods accessible
    base64ToPublicKey = crypto.getClass().getDeclaredMethod("base64ToPublicKey", String.class);
    base64ToPublicKey.setAccessible(true);
    encryptAnswer = crypto.getClass().getDeclaredMethod("encryptAnswer", Output.class, PublicKey.class);
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
  public void testEncryptAnswer() throws NoSuchAlgorithmException, InvocationTargetException, IllegalAccessException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
    Output encryptedOutput = null;
    try {
      encryptedOutput = (Output) encryptAnswer.invoke(crypto, output, keyPair.getPublic());
    } catch (Exception e) {
      fail("Exception when trying to encrypt: " + e);
    }

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
    String encryptedAnswer = encryptedOutput.getAnswer();
    byte[] encryptedBytes = Base64.decode(encryptedAnswer, Base64.NO_WRAP);
    byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
    String decryptedAnswer = new String(decryptedBytes, "UTF-8");

    assertEquals(decryptedAnswer, "answer");
  }

  /**
   * This test makes sure that encrypting the same string twice would yield a different result. This
   * is part of non-determinism, which is needed because otherwise someone with access to the server
   * DB could encrypt a string of plaintext with the public key of the experiment organizer, and
   * check if the encrypted text corresponds to any data in the database.
   */
  @Test
  public void testNonDeterminism() {
    try {
      Output encryptedOutput1 = (Output) encryptAnswer.invoke(crypto, output, keyPair.getPublic());
      Output output2 = new Output();
      output2.setName("name");
      output2.setAnswer("answer");
      Output encryptedOutput2 = (Output) encryptAnswer.invoke(crypto, output2, keyPair.getPublic());

      assertThat(encryptedOutput1.getAnswer(), not(equalTo(encryptedOutput2.getAnswer())));
    } catch (Exception e) {
      fail("Exception when trying to encrypt: " + e);
    }
  }
}