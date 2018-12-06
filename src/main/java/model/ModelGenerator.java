package model;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.experiment.InstanceQuery;

public class ModelGenerator {

    public enum METHODS {
        MULTILAYER_PERCEPTRON,
        J48,
        RANDOM_FOREST,
        SMO
    }

    Instances loadDatasetFromFile(String path) {
        Instances dataset = null;
        try {
            dataset = DataSource.read(path);
            if (dataset.classIndex() == -1) {
                dataset.setClassIndex(dataset.numAttributes() - 1);
            }
        } catch (Exception ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
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


    public Instances loadDatasetFromDB(InstanceQuery instanceQuery, String query) {
        Instances dataset = null;
        try {
            instanceQuery.setQuery(query);
            dataset = instanceQuery.retrieveInstances();
            if (dataset.classIndex() == -1) {
                dataset.setClassIndex(dataset.numAttributes() - 1);
            }
        } catch (Exception ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return dataset;
    }

    public Classifier buildClassifier(Instances trainDataSet, METHODS method) {
        Classifier classifier;
        switch (method) {
            case MULTILAYER_PERCEPTRON:
                classifier = new MultilayerPerceptron();
                break;
            case J48:
                classifier = new J48();
                break;
            case SMO:
                classifier = new SMO();
                break;
            case RANDOM_FOREST:
                classifier = new RandomForest();
                break;
            default:
                classifier = new J48();
        }

        try {
            classifier.buildClassifier(trainDataSet);
        } catch (Exception ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return classifier;
    }

    public String evaluateModel(Classifier model, Instances trainDataSet, Instances testDataSet) {
        Evaluation eval = null;
        try {
            // Evaluate classifier with test dataset
            eval = new Evaluation(trainDataSet);
            eval.evaluateModel(model, testDataSet);
        } catch (Exception ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return eval.toSummaryString("", true);
    }

    public void saveModel(Classifier model, String modelPath) {

        try {
            SerializationHelper.write(modelPath, model);
        } catch (Exception ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}