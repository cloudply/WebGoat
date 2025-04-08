/**
 * ************************************************************************************************
 * This file is part of WebGoat, an Open Web Application Security Project utility. For details,
 * please see http://www.owasp.org/
 *
 * <p>Copyright (c) 2002 - 2014 Bruce Mayhew
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * <p>Getting Source ==============
 *
 * <p>Source for this application is maintained at https://github.com/WebGoat/WebGoat, a repository
 * for free software projects.
 *
 * <p>
 *
 * @author WebGoat
 * @version $Id: $Id
 * @since December 12, 2015
 */
package org.owasp.webgoat.container;

import lombok.AllArgsConstructor;
import org.owasp.webgoat.container.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;

/** Security configuration for WebGoat. */
@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

  private final UserService userDetailsService;
  private final Environment environment;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/favicon.ico",
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "fonts/**",
                        "/plugins/**",
                        "/registration",
                        "/register.mvc",
                        "/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            login ->
                login
                    .loginPage("/login")
                    .defaultSuccessUrl("/welcome.mvc", true)
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll())
        .oauth2Login(
            oidc -> {
              oidc.defaultSuccessUrl("/login-oauth.mvc");
              oidc.loginPage("/login");
            })
        .logout(logout -> logout.deleteCookies("JSESSIONID").invalidateHttpSession(true))
        .headers(headers -> headers.disable())
        .exceptionHandling(
            handling ->
                handling.authenticationEntryPoint(new AjaxAuthenticationEntryPoint("/login")));

    // Check if we're running in a test environment
    if (isTestProfile()) {
        http.csrf(csrf -> csrf.disable());
    } else {
        // In production, enable CSRF but ignore specific paths
        http.csrf(csrf -> csrf.ignoringRequestMatchers(
            "/favicon.ico", 
            "/css/**", 
            "/images/**", 
            "/js/**", 
            "fonts/**", 
            "/plugins/**",
            "/WebGoat/**",  // Add WebGoat endpoints that need CSRF exceptions
            "/service/**"   // Add service endpoints that need CSRF exceptions
        ));
    }

    return http.build();
  }

  private boolean isTestProfile() {
    for (String profile : environment.getActiveProfiles()) {
        if (profile.contains("test") || profile.contains("webgoat-test")) {
            return true;
        }
    }
    return false;
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService);
  }

  @Bean
  public UserDetailsService userDetailsServiceBean() {
    return userDetailsService;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public NoOpPasswordEncoder passwordEncoder() {
    return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
  }
}
