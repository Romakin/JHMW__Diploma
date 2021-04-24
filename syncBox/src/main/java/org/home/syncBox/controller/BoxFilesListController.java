package org.home.syncBox.controller;

import lombok.AllArgsConstructor;
import org.home.syncBox.dto.BoxFileDto;
import org.home.syncBox.dto.ErrorDto;
import org.home.syncBox.service.BoxFileLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
@RestController
@RequestMapping(value = "/cloud/list")
public class BoxFilesListController {

    @Autowired
    BoxFileLocationService locationService;

    @GetMapping
    List<BoxFileDto> getBoxFilesList(@RequestParam int limit) {
        return locationService.getList(limit).stream()
                .map(boxFile -> new BoxFileDto(boxFile.getName(), boxFile.getSize()))
                .collect(Collectors.toList());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleFileNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDto(500, e.getMessage()));
    }
}
