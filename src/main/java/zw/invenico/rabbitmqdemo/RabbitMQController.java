package zw.invenico.rabbitmqdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RabbitMQController {

    // Rabbit Template is used to send messages, or AmqpTemplate is recommended.
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping(value = "/helloRabbit5")
    public String sendMQ5() {
        String msg = "rabbitmq Generator Send Failure and Consumer Failure Handling Scheme";
        try {
            // Re-connect 10 times using retryTemplate for disconnection caused by network reasons
            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(10));
            rabbitTemplate.setRetryTemplate(retryTemplate);

            // Verify whether it is sent to the switch, store the cache if it is not, resend it with another thread, and resend it directly with rabbitTemplate in it will throw a cyclic dependency error.
            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (!ack) {
                    // Memory Cache Operation
                    log.info(msg + "fail in send:" + cause);
                } else {
                    log.info("-----> Message acknowledged");
                }
            });

            // Confirm whether to send to the queue, store the cache if not, then check the exchange, routingKey configuration, and then resend
            rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
                // Memory Cache Operation
                log.info(new String(message.getBody()) + "No queue was found. exchange by" + exchange + ",routingKey by" + routingKey);
            });

            rabbitTemplate.convertAndSend("myExchange1", "routingKey4", msg);
        } catch (AmqpException e) {
            // Memory Cache Operation
            log.info(msg + "fail in send:The reason was not repeated 10 times.");
        }

        return "success";
    }
}