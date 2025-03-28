package org.owasp.webgoat.lessons.deserialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class SerializationHelper {

  private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static <T> T fromString(String s, Class<T> expectedClass) throws IOException, ClassNotFoundException {
    if (s == null || s.trim().isEmpty()) {
      throw new IllegalArgumentException("Input string cannot be null or empty");
    }
    try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(s));
         ObjectInputStream ois = new ObjectInputStream(bais)) {
      Object obj = ois.readObject();
      if (!expectedClass.isInstance(obj)) {
        throw new IllegalArgumentException("Deserialized object is not of expected type: " + expectedClass.getName());
      }
      return expectedClass.cast(obj);
    }
  }

  public static String toString(Serializable o) throws IOException {
    if (o == null) {
      throw new IllegalArgumentException("Input object cannot be null");
    }
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
      return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
  }

  public static String show() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeLong(-8699352886133051976L);
    dos.close();
    byte[] longBytes = baos.toByteArray();
    return bytesToHex(longBytes);
  }

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }
}
