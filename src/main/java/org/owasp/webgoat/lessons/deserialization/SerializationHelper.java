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

  private static final int MAX_BYTES = 8192; // 8KB limit
  
  public static Object fromString(String s) throws IOException, ClassNotFoundException {
    if (s == null || s.isEmpty()) {
      throw new IllegalArgumentException("Input string cannot be null or empty");
    }
    
    byte[] data = Base64.getDecoder().decode(s);
    if (data.length > MAX_BYTES) {
      throw new IllegalArgumentException("Input size exceeds maximum allowed");
    }
    
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data)) {
      @Override
      protected Class<?> resolveClass(java.io.ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        // Only allow specific classes to be deserialized
        String className = desc.getName();
        if (!className.startsWith("org.dummy.insecure.framework.") && 
            !className.startsWith("java.lang.")) {
          throw new InvalidClassException("Unauthorized deserialization attempt", className);
        }
        return super.resolveClass(desc);
      }
    }) {
      return ois.readObject();
    }
  }

  public static String toString(Serializable o) throws IOException {
    if (o == null) {
      throw new IllegalArgumentException("Input object cannot be null");
    }
    
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
      byte[] bytes = baos.toByteArray();
      if (bytes.length > MAX_BYTES) {
        throw new IllegalArgumentException("Serialized object size exceeds maximum allowed");
      }
      return Base64.getEncoder().encodeToString(bytes);
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
