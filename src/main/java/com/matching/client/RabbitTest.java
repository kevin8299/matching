package com.matching.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by root on 3/11/16.
 */
public class RabbitTest {
    static Channel channel;
    static String queueName;

    public static void main(String[] args) throws Exception {

        System.out.println("test");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        queueName = "peatio.orderbook.slave";
        channel.queueDeclare(queueName, true, false, false, null);


    }


}
