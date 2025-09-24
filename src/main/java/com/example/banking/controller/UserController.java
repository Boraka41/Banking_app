package com.example.banking.controller;

import com.example.banking.dto.responses.UserResponse;
import com.example.banking.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UserResponse userResponse = userService.me(authentication);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> listAll() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getOne(@PathVariable Long id) {
        return userService.getById(id);
    }

}
