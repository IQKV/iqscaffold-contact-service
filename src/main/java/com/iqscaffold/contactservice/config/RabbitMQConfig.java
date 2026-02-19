package com.iqscaffold.contactservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Contact Service.
 * Configures exchanges and message converters for contact lifecycle events.
 */
@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "iqscaffold.events";
  public static final String DLX_EXCHANGE = "iqscaffold.dlx";
  public static final String DLQ = "iqscaffold.dlq";
  public static final String CONTACT_CREATED_ROUTING_KEY = "contact.created";
  public static final String CONTACT_UPDATED_ROUTING_KEY = "contact.updated";
  public static final String CONTACT_DELETED_ROUTING_KEY = "contact.deleted";

  /**
   * Creates the CRM events topic exchange.
   * This exchange is shared across all CRM services.
   *
   * @return TopicExchange for CRM events
   */
  @Bean
  public TopicExchange crmEventsExchange() {
    return new TopicExchange(EXCHANGE_NAME, true, false);
  }

  /**
   * Dead Letter Exchange for failed messages
   */
  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(DLX_EXCHANGE, true, false);
  }

  /**
   * Dead Letter Queue for failed messages
   */
  @Bean
  public Queue deadLetterQueue() {
    return new Queue(DLQ, true);
  }

  /**
   * Bind dead letter queue to DLX with all routing keys
   */
  @Bean
  public Binding deadLetterBinding() {
    return BindingBuilder
        .bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with("#");
  }

  /**
   * Creates a JSON message converter for RabbitMQ messages.
   *
   * @return Jackson2JsonMessageConverter
   */
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /**
   * Creates a RabbitAdmin for auto-declaring exchanges.
   *
   * @param connectionFactory RabbitMQ connection factory
   * @return Configured RabbitAdmin
   */
  @Bean
  public RabbitAdmin rabbitAdmin(final ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  /**
   * Creates a RabbitTemplate with JSON message converter.
   *
   * @param connectionFactory RabbitMQ connection factory
   * @return Configured RabbitTemplate
   */
  @Bean
  public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
  }

  /**
   * Creates a RabbitListener container factory with JSON message converter.
   *
   * @param connectionFactory RabbitMQ connection factory
   * @return Configured SimpleRabbitListenerContainerFactory
   */
  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      final ConnectionFactory connectionFactory) {
    final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jsonMessageConverter());
    return factory;
  }


  /**
   * Tenant events queue with dead letter routing
   * Receives tenant lifecycle events from User Service
   */
  @Bean
  public Queue tenantEventsQueue() {
    return new Queue("iqscaffold.contact.tenant.events", true, false, false,
        java.util.Map.of(
            "x-dead-letter-exchange", DLX_EXCHANGE,
            "x-message-ttl", 86400000 // 24 hours
        ));
  }

  /**
   * Bind tenant events queue to exchange with tenant.# routing key
   * Receives all tenant lifecycle events (created, updated, deleted)
   */
  @Bean
  public Binding tenantEventsBinding() {
    return BindingBuilder
        .bind(tenantEventsQueue())
        .to(crmEventsExchange())
        .with("tenant.#");
  }

}
