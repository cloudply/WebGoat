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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
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
    NodeList nodes = null;
    File d = new File(webGoatHomeDirectory, "ClientSideFiltering/employees.xml");
    XPathFactory factory = XPathFactory.newInstance();
    XPath path = factory.newXPath();
    int columns = 5;
    List<Map<String, Object>> json = new ArrayList<>();
    java.util.Map<String, Object> employeeJson = new HashMap<>();

    try {
      Document doc = getSecureXmlDocument(d);

      StringBuilder sb = new StringBuilder();
      sb.append("/Employees/Employee/UserID | ");
      sb.append("/Employees/Employee/FirstName | ");
      sb.append("/Employees/Employee/LastName | ");
      sb.append("/Employees/Employee/SSN | ");
      sb.append("/Employees/Employee/Salary ");

      String expression = sb.toString();
      nodes = (NodeList) path.evaluate(expression, doc, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); i++) {
        if (i % columns == 0) {
          employeeJson = new HashMap<>();
          json.add(employeeJson);
        }
        Node node = nodes.item(i);
        employeeJson.put(node.getNodeName(), node.getTextContent());
      }
    } catch (XPathExpressionException e) {
      log.error("Unable to parse xml", e);
    } catch (IOException e) {
      log.error("Unable to read employees.xml at location: '{}'", d);
    }
    return json;
  }

  private Document getSecureXmlDocument(File file) throws IOException {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      // Disable external entities and DTD processing
      dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      dbFactory.setExpandEntityReferences(false);
      
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(file);
      doc.getDocumentElement().normalize();
      return doc;
    } catch (ParserConfigurationException e) {
      log.error("Parser configuration error", e);
      throw new IOException("XML parser configuration error", e);
    } catch (SAXException e) {
      log.error("SAX parsing error", e);
      throw new IOException("XML parsing error", e);
    }
  }
}
