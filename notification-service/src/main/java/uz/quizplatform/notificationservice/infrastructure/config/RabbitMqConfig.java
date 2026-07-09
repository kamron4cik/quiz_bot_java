package uz.quizplatform.notificationservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology configuration for the notification service.
 *
 * Exchanges:
 *   quiz.events    (topic)  — quiz session lifecycle events
 *   payment.events (topic)  — payment approval/rejection
 *   notification   (direct) — broadcast, single messages
 *
 * Queues (all durable, with Dead Letter Exchange):
 *   quiz.session.finished.queue
 *   quiz.session.timeout.queue
 *   payment.status.changed.queue
 *   notification.broadcast.queue
 *   notification.single.queue
 *   notification.dlq (dead letter queue)
 */
@Configuration
public class RabbitMqConfig {

    // ── Exchange names ─────────────────────────────────────────────────────────
    public static final String QUIZ_EVENTS_EXCHANGE = "quiz.events";
    public static final String PAYMENT_EVENTS_EXCHANGE = "payment.events";
    public static final String NOTIFICATION_EXCHANGE = "notification";
    public static final String DLX_EXCHANGE = "quiz.dlx";

    // ── Routing keys ──────────────────────────────────────────────────────────
    public static final String SESSION_FINISHED_KEY = "quiz.session.finished";
    public static final String SESSION_TIMEOUT_KEY = "quiz.session.timeout";
    public static final String PAYMENT_STATUS_KEY = "payment.status.changed";
    public static final String BROADCAST_KEY = "notification.broadcast";
    public static final String SINGLE_MSG_KEY = "notification.single";

    // ── Queue names ────────────────────────────────────────────────────────────
    public static final String SESSION_FINISHED_QUEUE = "quiz.session.finished.queue";
    public static final String SESSION_TIMEOUT_QUEUE = "quiz.session.timeout.queue";
    public static final String PAYMENT_STATUS_QUEUE = "payment.status.changed.queue";
    public static final String BROADCAST_QUEUE = "notification.broadcast.queue";
    public static final String SINGLE_MSG_QUEUE = "notification.single.queue";
    public static final String DLQ = "notification.dlq";

    // ── Exchanges ──────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange quizEventsExchange() {
        return ExchangeBuilder.topicExchange(QUIZ_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange paymentEventsExchange() {
        return ExchangeBuilder.topicExchange(PAYMENT_EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATION_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE).durable(true).build();
    }

    // ── Queues (with DLX configured) ──────────────────────────────────────────

    @Bean
    public Queue sessionFinishedQueue() {
        return QueueBuilder.durable(SESSION_FINISHED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue sessionTimeoutQueue() {
        return QueueBuilder.durable(SESSION_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue paymentStatusQueue() {
        return QueueBuilder.durable(PAYMENT_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue broadcastQueue() {
        return QueueBuilder.durable(BROADCAST_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue singleMessageQueue() {
        return QueueBuilder.durable(SINGLE_MSG_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    // ── Bindings ───────────────────────────────────────────────────────────────

    @Bean
    public Binding sessionFinishedBinding() {
        return BindingBuilder.bind(sessionFinishedQueue()).to(quizEventsExchange()).with(SESSION_FINISHED_KEY);
    }

    @Bean
    public Binding sessionTimeoutBinding() {
        return BindingBuilder.bind(sessionTimeoutQueue()).to(quizEventsExchange()).with(SESSION_TIMEOUT_KEY);
    }

    @Bean
    public Binding paymentStatusBinding() {
        return BindingBuilder.bind(paymentStatusQueue()).to(paymentEventsExchange()).with(PAYMENT_STATUS_KEY);
    }

    @Bean
    public Binding broadcastBinding() {
        return BindingBuilder.bind(broadcastQueue()).to(notificationExchange()).with(BROADCAST_KEY);
    }

    @Bean
    public Binding singleMessageBinding() {
        return BindingBuilder.bind(singleMessageQueue()).to(notificationExchange()).with(SINGLE_MSG_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(dlxExchange()).with(DLQ);
    }

    // ── Message converter ─────────────────────────────────────────────────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
