package model;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.*;

public class ModelClassifier {

    private ArrayList<Attribute> attributes;
    private ArrayList<String> classVal;
    private Instances dataRaw;


    public ModelClassifier() {
        attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("theta"));
        attributes.add(new Attribute("lowAlpha"));
        attributes.add(new Attribute("highAlpha"));
        attributes.add(new Attribute("lowBeta"));
        attributes.add(new Attribute("highBeta"));
        attributes.add(new Attribute("lowGamma"));
        attributes.add(new Attribute("midGamma"));
        attributes.add(new Attribute("attention"));
        attributes.add(new Attribute("meditation"));
        attributes.add(new Attribute("blink"));


        classVal = new ArrayList<String>();
        classVal.add("OPEN");
        classVal.add("HAPPY");
        classVal.add("ALIVE");
        classVal.add("GOOD");
        classVal.add("LOVE");
        classVal.add("INTERESTED");
        classVal.add("POSITIVE");
        classVal.add("STRONG");
        classVal.add("ANGRY");
        classVal.add("DEPRESSED");
        classVal.add("CONFUSED");
        classVal.add("HELPLESS");
        classVal.add("INDIFFERENT");
        classVal.add("AFRAID");
        classVal.add("HURT");
        classVal.add("SAD");
        attributes.add(new Attribute("feelingLabel", classVal));

        dataRaw = new Instances("TestInstances", attributes, 0);
        dataRaw.setClassIndex(dataRaw.numAttributes() - 1);
    }


    public Instances createInstance(int theta, int lowAlpha, int highAlpha, int lowBeta, int highBeta, int lowGamma, int midGamma, int attention, int meditation, int blink) {
        dataRaw.clear();
        Instance newInstance = new DenseInstance(11);
        newInstance.setValue(0, theta);
        newInstance.setValue(1, lowAlpha);
        newInstance.setValue(2, highAlpha);
        newInstance.setValue(3, lowBeta);
        newInstance.setValue(4, highBeta);
        newInstance.setValue(5, lowGamma);
        newInstance.setValue(6, midGamma);
        newInstance.setValue(7, attention);
        newInstance.setValue(8, meditation);
        newInstance.setValue(9, blink);

        dataRaw.add(newInstance);
        return dataRaw;
    }


    public String classify(Instances insts, String path) {
        String result = "Not classified!";
        Classifier cls;
        try {
            cls = (MultilayerPerceptron) SerializationHelper.read(path);
            System.out.println(cls.classifyInstance(insts.firstInstance()));
            result = classVal.get((int) cls.classifyInstance(insts.firstInstance()));
        } catch (Exception ex) {
            Logger.getLogger(ModelClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}