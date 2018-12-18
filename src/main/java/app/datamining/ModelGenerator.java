package app.datamining;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.experiment.InstanceQuery;

public class ModelGenerator {

    public enum METHODS {
        MULTILAYER_PERCEPTRON,
        RANDOM_FOREST,
        SMO
    }

    private static final Logger log = LoggerFactory.getLogger(ModelGenerator.class);

    Instances loadDataSetFromFile(String path) {
        Instances dataset = null;
        try {
            dataset = DataSource.read(path);
            if (dataset.classIndex() == -1) {
                dataset.setClassIndex(dataset.numAttributes() - 1);
            }
        } catch (Exception ex) {
            log.error("Error while loading data set!");
        }

        return dataset;
    }

    public InstanceQuery configDBConnection(File databaseUtilsFile, String user, String password, String databaseUrl) throws Exception {

        InstanceQuery instanceQuery = new InstanceQuery();

        instanceQuery.setCustomPropsFile(databaseUtilsFile);
        instanceQuery.setDatabaseURL(databaseUrl);
        instanceQuery.setUsername(user);
        instanceQuery.setPassword(password);

        return instanceQuery;
    }


    public Instances loadDataSetFromDB(InstanceQuery instanceQuery, String query) {
        Instances dataset = null;
        try {
            instanceQuery.setQuery(query);
            dataset = instanceQuery.retrieveInstances();
            if (dataset.classIndex() == -1) {
                dataset.setClassIndex(dataset.numAttributes() - 1);
            }
        } catch (Exception ex) {
            log.error("Error while loading data set!");
        }

        return dataset;
    }

    public Classifier buildClassifier(Instances trainDataSet, METHODS method) {
        Classifier classifier;
        switch (method) {
            case MULTILAYER_PERCEPTRON:
                classifier = new MultilayerPerceptron();
                break;
            case SMO:
                classifier = new SMO();
                break;
            case RANDOM_FOREST:
                classifier = new RandomForest();
                break;
            default:
                classifier = new MultilayerPerceptron();
        }

        try {
            classifier.buildClassifier(trainDataSet);
            return classifier;
        } catch (Exception ex) {
            log.error("Error while creating classifier {}!", method.name());
        }
        return null;
    }

    public String evaluateModel(Classifier model, Instances trainDataSet, Instances testDataSet) {
        Evaluation eval = null;
        try {
            // Evaluate classifier with test dataset
            eval = new Evaluation(trainDataSet);
            eval.evaluateModel(model, testDataSet);
        } catch (Exception ex) {
            log.error("Error while evaluating classifier!");
        }
        return eval.toSummaryString("", true);
    }

    public void saveModel(Classifier model, String modelPath) {

        try {
            SerializationHelper.write(modelPath, model);
        } catch (Exception ex) {
            log.error("Failed to save classifier at {}!", modelPath);
        }
    }

}