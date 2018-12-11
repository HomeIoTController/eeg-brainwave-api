package kafka;

import api.EEGData;
import api.EEGDataRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import com.google.gson.Gson;


@Component
public class KafkaThread extends Thread {

    @Autowired
    private EEGDataRepository eegDataRepository;

    private static final Logger log = LoggerFactory.getLogger(KafkaThread.class);

    private final Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
    private final Producer<Long, String> producer = ProducerCreator.createProducer();

    private final Gson gson = new Gson();

    @PostConstruct
    public void init() {
        start();
    }

    public void run() {
        runConsumer();
    }

    private void runConsumer() {
        int noMessageFound = 0;
        while (true) {
            ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMinutes(1));
            // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
            if (consumerRecords.count() == 0) {
                noMessageFound++;
                if (noMessageFound > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT)
                    // If no message found count is reached to threshold exit loop.
                    break;
                else
                    continue;
            }
            for (ConsumerRecord<Long, String> record : consumerRecords) {

                System.out.println("Record Key " + record.key());
                System.out.println("Record value " + record.value());
                System.out.println("Record partition " + record.partition());
                System.out.println("Record offset " + record.offset());

                EEGData eegData = gson.fromJson(record.value(), EEGData.class);

                log.info("eegData: {} ", eegData);
                eegDataRepository.save(eegData);

                runProducer(new ProducerRecord<>(IKafkaConstants.TOPIC_NAME, eegData.getUserId(), eegData.getUserId().longValue(),
                       gson.toJson(eegData.classify())));
            }
            // commits the offset of record to broker.
            consumer.commitAsync();
        }
        consumer.close();
        producer.close();
    }
    private void runProducer(ProducerRecord<Long, String> record) {
        try {
            RecordMetadata metadata = producer.send(record).get();
            System.out.println("Record sent with key " + record.key() + " to partition " + metadata.partition()
                    + " with offset " + metadata.offset());
        }
        catch (ExecutionException | InterruptedException e) {
            System.out.println("Error in sending record");
            System.out.println(e);
        }
    }

}
