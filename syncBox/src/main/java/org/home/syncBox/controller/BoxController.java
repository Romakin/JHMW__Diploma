package org.home.syncBox.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping(value = {"/file", "/list"})
@CrossOrigin(origins = "*", maxAge = 3600)
public class BoxController {



}
