package app.jobs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import app.datamining.ModelGenerator;
import app.model.EEGDataRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.core.Debug;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

@Component
public class ScheduledTasks {

    @Autowired
    private EEGDataRepository eegDataRepository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private final Integer minNumberOfClasses = 2;

    private ModelGenerator modelGenerator;
    private InstanceQuery instanceQuery;
    private Filter filter;

    {
        String mysqlUser = System.getenv("DB_USERNAME");
        String mysqlPassword = System.getenv("DB_PASSWORD");
        String databaseUrl = "jdbc:mysql://" + System.getenv("DB_HOST") + ":" + System.getenv("DB_PORT") + "/" + System.getenv("DB_NAME");
        File databaseUtilsFile = new File(System.getenv("DATABASE_UTILS_PATH"));

        try {
            filter = new Normalize();
            modelGenerator = new ModelGenerator();
            instanceQuery = modelGenerator.configDBConnection(databaseUtilsFile, mysqlUser, mysqlPassword, databaseUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "*/60 * * * * *" )
    public void generateClassifiers() throws Exception {
        log.info("Generating Classifiers at {}", dateFormat.format(new Date()));

        /* We want to filter users that have EEG data labeled for at least 2 classes  */
        HashMap<Integer, Integer> users = new HashMap<>();
        for (Object data : eegDataRepository.findDistinctUserIdsAndStates()) {
            Object[] eegData = (Object[]) data;
            Integer userId = (Integer) eegData[0];
            users.putIfAbsent(userId, 0);
            users.replace(userId, users.get(userId) + 1);
        }

        List<Integer> usersIds = users.entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= minNumberOfClasses)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("Number of users: {}", usersIds.size());

        // Loading instances for each user
        for (Integer userId: usersIds) {

            log.info("User ID: {}", userId.toString());

            String query = "SELECT theta, lowAlpha, highAlpha, lowBeta, highBeta, " +
            "lowGamma, midGamma, attention, meditation, blink, state " +
                    "FROM EEGData WHERE deleted IS NULL AND state != '?' AND userId = " + userId.toString();

            Instances dataSet = modelGenerator.loadDataSetFromDB(instanceQuery, query);

            // Divide dataSet to train dataSet 80% and test dataSet 20%
            int trainSize = (int) Math.round(dataSet.numInstances() * 0.8);
            int testSize = dataSet.numInstances() - trainSize;

            dataSet.randomize(new Debug.Random(1));// if you comment this line the accuracy of the model will be dropped from 96.6% to 80%

            // Normalize dataSet - 0 to 1
            filter.setInputFormat(dataSet);

            Instances dataSetNor = Filter.useFilter(dataSet, filter);

            Instances trainDataSet = new Instances(dataSetNor, 0, trainSize);
            Instances testDataSet = new Instances(dataSetNor, trainSize, testSize);

            // Create and clear the user models directory
            Path modelsDirectoryPath = Paths.get(System.getenv("MODELS_PATH"), userId.toString());
            File modelsDirectory = modelsDirectoryPath.toFile();
            if (!modelsDirectory.exists()) {
                modelsDirectory.mkdir();
            }
            FileUtils.cleanDirectory(modelsDirectory);

            // Save filter for future use while classifying instances
            Path filterPath = Paths.get(modelsDirectoryPath.toString(), "FILTER.bin");
            modelGenerator.saveFilter(filter, filterPath.toString());

            // Create and evaluate classifiers with data set
            for (ModelGenerator.METHODS method : ModelGenerator.METHODS.values()) {

                log.info("Generating model for: {}", method.name());

                try {
                    // Build classifier with train DataSet
                    Classifier classifier = modelGenerator.buildClassifier(trainDataSet, method);

                    String evalSummary = modelGenerator.evaluateModel(classifier, trainDataSet, testDataSet);
                    log.info("Evaluation for {}: {}", method.name(), evalSummary);

                    // Save model
                    Path modelPath = Paths.get(modelsDirectoryPath.toString(), method.name() + ".bin");
                    modelGenerator.saveModel(classifier, modelPath.toString());

                } catch(Exception e) {
                    log.info("Failed to generate model: {}", method.name());
                    e.printStackTrace();
                }
            }
        }
    }
}
