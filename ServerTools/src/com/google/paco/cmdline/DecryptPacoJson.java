package com.google.paco.cmdline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class allows you to decrypt a JSON file exported by Paco with a given private key.
 */
class DecryptPacoJson {

  static class SymmetricParameters {
    private SecretKey mSecretKey;
    private IvParameterSpec mIvParameterSpec;

    public SecretKey getSecretKey() { return mSecretKey; }
    public void setSecretKey(SecretKey secretKey) { this.mSecretKey = secretKey; }
    public IvParameterSpec getIvParameterSpec() { return mIvParameterSpec; }
    public void setIvParameterSpec(IvParameterSpec ivParameterSpec) { this.mIvParameterSpec = ivParameterSpec; }
  }

  public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, JSONException {
    String inputFilename = args[0];
    String outputFilename = args[1];
    String privateKeyEncoded = args[2];

    PrivateKey privateKey = base64ToPrivateKey(privateKeyEncoded);
    JSONArray input = jsonFromFile(inputFilename);

    decryptJson(input, privateKey);

    jsonToFile(input, outputFilename);
  }

  /**
   * Decrypts a JSON array exported by Paco in place, using a given private RSA key.
   * @param input The JSON array exported by Paco
   * @param privateKey The private key of the experiment organizer
   */
  public static void decryptJson(JSONArray input, PrivateKey privateKey) throws JSONException {
    for (int i = 0; i < input.length(); i++) {
      JSONObject event = input.getJSONObject(i);
      JSONArray responses = null;
      try {
        responses = event.getJSONArray("responses");
        SymmetricParameters eventKey = getEventSecretKey(responses, privateKey);
        if (eventKey != null) {
          responses = decryptAnswers(responses, eventKey);
        }
      } catch (JSONException e) {
        System.out.println("Found event without responses, ignoring.");
      }
      // Replace the responses with the decrypted version, if available
      if (responses != null)
        event.put("responses", responses);
    }
  }

