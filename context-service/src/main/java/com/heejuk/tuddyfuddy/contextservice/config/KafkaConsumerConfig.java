//package com.heejuk.tuddyfuddy.contextservice.config;
//
//import java.util.HashMap;
//import java.util.Map;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.listener.ContainerProperties;
//
//@Slf4j
//@EnableKafka
//@Configuration
//public class KafkaConsumerConfig {
//
//    @Value("${spring.message.kafka.host}")
//    private String kafkaHost;
//
//    @Value("${spring.message.kafka.port}")
//    private String kafkaPort;
//
//    @Value("${spring.message.kafka.group-id}")
//    private String groupId;
//
//    @Bean
//    public ConsumerFactory<String, String> consumerFactory() {
//
//        Map<String, Object> properties = new HashMap<>();
//        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
//        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//
//        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
//
//        return new DefaultKafkaConsumerFactory<>(properties);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
//
//        ConcurrentKafkaListenerContainerFactory<String, String> factory =
//            new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        factory.getContainerProperties()
//               .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
//
//        return factory;
//    }
//}
