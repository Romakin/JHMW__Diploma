package org.home.syncBox.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.home.syncBox.model.BoxFile;
import org.home.syncBox.model.Status;
import org.home.syncBox.model.User;
import org.home.syncBox.repository.BoxFileDbRepository;
import org.home.syncBox.repository.FileSystemRepository;
import org.home.syncBox.repository.UserRepository;
import org.home.syncBox.service.BoxFileLocationService;
import org.home.syncBox.service.utils.BoxFileLoactionUtilsService;
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

    private final FileSystemRepository fileSystemRepository;
    private final BoxFileDbRepository dbRepository;
    private final UserRepository userRepository;

    @Autowired
    public BoxFileLocationServiceImpl(FileSystemRepository fileSystemRepository, BoxFileDbRepository dbRepository, UserRepository userRepository) {
        this.fileSystemRepository = fileSystemRepository;
        this.dbRepository = dbRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Long save(byte[] bytes, String fileName, String originalName) throws IOException, NoSuchAlgorithmException {
        String location = fileSystemRepository.save(bytes, fileName);
        BoxFile boxFile = BoxFile.builder()
                .name(fileName)
                .location(location)
                .size((long) (Long.valueOf(bytes.length) / 1024.0))
                .humanSize(BoxFileLoactionUtilsService.humanReadableByteCountBin(bytes.length))
                .hash(BoxFileLoactionUtilsService.SHAsum(bytes))
                .originalName(originalName)
                .user(getUser())
                .created(new Date())
                .updated(new Date())
                .status(Status.ACTIVE).build();
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
    public void clear(String filename) throws IOException {
        BoxFile boxFile = dbRepository.getBoxFileByName(filename)
                .orElseThrow(FileNotFoundException::new);
        if (fileSystemRepository.remove(boxFile.getLocation()))
            dbRepository.delete(boxFile);
        else
            throw new FileNotFoundException();
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
        return dbRepository.findBoxFileByUserUsernameAndStatus(getUser().getUsername(), Status.ACTIVE, pageWithSomeElements);
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


}
