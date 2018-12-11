package kafka;

public interface IKafkaConstants {
    String KAFKA_BROKERS = System.getenv("KAFKA_SERVER") + ":" + System.getenv("KAFKA_PORT");
    String CLIENT_ID="eeg-api";
    String TOPIC_NAME=System.getenv("KAFKA_TOPIC");
    String GROUP_ID_CONFIG="eeg";
    Integer MAX_NO_MESSAGE_FOUND_COUNT=100;
    String OFFSET_RESET_EARLIER="earliest";
    Integer MAX_POLL_RECORDS=1;
}
