package app.service;

import app.model.UserState;
import app.model.UserStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserStateService {

    @Autowired
    private UserStateRepository userStateRepository;

    @Autowired
    private EEGDataService eegDataService;

    public ArrayList<UserState> getByUserId(Integer userId) {
        return userStateRepository.findByUserId(userId);
    }

    public boolean updateByUserId(Integer userId, ArrayList<String> userStates) {
        ArrayList<String> oldUserStates = new ArrayList<>();
        for (UserState oldUserState : userStateRepository.findByUserId(userId)) {
            oldUserStates.add(oldUserState.getState());
        }

        if (oldUserStates.size() > 0) {
            userStateRepository.deleteByUserId(userId);
            eegDataService.deleteStatesIn(userId, oldUserStates);
        }

        for (String userState : userStates) {
            userStateRepository.save(new UserState(userId, userState));
        }
        return true;
    }
}
