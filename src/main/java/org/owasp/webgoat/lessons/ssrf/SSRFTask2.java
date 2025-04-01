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

package org.owasp.webgoat.lessons.ssrf;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.owasp.webgoat.container.assignments.AssignmentEndpoint;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AssignmentHints({"ssrf.hint3"})
public class SSRFTask2 extends AssignmentEndpoint {

  @PostMapping("/SSRF/task2")
  @ResponseBody
  public AttackResult completed(@RequestParam String url) {
    return furBall(url);
  }

  protected AttackResult furBall(String url) {
    // Strict URL validation
    if (!isValidUrl(url)) {
      return getFailedResult("Invalid URL format");
    }

    // Whitelist check
    if (!url.equals("http://ifconfig.pro")) {
      return getFailedResult("Only http://ifconfig.pro is allowed");
    }

    String html;
    try {
      URL targetUrl = new URL(url);
      // Additional security checks
      if (!targetUrl.getHost().equals("ifconfig.pro") || 
          !targetUrl.getProtocol().equals("http")) {
        return getFailedResult("Invalid target host or protocol");
      }

      try (InputStream in = targetUrl.openStream()) {
        html = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("\n", "<br>"); // Otherwise the \n gets escaped in the response
      }
    } catch (MalformedURLException e) {
      return getFailedResult("Malformed URL: " + e.getMessage());
    } catch (IOException e) {
      // in case the external site is down, the test and lesson should still be ok
      html = "<html><body>Although the http://ifconfig.pro site is down, you still managed to solve"
             + " this exercise the right way!</body></html>";
    }
    return success(this).feedback("ssrf.success").output(html).build();
  }

  private boolean isValidUrl(String url) {
    try {
      new URL(url);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }

  private AttackResult getFailedResult(String errorMsg) {
    return failed(this).feedback("ssrf.failure").output(errorMsg).build();
  }
}
