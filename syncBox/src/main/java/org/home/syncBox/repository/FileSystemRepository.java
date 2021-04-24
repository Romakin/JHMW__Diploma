package org.home.syncBox.repository;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Repository
public class FileSystemRepository {

    private String RESOURCES_DIR = FileSystemRepository.class.getResource("/").getPath();

    public String save(byte[] content, String fileName) throws IOException {
        Path newFile = Paths.get(RESOURCES_DIR + new Date().getTime() + "_" + fileName);
        Files.createDirectories(newFile.getParent());
        Files.write(newFile, content);
        return newFile.toAbsolutePath().toString();
    }

    public boolean remove(String location) throws IOException {
        Path file = Paths.get(location);
        return Files.deleteIfExists(file);
    }

     public FileSystemResource findInFileSystem(String location) {
        try {
            return new FileSystemResource(Paths.get(location));
        } catch (Exception ex) {
            // Handle access or file not found problems.
//            throw new RuntimeException();
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Actor Not Found", ex);
        }
    }

}
