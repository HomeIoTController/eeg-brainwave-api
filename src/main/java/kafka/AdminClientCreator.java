package kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class AdminClientCreator {
    public static AdminClient createAdminClient() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, IKafkaConstants.KAFKA_BROKERS);
        AdminClient adminClient = AdminClient.create(props);

        adminClient.describeCluster();
        try {
            Collection<TopicListing> topicListings = adminClient.listTopics().listings().get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Failed to list topics!");
        }
        return adminClient;
    }
}
