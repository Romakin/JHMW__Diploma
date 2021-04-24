package org.home.syncBox.service;

import org.home.syncBox.model.BoxFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface BoxFileLocationService {

    Long save(byte[] bytes, String fileName, String originalName) throws IOException, NoSuchAlgorithmException;

    String setNewFilename(String fileName, String newFileName) throws FileNotFoundException;

    void delete(String filename) throws FileNotFoundException;

    void clear(String filename) throws FileNotFoundException, IOException;

    FileSystemResource findById(Long id) throws FileNotFoundException;

    Resource getByName(String filename) throws FileNotFoundException;

    List<BoxFile> getList(int limit);
}