  /**
   * Generate a public/private RSA key pair for use with Paco.
   * @return Two BASE64 encoded strings: an encoded version of the public key, and an encoded version
   *          of the private key
   */
  public static String[] generateKey() throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.genKeyPair();
    byte[] encodedPubKey = Base64.getEncoder().encode(keyPair.getPublic().getEncoded());
    byte[] encodedPrivKey = Base64.getEncoder().encode(keyPair.getPrivate().getEncoded());
    System.out.println("Here's a key pair for you. Its format is " + keyPair.getPrivate().getFormat() + keyPair.getPublic().getFormat() + "\n");
    String pubString = new String(encodedPubKey, "UTF-8");
    String privString = new String(encodedPrivKey, "UTF-8");
    System.out.println("Public: " + pubString + "\n");
    System.out.println("Private: " + privString);
    return new String[] {pubString, privString};
  }

  /**
   * Decrypts all answers in a JSONArray with a given key and IV
   * @param responses The encrypted responses
   * @param key The symmetric parameters used for encryption
   * @return A JSONArray containing the decrypted responses (or unchanged in case we were unable
   *          to decrypt).
   */
  private static JSONArray decryptAnswers(JSONArray responses, SymmetricParameters key) throws JSONException {
    JSONArray result = new JSONArray();
    for (int i = 0; i < responses.length(); i++) {
      JSONObject response = responses.getJSONObject(i);
      if (!response.getString("name").equals("encryptionKey") && !response.getString("name").equals("encryptionIv")) {
        String encryptedAnswer = "";
        try {
          encryptedAnswer = response.getString("answer");
          String decryptedAnswer = decryptSymmetric(encryptedAnswer, key);
          //System.out.println("A decrypted answer for you: " + decryptedAnswer + " -- from " + encryptedAnswer);
          response.put("answer", decryptedAnswer);
        } catch (JSONException e) {
          System.out.println("Scary. Response " + response.getString("name") + " has no answer. Ignoring.");
        } catch (Exception e) {
          System.out.println("Exception when trying to decrypt " + response.getString("name") + ": " + e);
          e.printStackTrace();
        }
        // Add the (either unchanged or decrypted) response to the result set
        result.put(response);
      }
    }
    return result;
  }

  /**
   * Get the secret key for an event in the Paco datastore
   * @param responses Array of all responses for this event
   * @return The parameters for symmetric encryption for this event (secret key and IV), or null if this event was not encrypted
   */
  private static SymmetricParameters getEventSecretKey(JSONArray responses, PrivateKey privateKey) throws JSONException {
    String encryptionKeyEncrypted = null;
    String encryptionIvEncrypted = null;

    for (int i = 0; i < responses.length(); i++) {
      JSONObject response = responses.getJSONObject(i);
      try {
        if (response.getString("name").equals("encryptionKey")) {
          encryptionKeyEncrypted = response.getString("answer");
        } else if (response.getString("name").equals("encryptionIv")) {
          encryptionIvEncrypted = response.getString("answer");
        }
      } catch (JSONException e) {
        System.out.println("Scary. This response has either no name or no answer: " + response);
      }
    }

    if (encryptionKeyEncrypted == null) {
      return null;
    }
    if (encryptionIvEncrypted == null) {
      System.out.println("Scary. This response had an encryption key, but no IV.");
      return null;
    }

    byte[] encryptionKeyDecrypted = null;
    try {
      encryptionKeyDecrypted = decryptAsymmetric(encryptionKeyEncrypted, privateKey);
    } catch (Exception e) {
      System.out.println("Problem when trying to decrypt key for event: " + e);
      return null;
    }
    byte[] encryptionIvDecrypted = null;
    try {
      encryptionIvDecrypted = decryptAsymmetric(encryptionIvEncrypted, privateKey);
    } catch (Exception e) {
      System.out.println("Problem when trying to decrypt IV for event: " + e);
      return null;
    }
    SymmetricParameters parameters = new SymmetricParameters();
    parameters.setSecretKey(new SecretKeySpec(encryptionKeyDecrypted, "AES"));
    parameters.setIvParameterSpec(new IvParameterSpec(encryptionIvDecrypted));

    return parameters;
  }

  /**
   * Decrypt a byte array using a given private RSA key.
   * @param encrypted The byte array that was encrypted using the corresponding RSA public key
   * @param privateKey A private RSA key.
   * @return The unencrypted byte array.
   */
  private static byte[] decryptAsymmetric(String encrypted, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] encryptedBytes = Base64.getDecoder().decode(encrypted.getBytes("UTF-8"));
    byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
    return decryptedBytes;
  }

  /**
   * Decrypt a given string using the provided symmetric AES parameters
   * @param encrypted The string that was encrypted using the corresponding AES parameters
   * @param key The parameters for the decryption (key itself, and IV)
   * @return An unencrypted version of the string
   */
  private static String decryptSymmetric(String encrypted, SymmetricParameters key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, key.getSecretKey(), key.getIvParameterSpec());
    byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);
    byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
    return new String(decryptedBytes, "UTF-8");
  }

  /**
   * Converts a provided BASE64 encoded public key to the corresponding RSA key
   * @param privateKeyString A BASE64 encoded private key
   * @return A RSA Private Key
   */
  public static PrivateKey base64ToPrivateKey(String privateKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
    // NO_WRAP is used for compatibility with apache's BASE64 encoder
    byte[] decoded = Base64.getDecoder().decode(privateKeyString.getBytes("UTF-8"));
    PKCS8EncodedKeySpec pkcs8privKey = new PKCS8EncodedKeySpec(decoded);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(pkcs8privKey);
  }

  /**
   * Reads a file containing a JSON array, as exported by Paco.
   * @param fileName The name of the file
   * @return A JSON array generated using the file's contents
   */
  public static JSONArray jsonFromFile(String fileName) throws IOException, JSONException {
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    StringBuilder builder = new StringBuilder();
    String buf;
    while ((buf = reader.readLine()) != null) {
      builder.append(buf);
    }
    return new JSONArray(builder.toString());
  }

  /**
   * Writes a (decrypted) JSON array to a file
   * @param input The JSON array
   * @param outputFilename The filename of the file to write to
   */
  private static void jsonToFile(JSONArray input, String outputFilename) throws IOException {
    FileWriter writer = new FileWriter(outputFilename);
    writer.write(input.toString());
    writer.close();
  }
}