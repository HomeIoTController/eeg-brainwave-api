package app.controller;

import app.model.EEGData;
import app.service.EEGDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;


@Controller    // This means that this class is a Controller
@RequestMapping(path="/eeg") // This means URL's start with /eeg (after Application path)
public class EEGController {

    @Autowired
    private EEGDataService eegDataService;

    @PostMapping("/classify")
    public @ResponseBody
    HashMap<String, String> classify(@RequestBody EEGData eegData) throws Exception {
        return eegDataService.classify(eegData);
    }
}