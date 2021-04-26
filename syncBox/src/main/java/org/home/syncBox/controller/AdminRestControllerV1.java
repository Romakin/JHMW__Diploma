package org.home.syncBox.controller;

import org.home.syncBox.model.User;
import org.home.syncBox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RestController
@RequestMapping(value = "/api/v1/admin/")
public class AdminRestControllerV1 {

    private final UserService userService;
    private final static int DEFAULT_SIZE = 20;

    @Autowired
    public AdminRestControllerV1(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable(name = "id") Long id) {
        User user = userService.getById(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "users/{size}/{pageNum}")
    public ResponseEntity getUsersWithPagingAndSize(@PathVariable(name = "size") int size, @PathVariable(name = "pageNum") int pageNum) {
        List<User> users = userService.getAllUsers(pageNum, size);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(value = "users/page/{pageNum}")
    public ResponseEntity getUsersWithPage(@PathVariable(name = "pageNum") int pageNum) {
        List<User> users = userService.getAllUsers(pageNum, DEFAULT_SIZE);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping(value = "users")
    public ResponseEntity getPostUsersWithPage(@RequestBody Map<String, ?> json) {
        if (!json.containsKey("pageNum")) return new ResponseEntity(HttpStatus.BAD_REQUEST);
        int pageNum = (Integer) json.get("pageNum");
        List<User> users = userService.getAllUsers(pageNum, DEFAULT_SIZE);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping(value = "users/delete")
    public ResponseEntity<User> deleteUserById(@RequestBody Map<String, ?> json) {
        if (!json.containsKey("id")) return new ResponseEntity(HttpStatus.BAD_REQUEST);
        Long id = (Long) json.get("id");
        User user = userService.getById(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        userService.delete(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping(value = "/users/update")
    public ResponseEntity updateUsers(@RequestBody List<User> users) {
        Map<String, List<String>> response = processResponseForUsersList(users, user -> userService.update(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/users/register")
    public ResponseEntity registerUsers(@RequestBody List<User> users) {
        Map<String, List<String>> response = processResponseForUsersList(users, user -> userService.register(user));
        return ResponseEntity.ok(response);
    }

    private Map<String, List<String>> processResponseForUsersList(List<User> users, Function<User, User> function) {
        List<String> successUpdatedUsernames = new ArrayList<>();
        List<String> failedUpdatedUsernames = new ArrayList<>();
        for (User user : users) {
            try {
                User userUpd = function.apply(user);
                if (userUpd != null)
                    successUpdatedUsernames.add(userUpd.getUsername());
                else
                    failedUpdatedUsernames.add(user.getUsername());
            } catch (Exception e) {
                failedUpdatedUsernames.add(user.getUsername());
            }
        }
        Map<String, List<String>> response = new HashMap<>();
        response.put("success", successUpdatedUsernames);
        response.put("failed", failedUpdatedUsernames);
        return response;
    }

}
