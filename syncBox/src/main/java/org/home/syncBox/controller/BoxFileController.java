package org.home.syncBox.controller;

import lombok.AllArgsConstructor;
import org.home.syncBox.dto.ErrorDto;
import org.home.syncBox.service.BoxFileLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/cloud/file")
public class BoxFileController {

    @Autowired
    BoxFileLocationService locationService;

    @PostMapping
    ResponseEntity uploadBoxFile(@RequestParam("filename") String filename,
                                 @RequestParam("file") MultipartFile multipartFile) throws Exception {
        locationService.save(multipartFile.getBytes(), filename, multipartFile.getOriginalFilename());
        return ResponseEntity.ok(null);
    }

    @GetMapping
    ResponseEntity downloadBoxFile(@RequestParam("filename") String filename) throws IOException {
        Resource resource = locationService.getByName(filename);
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" +
                                resource.getFilename().replace(" ", "_")
                ).body(resource);
    }

    @DeleteMapping
    ResponseEntity delete(@RequestParam String filename) throws Exception {
        locationService.delete(filename);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping(value = "/clear")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity clear(@RequestParam String filename) throws Exception {
        locationService.clear(filename);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping
    ResponseEntity editFilename(@RequestParam String filename, @RequestBody Map<String, String> newFilenameMap) throws FileNotFoundException {
        if (newFilenameMap.containsKey("filename")){
            String acceptedFileName = locationService.setNewFilename(filename, newFilenameMap.get("filename"));
            newFilenameMap.remove("filename");
            newFilenameMap.put("name", acceptedFileName);
            return ResponseEntity.ok(newFilenameMap);
        } else  {
            throw new InvalidParameterException("Invalid parameters");
        }
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorDto> handleFileNotFoundException(FileNotFoundException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(400, e.getMessage()));
    }

    @ExceptionHandler(value = {IOException.class, NoSuchAlgorithmException.class})
    public ResponseEntity<ErrorDto> handleIOException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDto(500, e.getMessage()));
    }

    // ToDo update with sources

}
