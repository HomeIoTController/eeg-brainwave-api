package app.datamining;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.filters.Filter;

public class ModelClassifier {

    private static final Logger log = LoggerFactory.getLogger(ModelClassifier.class);

    private ArrayList<String> classVal;
    private Instances dataRaw;
    private Filter filter;

    public ModelClassifier(ArrayList<String> attributeNames, ArrayList<String> classValues, String filterPath) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        classVal = new ArrayList<>(classValues);

        for(int i = 0; i < attributeNames.size(); i++) {
            String attributeName = attributeNames.get(i);
            if (i < (attributeNames.size()-1)) {
                attributes.add(new Attribute(attributeName));
            } else {
                attributes.add(new Attribute(attributeName, classVal));
            }
        }
        dataRaw = new Instances("TestInstances", attributes, 0);
        dataRaw.setClassIndex(dataRaw.numAttributes() - 1);

        try {
            filter = (Filter) SerializationHelper.read(filterPath);
        } catch (Exception e) {
            log.info("Failed to load filter from path: {}", filterPath);
            e.printStackTrace();
        }
    }


    public Instances createInstance(ArrayList<Integer> attributeValues) throws Exception {
        Instance newInstance = new DenseInstance(attributeValues.size()+1);
        for(int i = 0; i < attributeValues.size(); i++) {
            newInstance.setValue(i, attributeValues.get(i));
        }
        dataRaw.add(newInstance);

        // Normalize dataSet
        return Filter.useFilter(dataRaw, filter);
    }


    public String classify(Instances instances, String path) {
        String result = "Not classified!";

        try {
            Classifier classifier = (Classifier) SerializationHelper.read(path);
            result = classVal.get((int) classifier.classifyInstance(instances.firstInstance()));
        } catch (Exception ex) {
            log.info("Failed to classify using {}!", path);
            ex.printStackTrace();
        }

        return result;
    }

}
