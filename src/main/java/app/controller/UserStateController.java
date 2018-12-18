package app.controller;

import app.model.UserState;
import app.model.UserStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@Controller    // This means that this class is a Controller
@RequestMapping(path="/userState") // This means URL's start with /userState (after Application path)
public class UserStateController {

    private static final Logger log = LoggerFactory.getLogger(UserStateController.class);

    @Autowired // This means to get the bean called userStateRepository
    private UserStateRepository userStateRepository;

    @GetMapping("/get/{userId}")
    public @ResponseBody
    Iterable<UserState> getByUserId(@PathVariable("userId") int userId) {
        return userStateRepository.findByUserId(userId);
    }

    @PostMapping("/update/:userId")
    public @ResponseBody
    Boolean updateByUserId(@RequestBody ArrayList<UserState> userStates) {
        userStateRepository.deleteAll();
        userStateRepository.saveAll(userStates);
        return true;
    }
}