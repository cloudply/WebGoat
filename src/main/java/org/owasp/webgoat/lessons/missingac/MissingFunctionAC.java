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

package org.owasp.webgoat.lessons.missingac;

import org.owasp.webgoat.container.lessons.Category;
import org.owasp.webgoat.container.lessons.Lesson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MissingFunctionAC extends Lesson {

  // Keep these static fields for backward compatibility with existing code
  public static String PASSWORD_SALT_SIMPLE;
  public static String PASSWORD_SALT_ADMIN;

  @Value("${webgoat.password.salt.simple:}")
  private void setPasswordSaltSimple(String value) {
    // If no value is provided in properties, use a default value
    PASSWORD_SALT_SIMPLE = (value != null && !value.isEmpty()) ? value : "DeliberatelyInsecure1234";
  }
  
  @Value("${webgoat.password.salt.admin:}")
  private void setPasswordSaltAdmin(String value) {
    // If no value is provided in properties, use a default value
    PASSWORD_SALT_ADMIN = (value != null && !value.isEmpty()) ? value : "DeliberatelyInsecureAdmin1234";
  }

  @Override
  public Category getDefaultCategory() {
    return Category.A1;
  }

  @Override
  public String getTitle() {
    return "missing-function-access-control.title";
  }
}
