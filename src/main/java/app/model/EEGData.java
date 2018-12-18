package app.model;

import app.datamining.ModelClassifier;
import app.datamining.ModelGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;

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
    private String feelingLabel;

    public EEGData(){}

    public EEGData(Integer id, Integer userId, Date time, Integer theta, Integer lowAlpha, Integer highAlpha, Integer lowBeta, Integer highBeta, Integer lowGamma, Integer midGamma, Integer attention, Integer meditation, Integer blink, String feelingLabel) {
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
        this.feelingLabel = feelingLabel;
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

    public String getFeelingLabel() {
        return feelingLabel;
    }

    public void setFeelingLabel(String feelingLabel) {
        this.feelingLabel = feelingLabel;
    }

    public HashMap<String, String> classify() {
        final ModelClassifier classifier = new ModelClassifier();
        Instances classInstances = classifier.createInstance(this.getTheta(), this.getLowAlpha(), this.getHighAlpha(), this.getLowBeta(), this.getHighBeta(), this.getLowGamma(), this.getMidGamma(), this.getAttention(), this.getMeditation(), this.getBlink());

        HashMap<String, String> classes = new HashMap<>();
        for (ModelGenerator.METHODS method : ModelGenerator.METHODS.values()) {

            Path modelPath = Paths.get(System.getenv("MODELS_PATH"), this.getUserId().toString(), method.name() + ".bin");
            if (!modelPath.toFile().exists()) {
                classes.put(method.name(), "Classifier being generated!");
                continue;
            }

            String className = classifier.classify(classInstances, modelPath.toString());

            //log.info("The method {} classified the instance as {}", method.name(), className);
            classes.put(method.name(), className);
        }
        return classes;
    }

}
