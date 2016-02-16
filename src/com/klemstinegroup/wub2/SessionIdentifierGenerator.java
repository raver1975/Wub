package com.klemstinegroup.wub2;
import java.security.SecureRandom;
import java.math.BigInteger;

public final class SessionIdentifierGenerator {
  private SecureRandom random = new SecureRandom();

  public String nextSessionId() {
    return new BigInteger(130, random).toString(32);
  }
}