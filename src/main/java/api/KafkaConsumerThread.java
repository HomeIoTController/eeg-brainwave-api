package api;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

@Component
public class KafkaConsumerThread extends Thread {

    @Autowired
    private EEGDataRepository eegDataRepository;

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerThread.class);

    private final String TOPIC = System.getenv("KAFKA_TOPIC");
    private final String BOOTSTRAP_SERVERS = System.getenv("KAFKA_SERVER") + ":" + System.getenv("KAFKA_PORT");

    @PostConstruct
    public void init() {
        start();
    }

    private Consumer<Long, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<Long, String> consumer =
                new KafkaConsumer<>(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }

    void runConsumer() {
        log.info("Started Kafka Consumer");

        final Consumer<Long, String> consumer = createConsumer();
        final int giveUp = 100;
        int noRecordsCount = 0;
        Gson gson = new Gson();

        while (true) {
            final ConsumerRecords<Long, String> consumerRecords =
                    consumer.poll(Duration.ofMinutes(1));

            //log.info("Pulling {} topic data", TOPIC);
            //log.info("Found {} records!", consumerRecords.count());
            //log.info("Number of tries  {} (will give up on {})", noRecordsCount, giveUp);

            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }
            for (ConsumerRecord<Long, String> consumerRecord : consumerRecords) {
                //log.info("Consumer Record: ({}, {}, {}, {})\n",
                //        consumerRecord.key(), consumerRecord.value(),
                //        consumerRecord.partition(), consumerRecord.offset());

                EEGData eegData = gson.fromJson(consumerRecord.value(), EEGData.class);
                log.info("eegData: {} ", eegData);
                eegDataRepository.save(eegData);
            }
            consumer.commitAsync();
        }
        consumer.close();
        log.info("Consumer was closed!");
    }

    public void run() {
        runConsumer();
    }
}
