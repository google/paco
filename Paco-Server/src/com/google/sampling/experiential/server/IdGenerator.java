package com.google.sampling.experiential.server;

import java.math.BigInteger;

public class IdGenerator {
  public static BigInteger generate(BigInteger version, Integer order) { 
    BigInteger field1 = version.multiply(new BigInteger("1000"));
    Integer field2 = order * 1000;
    String concatInt = field1.toString().concat(field2.toString());
    BigInteger ret = BigInteger.valueOf(Long.parseLong(concatInt));
    return ret;
  }
}
