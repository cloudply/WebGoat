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

package org.owasp.webgoat.lessons.clientsidefiltering;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@RestController
@Slf4j
public class Salaries {

  @Value("${webgoat.user.directory}")
  private String webGoatHomeDirectory;

  @PostConstruct
  public void copyFiles() {
    ClassPathResource classPathResource = new ClassPathResource("lessons/employees.xml");
    File targetDirectory = new File(webGoatHomeDirectory, "/ClientSideFiltering");
    if (!targetDirectory.exists()) {
      targetDirectory.mkdir();
    }
    try {
      FileCopyUtils.copy(
          classPathResource.getInputStream(),
          new FileOutputStream(new File(targetDirectory, "employees.xml")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping("clientSideFiltering/salaries")
  @ResponseBody
  public List<Map<String, Object>> invoke() {
    List<Map<String, Object>> json = new ArrayList<>();
    Map<String, Object> employeeJson = null;
    File employeeFile = new File(webGoatHomeDirectory, "ClientSideFiltering/employees.xml");

    try (InputStream is = new FileInputStream(employeeFile)) {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      // Disable external entities and DTD processing
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      
      XMLStreamReader reader = factory.createXMLStreamReader(is);
      
      String currentElement = "";
      while (reader.hasNext()) {
        int event = reader.next();
        
        switch (event) {
          case XMLStreamConstants.START_ELEMENT:
            currentElement = reader.getLocalName();
            if ("Employee".equals(currentElement)) {
              employeeJson = new HashMap<>();
              json.add(employeeJson);
            }
            break;
            
          case XMLStreamConstants.CHARACTERS:
            if (employeeJson != null && !reader.isWhiteSpace()) {
              String text = reader.getText().trim();
              if (!text.isEmpty() && isRelevantField(currentElement)) {
                employeeJson.put(currentElement, text);
              }
            }
            break;
        }
      }
      reader.close();
    } catch (IOException | XMLStreamException e) {
      log.error("Error processing employees.xml at location: '{}'", employeeFile, e);
    }
    return json;
  }
  
  private boolean isRelevantField(String fieldName) {
    return "UserID".equals(fieldName) 
        || "FirstName".equals(fieldName)
        || "LastName".equals(fieldName)
        || "SSN".equals(fieldName)
        || "Salary".equals(fieldName);
  }
}
