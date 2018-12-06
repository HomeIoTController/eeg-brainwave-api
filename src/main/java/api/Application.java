package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Application.class, args);

        KafkaConsumerThread kafkaConsumerThread = new KafkaConsumerThread();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(kafkaConsumerThread);
        kafkaConsumerThread.start();
    }
}

