package app.controller;

import app.model.EEGDataRepository;
import app.model.UserState;
import app.model.UserStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@Controller    // This means that this class is a Controller
@RequestMapping(path="/user") // This means URL's start with /user (after Application path)
public class UserStateController {

    private static final Logger log = LoggerFactory.getLogger(UserStateController.class);

    @Autowired // This means to get the bean called userStateRepository
    private UserStateRepository userStateRepository;
    @Autowired
    private EEGDataRepository eegDataRepository;

    @GetMapping("/{userId}/states")
    public @ResponseBody
    Iterable<UserState> getByUserId(@PathVariable("userId") int userId) {
        return userStateRepository.findByUserId(userId);
    }

    @PostMapping("/{userId}/states")
    public @ResponseBody
    Boolean updateByUserId(@PathVariable("userId") int userId, @RequestBody ArrayList<String> userStates) {
        ArrayList<String> oldUserStates = new ArrayList<>();
        for (UserState oldUserState : userStateRepository.findByUserId(userId)) {
            oldUserStates.add(oldUserState.getState());
        }
        eegDataRepository.deleteStatesIn(oldUserStates);
        userStateRepository.deleteByUserId(userId);
        for (String userState : userStates) {
            userStateRepository.save(new UserState(userId, userState));
        }
        return true;
    }
}