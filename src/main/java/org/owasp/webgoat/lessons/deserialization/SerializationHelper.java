package org.owasp.webgoat.lessons.deserialization;

import java.io.*;
import java.util.Base64;

public class SerializationHelper {

  private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static Object fromString(String s) throws IOException, ClassNotFoundException {
    if (s == null || s.trim().isEmpty()) {
      throw new IllegalArgumentException("Input string cannot be null or empty");
    }
    byte[] data = Base64.getDecoder().decode(s);
    try (ObjectInputStream ois = new SafeObjectInputStream(new ByteArrayInputStream(data))) {
      return ois.readObject();
    }
  }

  private static class SafeObjectInputStream extends ObjectInputStream {
    public SafeObjectInputStream(InputStream in) throws IOException {
      super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      String className = desc.getName();
      // Only allow specific classes to be deserialized
      if (className.startsWith("java.") 
          || className.startsWith("javax.") 
          || className.startsWith("org.owasp.webgoat")) {
        return super.resolveClass(desc);
      }
      throw new InvalidClassException("Unauthorized deserialization attempt", className);
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
