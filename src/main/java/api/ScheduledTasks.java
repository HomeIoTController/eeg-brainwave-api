package api;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import model.ModelGenerator;
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
    EEGDataRepository eegDataRepository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "*/60 * * * * *" )
    public void generateClassifiers() throws Exception {
        log.info("The time is now {}", dateFormat.format(new Date()));

        String mysqlUser = System.getenv("DB_USERNAME");
        String mysqlPassword = System.getenv("DB_PASSWORD");
        String databaseUrl = "jdbc:mysql://" + System.getenv("DB_HOST") + ":" + System.getenv("DB_PORT") + "/" + System.getenv("DB_NAME");
        File databaseUtilsFile = new File(System.getenv("DATABASE_UTILS_PATH"));

        ModelGenerator mg = new ModelGenerator();
        InstanceQuery instanceQuery = mg.configDBConnection(databaseUtilsFile, mysqlUser, mysqlPassword, databaseUrl);

        ArrayList<Integer> usersIds = eegDataRepository.findDistinctUserIds();

        Filter filter = new Normalize();

        // Loading instances for each user
        for (Integer userId: usersIds) {

            log.info("User ID {}", userId.toString());

            String query = "SELECT theta, lowAlpha, highAlpha, lowBeta, highBeta, " +
            "lowGamma, midGamma, attention, meditation, blink, feelingLabel " +
                    "FROM EEGData WHERE userId = " + userId.toString();

            Instances dataSet = mg.loadDatasetFromDB(instanceQuery, query);

            // Divide dataSet to train dataSet 80% and test dataSet 20%
            int trainSize = (int) Math.round(dataSet.numInstances() * 0.8);
            int testSize = dataSet.numInstances() - trainSize;

            dataSet.randomize(new Debug.Random(1));// if you comment this line the accuracy of the model will be dropped from 96.6% to 80%

            // Normalize dataSet
            filter.setInputFormat(dataSet);

            Instances dataSetNor = Filter.useFilter(dataSet, filter);

            Instances trainDataSet = new Instances(dataSetNor, 0, trainSize);
            Instances testDataSet = new Instances(dataSetNor, trainSize, testSize);

            // Create and clear models directory for user
            Path modelsDirectoryPath = Paths.get(System.getenv("MODELS_PATH"), userId.toString());
            File modelsDirectory = modelsDirectoryPath.toFile();
            if (!modelsDirectory.exists()) {
                modelsDirectory.mkdir();
            }
            FileUtils.cleanDirectory(modelsDirectory);

            // Evaluate classifier with test DataSet
            for (ModelGenerator.METHODS method : ModelGenerator.METHODS.values()) {

                log.info("Generating models for: {}", method.name());

                try {
                    // Build classifier with train DataSet
                    Classifier classifier = mg.buildClassifier(trainDataSet, method);

                    String evalSummary = mg.evaluateModel(classifier, trainDataSet, testDataSet);
                    log.info("Evaluation for {}: {}", method.name(), evalSummary);

                    // Save model
                    Path modelPath = Paths.get(modelsDirectoryPath.toString(), method.name() + ".bin");
                    mg.saveModel(classifier, modelPath.toString());
                } catch(Exception e) {
                    log.info("Failed to generate model: {}", method.name());
                    e.printStackTrace();
                }
            }
        }


    }
}
