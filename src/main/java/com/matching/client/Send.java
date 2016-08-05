package com.matching.client;

/**
 * Created by root on 3/2/16.
 */

import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {

    private final static String QUEUE_NAME = "test";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.150.120");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                /*
                {"action":"submit",
                "order":{"id":15,"market":"555cny","type":"bid","ord_type":"limit",
                "volume":"1.0","price":"1.0","locked":"1.0","timestamp":1457148770},
                "locale":"zh-CN"}
                */

        JsonObject jo = new JsonObject();
        jo.addProperty("id", 15);
        jo.addProperty("market", "tea");
        jo.addProperty("type", "bid");
        jo.addProperty("ord_type", "limit");
        jo.addProperty("volume", "1");
        jo.addProperty("price", "15");
        jo.addProperty("locked", "1.0");
        jo.addProperty("timestamp", 1457148770);



        //LimitOrder lo = new LimitOrder(jo);
        JsonObject req = new JsonObject();
        req.addProperty("action", "submit");
        req.add("order", jo);

        //Request req = new Request("submit", lo);
        System.out.println(req.toString());

        String message = req.toString();
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}
