package app.controller;

import app.model.UserState;
import app.service.UserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller    // This means that this class is a Controller
@RequestMapping(path="/user") // This means URL's start with /user (after Application path)
public class UserStateController {

    @Autowired
    private UserStateService userStateService;

    @GetMapping("/{userId}/states")
    public @ResponseBody
    Iterable<UserState> getByUserId(@PathVariable("userId") int userId) {
        return userStateService.getByUserId(userId);
    }

    @PostMapping("/{userId}/states")
    public @ResponseBody
    Boolean updateByUserId(@PathVariable("userId") Integer userId, @RequestBody ArrayList<String> userStates) {
        return userStateService.updateByUserId(userId, userStates);
    }
}