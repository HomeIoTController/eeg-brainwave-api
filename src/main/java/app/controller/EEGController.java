package app.controller;

import app.model.EEGData;
import app.model.EEGDataRepository;
import app.model.UserStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;


@Controller    // This means that this class is a Controller
@RequestMapping(path="/eeg") // This means URL's start with /eeg (after Application path)
public class EEGController {

    private static final Logger log = LoggerFactory.getLogger(EEGController.class);

    @Autowired // This means to get the bean called eegDataRepository
    private EEGDataRepository eegDataRepository;

    @Autowired
    private UserStateRepository userStateRepository;

    @GetMapping("/get")
    public @ResponseBody
    Iterable<EEGData> get() {
        return eegDataRepository.findAll();
    }

    @PostMapping("/classify")
    public @ResponseBody
    HashMap<String, String> classify(@RequestBody EEGData eegData) throws Exception {
        return eegData.classify(userStateRepository.findByUserId(eegData.getUserId()));
    }

}