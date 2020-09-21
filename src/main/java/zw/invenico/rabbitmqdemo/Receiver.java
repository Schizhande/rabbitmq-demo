package zw.invenico.rabbitmqdemo;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Receiver {

    /**
     * basicNack(long deliveryTag, boolean multiple, boolean requeue)
     * deliveryTag: The id of each message within mq,
     * multiple: Whether to batch (true: all messages less than deliveryTag will be rejected once);
     * requeue: Whether to rejoin the team
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "myQueue6"),
                    exchange = @Exchange(value = "myExchange1"),
                    key = "routingKey4"
            ))
    public void process7(Message message, Channel channel) throws Exception {
        // Simulate consumer code exceptions. In this case, the number of retries must be set in the catch block (or globally in the configuration file) to prevent dead loops.
        // Retry in catch block can use redis self-increment as counter
        try {
            //  int i = 1 / 1;
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("----> message received");
        } catch (Exception e) {
            log.info("myQueue6:" + new String(message.getBody()));
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            // Return ack with this line of code after reaching the number of retries
            // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}