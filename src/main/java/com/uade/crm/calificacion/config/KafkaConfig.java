package com.uade.crm.calificacion.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Los tres tópicos del ecosistema (docs/08-contrato-tres-topicos.md del orquestador): calificacion escucha
 * {@code crm.comandos}, publica sus hechos en {@code crm.eventos} y manda a {@code crm.dlt} el comando que agotó sus
 * reintentos.
 */
@Configuration
public class KafkaConfig implements DisposableBean {

    @Value("${crm.kafka.topicos.comandos:crm.comandos}")
    private String topicoComandos;

    @Value("${crm.kafka.topicos.eventos:crm.eventos}")
    private String topicoEventos;

    @Value("${crm.kafka.topicos.dlt:crm.dlt}")
    private String topicoDlt;

    private DefaultKafkaProducerFactory<String, String> productorFallidos;

    @Bean
    public NewTopic comandosTopic() {
        return TopicBuilder.name(topicoComandos).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic eventosTopic() {
        return TopicBuilder.name(topicoEventos).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic dltTopic() {
        return TopicBuilder.name(topicoDlt).partitions(1).replicas(1).build();
    }

    /**
     * Reintenta el comando que falla (3 intentos, 1 segundo entre cada uno) y después lo manda a {@code crm.dlt}.
     *
     * <p>El productor de fallidos copia el mensaje original como texto, tal como se recibió (con su sobre): no puede
     * usar el {@code JsonSerializer} del publicador de eventos, que lo volvería a codificar como un string JSON.
     * Toma la configuración del productor de Boot (broker, SASL) con sus propios serializers y se cierra en
     * {@link #destroy()}. Partición -1: decide Kafka (la del mensaje original no tiene por qué existir en la DLT).
     */
    @Bean
    public CommonErrorHandler errorHandler(ProducerFactory<?, ?> productores) {
        productorFallidos = new DefaultKafkaProducerFactory<>(productores.getConfigurationProperties(),
                new StringSerializer(), new StringSerializer());
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                new KafkaTemplate<>(productorFallidos), (mensaje, falla) -> new TopicPartition(topicoDlt, -1));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            CommonErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Override
    public void destroy() {
        if (productorFallidos != null) {
            productorFallidos.destroy();
        }
    }
}
