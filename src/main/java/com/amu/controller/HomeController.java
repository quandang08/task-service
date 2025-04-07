package com.amu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {
    @GetMapping("/tasks")
    public ResponseEntity<String> getAssignedUsersTask(){

        return new ResponseEntity<>("Welcome to task serviceb",HttpStatus.OK);
    }
}
