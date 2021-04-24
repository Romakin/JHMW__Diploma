package org.home.syncBox.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.home.syncBox.model.BoxFile;
import org.home.syncBox.model.Status;
import org.home.syncBox.model.User;
import org.home.syncBox.repository.BoxFileDbRepository;
import org.home.syncBox.repository.FileSystemRepository;
import org.home.syncBox.repository.UserRepository;
import org.home.syncBox.service.BoxFileLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

@Service
@Slf4j
public class BoxFileLocationServiceImpl implements BoxFileLocationService {

    final FileSystemRepository fileSystemRepository;

    final BoxFileDbRepository dbRepository;

    final UserRepository userRepository;

    @Autowired
    public BoxFileLocationServiceImpl(FileSystemRepository fileSystemRepository, BoxFileDbRepository dbRepository, UserRepository userRepository) {
        this.fileSystemRepository = fileSystemRepository;
        this.dbRepository = dbRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Long save(byte[] bytes, String fileName, String originalName) throws IOException, NoSuchAlgorithmException {
        String location = fileSystemRepository.save(bytes, fileName);
        BoxFile boxFile = new BoxFile();
        boxFile.setName(fileName);
        boxFile.setLocation(location);
        boxFile.setSize((long) (Long.valueOf(bytes.length) / 1024.0));
        boxFile.setHumanSize(humanReadableByteCountBin(bytes.length));
        boxFile.setHash(SHAsum(bytes));
        boxFile.setOriginalName(originalName);
        boxFile.setUser(getUser());
        boxFile.setCreated(new Date());
        boxFile.setUpdated(new Date());
        boxFile.setStatus(Status.ACTIVE);
        return dbRepository.save(boxFile).getId();
    }

    @Override
    public String setNewFilename(String fileName, String newFileName) throws FileNotFoundException {
        BoxFile boxFile = dbRepository.getBoxFileByName(fileName).orElseThrow(FileNotFoundException::new);
        boxFile.setName(newFileName);
        dbRepository.save(boxFile);
        return boxFile.getName();
    }

    @Override
    public void delete(String filename) throws FileNotFoundException {
        BoxFile boxFile = dbRepository.getBoxFileByName(filename)
                .orElseThrow(FileNotFoundException::new);
        boxFile.setStatus(Status.DELETED);
        boxFile.setUpdated(new Date());
        dbRepository.save(boxFile);
    }

    @Override
    public void clear(String filename) throws FileNotFoundException, IOException {
        BoxFile boxFile = dbRepository.getBoxFileByName(filename)
                .orElseThrow(FileNotFoundException::new);
        dbRepository.delete(boxFile);
        if(!fileSystemRepository.remove(boxFile.getLocation()))
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Resource getByName(String filename) throws FileNotFoundException {
        BoxFile boxFile = dbRepository.getBoxFileByName(filename)
                .orElseThrow(FileNotFoundException::new);
        return fileSystemRepository.findInFileSystem(boxFile.getLocation());
    }

    @Override
    public List<BoxFile> getList(int limit) {
        Pageable pageWithSomeElements = PageRequest.of(0, limit);
        return dbRepository.getBoxFileByUsernameAndStatusPageable(getUser(), Status.ACTIVE, pageWithSomeElements);
    }

    @Override
    public FileSystemResource findById(Long id) throws FileNotFoundException {
        BoxFile boxFile = dbRepository.findById(id)
                .orElseThrow(FileNotFoundException::new);
        return fileSystemRepository.findInFileSystem(boxFile.getLocation());
    }

    private User getUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    private String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    private String SHAsum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        Formatter formatter = new Formatter();
        for (byte b : md.digest(bytes))
            formatter.format("%02x", b);
        return formatter.toString();
    }
}
