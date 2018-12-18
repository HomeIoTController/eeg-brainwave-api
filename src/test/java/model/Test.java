package model;

import app.datamining.ModelClassifier;
import app.datamining.ModelGenerator;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.util.HashMap;

public class Test {

    public static void main(String[] args) throws Exception {

        Dotenv dotenv = Dotenv.load();

        ModelGenerator mg = new ModelGenerator();

        String mysqlUser = dotenv.get("DB_USERNAME");
        String mysqlPassword = dotenv.get("DB_PASSWORD");
        String databaseUrl = "jdbc:mysql://" + dotenv.get("DB_HOST") + ":" + dotenv.get("DB_PORT") +"/" + dotenv.get("DB_NAME");

        ClassLoader classLoader = ModelGenerator.class.getClassLoader();
        File databaseUtilsFile = new File(classLoader.getResource("DatabaseUtils.props").toURI());

        InstanceQuery instanceQuery = mg.configDBConnection(databaseUtilsFile, mysqlUser, mysqlPassword, databaseUrl);

        String query = "SELECT theta, lowAlpha, highAlpha, lowBeta, highBeta, lowGamma, midGamma, attention, meditation, blink, state FROM EEGData";
        Instances dataSet = mg.loadDataSetFromDB(instanceQuery, query);

        Filter filter = new Normalize();

        // Divide dataSet to train dataSet 80% and test dataSet 20%
        int trainSize = (int) Math.round(dataSet.numInstances() * 0.8);
        int testSize = dataSet.numInstances() - trainSize;

        dataSet.randomize(new Debug.Random(1));// if you comment this line the accuracy of the model will be dropped from 96.6% to 80%

        // Normalize dataset
        filter.setInputFormat(dataSet);

        // Create a HashMap with class possible values
        HashMap<String, Integer> classAttrVals = new HashMap<String, Integer>();
        Attribute classAttr = filter.getOutputFormat().classAttribute();
        for (int i = 0; i < classAttr.numValues(); i++) {
            System.out.println(classAttr.value(i));
            classAttrVals.put(classAttr.value(i), i);
        }

        Instances datasetNor = Filter.useFilter(dataSet, filter);

        Instances trainDataSet = new Instances(datasetNor, 0, trainSize);
        Instances testDataset = new Instances(datasetNor, trainSize, testSize);

        System.out.println(trainDataSet);

        // Build classifier with train dataset
        MultilayerPerceptron ann = (MultilayerPerceptron) mg.buildClassifier(trainDataSet, ModelGenerator.METHODS.MULTILAYER_PERCEPTRON);

        // Evaluate classifier with test dataset
        String evalSummary = mg.evaluateModel(ann, trainDataSet, testDataset);
        System.out.println("Evaluation: " + evalSummary);

        // Save model
        String modelPath = ModelGenerator.class.getClassLoader().getResource("model.bin").getPath();
        mg.saveModel(ann, modelPath);

        // Classify a single instance
        ModelClassifier cls = new ModelClassifier();
        Instances clsInstances = Filter.useFilter(cls.createInstance(24042, 18237, 8134, 12273, 3112, 5155, 1760, 0, 0, -1), filter);
        String classname = cls.classify(clsInstances, modelPath);
        System.out.println("The class name for the instance with: \n" + clsInstances + " is " + classname);

    }

}