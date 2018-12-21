package app.jobs;

import java.text.SimpleDateFormat;
import java.util.*;

import app.service.EEGDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    @Autowired
    private EEGDataService eegDataService;

    @Scheduled(cron = "*/60 * * * * *" )
    public void generateClassifiers() throws Exception {
        log.info("Generating Classifiers at {}", dateFormat.format(new Date()));
        eegDataService.generateClassifiers();
    }
}
