package api;

import kafka.AdminClientCreator;
import kafka.ConsumerCreator;
import kafka.IKafkaConstants;
import kafka.ProducerCreator;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreatePartitionsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
    private final AdminClient adminClient = AdminClientCreator.createAdminClient();

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
            log.info("Pulling data!!!");

            ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofSeconds(2));
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

                log.info("Record Key " + record.key());
                log.info("Record value " + record.value());
                log.info("Record partition " + record.partition());
                log.info("Record offset " + record.offset());

                EEGData eegData = gson.fromJson(record.value(), EEGData.class);
                eegDataRepository.save(eegData);

                // Increase partition size depending on the number of registered users
                // UserId grows as 1,2,3,4...
                Integer partitionCount = consumer.partitionsFor(IKafkaConstants.TOPIC_NAME).size();
                if (eegData.getUserId() >= partitionCount) {
                    final NewPartitions newPartitions = NewPartitions.increaseTo(eegData.getUserId() + 5);
                    final Map<String, NewPartitions> request = Collections.singletonMap(IKafkaConstants.TOPIC_NAME, newPartitions);
                    CreatePartitionsResult partitionsResult = adminClient.createPartitions(request);
                    try {
                        partitionsResult.all().get(1000, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        // log.error("Failed to create partitions", e.getCause());
                    }
                }

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
            log.info("Record sent with key " + record.key() + " to partition " + metadata.partition()
                    + " with offset " + metadata.offset());
        }
        catch (ExecutionException | InterruptedException e) {
            log.error("Error in sending record! Message: " + e.getMessage());
        }
    }

}
