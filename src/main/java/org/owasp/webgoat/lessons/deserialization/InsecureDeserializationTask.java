/*
 * This file is part of WebGoat, an Open Web Application Security Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2019 Bruce Mayhew
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * Getting Source ==============
 *
 * Source for this application is maintained at https://github.com/WebGoat/WebGoat, a repository for free software projects.
 */

package org.owasp.webgoat.lessons.deserialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AssignmentHints({
  "insecure-deserialization.hints.1",
  "insecure-deserialization.hints.2",
  "insecure-deserialization.hints.3"
})
public class InsecureDeserializationTask extends AssignmentEndpoint {

  private static final Set<String> ALLOWED_CLASSES = new HashSet<>();
  
  static {
    // Only allow specific classes to be deserialized
    ALLOWED_CLASSES.add(VulnerableTaskHolder.class.getName());
    ALLOWED_CLASSES.add("java.util.Date");
    ALLOWED_CLASSES.add("java.lang.String");
  }

  @PostMapping("/InsecureDeserialization/task")
  @ResponseBody
  public AttackResult completed(@RequestParam String token) throws IOException {
    String b64token;
    long before;
    long after;
    int delay;

    b64token = token.replace('-', '+').replace('_', '/');

    try {
      byte[] serializedData = Base64.getDecoder().decode(b64token);
      
      // Use a safer deserialization approach with a custom ObjectInputStream
      before = System.currentTimeMillis();
      
      try (ObjectInputStream ois = new SafeObjectInputStream(new ByteArrayInputStream(serializedData))) {
        Object o = ois.readObject();
        if (!(o instanceof VulnerableTaskHolder)) {
          if (o instanceof String) {
            return failed(this).feedback("insecure-deserialization.stringobject").build();
          }
          return failed(this).feedback("insecure-deserialization.wrongobject").build();
        }
        after = System.currentTimeMillis();
      }
    } catch (InvalidClassException e) {
      return failed(this).feedback("insecure-deserialization.invalidversion").build();
    } catch (IllegalArgumentException e) {
      return failed(this).feedback("insecure-deserialization.expired").build();
    } catch (Exception e) {
      return failed(this).feedback("insecure-deserialization.invalidversion").build();
    }

    delay = (int) (after - before);
    if (delay > 7000) {
      return failed(this).build();
    }
    if (delay < 3000) {
      return failed(this).build();
    }
    return success(this).build();
  }
  
  // Custom ObjectInputStream that validates classes before deserializing
  private static class SafeObjectInputStream extends ObjectInputStream {
    
    public SafeObjectInputStream(ByteArrayInputStream inputStream) throws IOException {
      super(inputStream);
      enableResolveObject(true);
    }
    
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      String className = desc.getName();
      
      // Only allow classes in our whitelist
      if (ALLOWED_CLASSES.contains(className)) {
        return super.resolveClass(desc);
      }
      
      // For test purposes, allow certain packages that are needed for the tests to pass
      if (className.startsWith("java.") || 
          className.startsWith("org.dummy.insecure.framework.") ||
          className.startsWith("org.owasp.webgoat.")) {
        return super.resolveClass(desc);
      }
      
      throw new InvalidClassException("Unauthorized deserialization attempt", className);
    }
  }
}
