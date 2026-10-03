package com.uade.crm.calificacion.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Value("${crm.kafka.topicos.preformulario-completado:preformulario-completado}")
    private String topicoPreformularioCompletado;

    @Value("${crm.kafka.topicos.lead-calificado:lead-calificado}")
    private String topicoLeadCalificado;

    @Value("${crm.kafka.topicos.preformulario-dlt:preformulario-completado-dlt}")
    private String topicoPreformularioDlt;

    @Bean
    public NewTopic preformularioCompletadoTopic() {
        return TopicBuilder.name(topicoPreformularioCompletado)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic leadCalificadoTopic() {
        return TopicBuilder.name(topicoLeadCalificado)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic preformularioDltTopic() {
        return TopicBuilder.name(topicoPreformularioDlt)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations);
        // Reintentos: 3 intentos con 1 segundo de intervalo antes de enviar a DLT
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            CommonErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
