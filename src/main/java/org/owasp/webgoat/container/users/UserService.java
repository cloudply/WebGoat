package org.owasp.webgoat.container.users;

import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.flywaydb.core.Flyway;
import org.owasp.webgoat.container.lessons.Initializeable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author nbaars
 * @since 3/19/17.
 */
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserProgressRepository userTrackerRepository;
  private final JdbcTemplate jdbcTemplate;
  private final Function<String, Flyway> flywayLessons;
  private final List<Initializeable> lessonInitializables;

  @Override
  public WebGoatUser loadUserByUsername(String username) throws UsernameNotFoundException {
    WebGoatUser webGoatUser = userRepository.findByUsername(username);
    if (webGoatUser == null) {
      throw new UsernameNotFoundException("User not found");
    } else {
      webGoatUser.createUser();
      lessonInitializables.forEach(l -> l.initialize(webGoatUser));
    }
    return webGoatUser;
  }

  public void addUser(String username, String password) {
    // get user if there exists one by the name
    var userAlreadyExists = userRepository.existsByUsername(username);
    var webGoatUser = userRepository.save(new WebGoatUser(username, password));

    if (!userAlreadyExists) {
      userTrackerRepository.save(
          new UserProgress(username)); // if user previously existed it will not get another tracker
      createLessonsForUser(webGoatUser);
    }
  }

  private void createLessonsForUser(WebGoatUser webGoatUser) {
    // Validate username to prevent SQL injection
    if (!isValidSchemaName(webGoatUser.getUsername())) {
      throw new IllegalArgumentException("Invalid schema name");
    }
    
    // Use the validated username in the SQL statement
    jdbcTemplate.execute("CREATE SCHEMA \"" + webGoatUser.getUsername() + "\" authorization dba");
    flywayLessons.apply(webGoatUser.getUsername()).migrate();
  }
  
  /**
   * Validates that the schema name contains only allowed characters
   * This prevents SQL injection in schema names
   */
  private boolean isValidSchemaName(String schemaName) {
    // Only allow alphanumeric characters and underscores
    return schemaName != null && schemaName.matches("^[a-zA-Z0-9_]+$");
  }

  public List<WebGoatUser> getAllUsers() {
    return userRepository.findAll();
  }
}
