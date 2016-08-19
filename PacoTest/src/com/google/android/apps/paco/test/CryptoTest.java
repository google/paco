package com.google.android.apps.paco.test;

import android.test.AndroidTestCase;
import android.util.Base64;
import android.util.Log;

import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.Crypto;

import org.junit.Assert;
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

  @Before
  public void setUp() throws Exception {
    crypto = new Crypto(getContext());
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

    Assert.assertArrayEquals(publicKey, decoded.getEncoded());
  }

  @Test
  public void testEncryptAnswer() throws NoSuchAlgorithmException, InvocationTargetException, IllegalAccessException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.genKeyPair();

    Output output = new Output();
    output.setName("name");
    output.setAnswer("answer");

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
}