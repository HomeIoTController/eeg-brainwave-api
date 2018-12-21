package app.model;

import app.datamining.ModelClassifier;
import app.datamining.ModelGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.*;

@Entity
@Table(name = "EEGData")
public class EEGData {

    private static final Logger log = LoggerFactory.getLogger(EEGData.class);

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    private Integer userId;
    private Date time;
    private Integer theta;
    private Integer lowAlpha;
    private Integer highAlpha;
    private Integer lowBeta;
    private Integer highBeta;
    private Integer lowGamma;
    private Integer midGamma;
    private Integer attention;
    private Integer meditation;
    private Integer blink;
    private String state;
    private Boolean deleted;

    public EEGData(){}

    public EEGData(Integer id, Integer userId, Date time, Integer theta, Integer lowAlpha, Integer highAlpha, Integer lowBeta, Integer highBeta, Integer lowGamma, Integer midGamma, Integer attention, Integer meditation, Integer blink, String state, Boolean deleted) {
        this.id = id;
        this.userId = userId;
        this.time = time;
        this.theta = theta;
        this.lowAlpha = lowAlpha;
        this.highAlpha = highAlpha;
        this.lowBeta = lowBeta;
        this.highBeta = highBeta;
        this.lowGamma = lowGamma;
        this.midGamma = midGamma;
        this.attention = attention;
        this.meditation = meditation;
        this.blink = blink;
        this.state = state;
        this.deleted = deleted;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Integer getTheta() {
        return theta;
    }

    public void setTheta(Integer theta) {
        this.theta = theta;
    }

    public Integer getLowAlpha() {
        return lowAlpha;
    }

    public void setLowAlpha(Integer lowAlpha) {
        this.lowAlpha = lowAlpha;
    }

    public Integer getHighAlpha() {
        return highAlpha;
    }

    public void setHighAlpha(Integer highAlpha) {
        this.highAlpha = highAlpha;
    }

    public Integer getLowBeta() {
        return lowBeta;
    }

    public void setLowBeta(Integer lowBeta) {
        this.lowBeta = lowBeta;
    }

    public Integer getHighBeta() {
        return highBeta;
    }

    public void setHighBeta(Integer highBeta) {
        this.highBeta = highBeta;
    }

    public Integer getLowGamma() {
        return lowGamma;
    }

    public void setLowGamma(Integer lowGamma) {
        this.lowGamma = lowGamma;
    }

    public Integer getMidGamma() {
        return midGamma;
    }

    public void setMidGamma(Integer midGamma) {
        this.midGamma = midGamma;
    }

    public Integer getAttention() {
        return attention;
    }

    public void setAttention(Integer attention) {
        this.attention = attention;
    }

    public Integer getMeditation() {
        return meditation;
    }

    public void setMeditation(Integer meditation) {
        this.meditation = meditation;
    }

    public Integer getBlink() {
        return blink;
    }

    public void setBlink(Integer blink) {
        this.blink = blink;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public HashMap<String, String> classify(ArrayList<UserState> userStates) {
        ArrayList<String> attributeNames = new ArrayList<>(
                Arrays.asList("theta", "lowAlpha", "highAlpha", "lowBeta", "highBeta",
                        "lowGamma", "midGamma", "attention", "meditation", "blink", "state")
        );
        ArrayList<Integer> attributeValues = new ArrayList<>(
                Arrays.asList(this.getTheta(), this.getLowAlpha(), this.getHighAlpha(), this.getLowBeta(),
                        this.getHighBeta(), this.getLowGamma(), this.getMidGamma(), this.getAttention(),
                        this.getMeditation(), this.getBlink())
        );

        ArrayList<String> classValues = new ArrayList<>(userStates
                .stream()
                .map(UserState::getState)
                .collect(Collectors.toList()));

        // Not enough class values in the database for this userId
        if (classValues.size() < 2) {
            HashMap<String, String> classes = new HashMap<>();
            for (ModelGenerator.METHODS method : ModelGenerator.METHODS.values()) {
                classes.put(method.name(), "Classifier needs states!");
            }
            return classes;
        }

        HashMap<String, String> classes = new HashMap<>();
        try {
            Path filterPath = Paths.get(System.getenv("MODELS_PATH"), this.getUserId().toString(), "FILTER.bin");
            final ModelClassifier classifier = new ModelClassifier(attributeNames, classValues, filterPath.toString());
            Instances classInstances = classifier.createInstance(attributeValues);

            for (ModelGenerator.METHODS method : ModelGenerator.METHODS.values()) {

                Path modelPath = Paths.get(System.getenv("MODELS_PATH"), this.getUserId().toString(), method.name() + ".bin");
                if (!modelPath.toFile().exists()) {
                    classes.put(method.name(), "Classifier being generated!");
                    continue;
                }

                String className = classifier.classify(classInstances, modelPath.toString());

                log.info("The method {} classified the instance as {} for userId {}", method.name(), className, userId.toString());
                classes.put(method.name(), className);
            }
        } catch(Exception e) {
            for (ModelGenerator.METHODS method : ModelGenerator.METHODS.values()) {
                classes.put(method.name(), "Classifier being generated!");
            }
        }
        return classes;
    }

}
