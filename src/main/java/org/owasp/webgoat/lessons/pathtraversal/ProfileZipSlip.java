package org.owasp.webgoat.lessons.pathtraversal;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.owasp.webgoat.container.assignments.AssignmentHints;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.owasp.webgoat.container.session.WebSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AssignmentHints({
  "path-traversal-zip-slip.hint1",
  "path-traversal-zip-slip.hint2",
  "path-traversal-zip-slip.hint3",
  "path-traversal-zip-slip.hint4"
})
@Slf4j
public class ProfileZipSlip extends ProfileUploadBase {

  public ProfileZipSlip(
      @Value("${webgoat.server.directory}") String webGoatHomeDirectory, WebSession webSession) {
    super(webGoatHomeDirectory, webSession);
  }

  @PostMapping(
      value = "/PathTraversal/zip-slip",
      consumes = ALL_VALUE,
      produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public AttackResult uploadFileHandler(@RequestParam("uploadedFileZipSlip") MultipartFile file) {
    if (!file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
      return failed(this).feedback("path-traversal-zip-slip.no-zip").build();
    } else {
      return processZipUpload(file);
    }
  }

  @SneakyThrows
  private AttackResult processZipUpload(MultipartFile file) {
    var tmpZipDirectory = Files.createTempDirectory(getWebSession().getUserName());
    cleanupAndCreateDirectoryForUser();
    var currentImage = getProfilePictureAsBase64();

    try {
      var uploadedZipFile = tmpZipDirectory.resolve(file.getOriginalFilename());
      FileCopyUtils.copy(file.getBytes(), uploadedZipFile.toFile());

      try (ZipFile zip = new ZipFile(uploadedZipFile.toFile())) {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
          ZipEntry e = entries.nextElement();
          if (e.isDirectory()) {
            continue; // Skip directories
          }
          
          // Get the entry name and sanitize it
          String entryName = e.getName();
          
          // Skip entries with suspicious paths
          if (entryName.contains("..") || entryName.startsWith("/") || entryName.startsWith("\\")) {
            continue;
          }
          
          // Create a file object for the target path
          File f = new File(tmpZipDirectory.toFile(), entryName);
          
          // Ensure the normalized path is within the target directory
          if (!f.getCanonicalPath().startsWith(tmpZipDirectory.toFile().getCanonicalPath())) {
            continue; // Skip this entry as it's trying to write outside the target directory
          }
          
          // Create parent directories if needed
          File parentDir = f.getParentFile();
          if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
          }
          
          // Copy the file
          try (InputStream is = zip.getInputStream(e)) {
            Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
          }
        }
      }

      return isSolved(currentImage, getProfilePictureAsBase64());
    } catch (IOException e) {
      log.error("Error processing zip file", e);
      return failed(this).output(e.getMessage()).build();
    }
  }

  private AttackResult isSolved(byte[] currentImage, byte[] newImage) {
    if (Arrays.equals(currentImage, newImage)) {
      return failed(this).output("path-traversal-zip-slip.extracted").build();
    }
    return success(this).output("path-traversal-zip-slip.extracted").build();
  }

  @GetMapping("/PathTraversal/zip-slip/")
  @ResponseBody
  public ResponseEntity<?> getProfilePicture() {
    return super.getProfilePicture();
  }

  @GetMapping("/PathTraversal/zip-slip/profile-image/{username}")
  @ResponseBody
  public ResponseEntity<?> getProfilePicture(@PathVariable("username") String username) {
    return ResponseEntity.notFound().build();
  }
}
