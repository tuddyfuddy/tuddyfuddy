package com.heejuk.tuddyfuddy.notificationservice.config;

import com.heejuk.tuddyfuddy.notificationservice.exception.NotificationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.message.kafka.host}")
    private String kafkaHost;

    @Value("${spring.message.kafka.port}")
    private String kafkaPort;

    @Value("${spring.message.kafka.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // AckMode를 RECORD로 변경 (메시지 단위로 처리)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        // 에러 핸들러
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (consumerRecord, exception) -> {
                if (exception instanceof IllegalArgumentException ||
                    exception.getCause() instanceof IllegalArgumentException ||
                    exception instanceof NotificationException ||
                    exception.getCause() instanceof NotificationException) {
                    log.error("Message processing failed, skipping: {}", consumerRecord.value(),
                        exception);
                }
            },
            new FixedBackOff(1000L, 0L)
        );

        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            NotificationException.class
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
