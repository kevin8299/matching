package com.matching.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.matching.matching_engine.MatchingEngine;
import com.matching.matching_engine.Order;
import com.matching.matching_engine.PriceLevel;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by root on 3/3/16.
 */
public class Client {
    private final static String QUEUE_NAME = "test";
    private final static boolean durable = true;

    static org.slf4j.Logger logger;
    static MatchingEngine engine;

    Client(String engineCode) throws Exception{

        //logger = Logger.logger;
        engine = new MatchingEngine(engineCode); //0: self queue mode   1: use RabbitMQ

    }

    public static void main(String[] args) throws Exception {
        Client c = new Client("Tea");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                //System.out.println(consumerTag + "  " + envelope.toString() + "  " + properties.toString());
                System.out.println(" [x] Received '" + message + "'");

                /*

                {"action":"submit",
                "order":{"id":15,"market":"555cny","type":"bid","ord_type":"limit",
                "volume":"1.0","price":"1.0","locked":"1.0","timestamp":1457148770},
                "locale":"zh-CN"}

                */

                JsonParser parser = new JsonParser();
                JsonObject JsonMsg = (JsonObject) parser.parse(message);
                String action = JsonMsg.get("action").getAsString();

                if("submit".equals(action))
                    try {
                        engine.submit(JsonMsg);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                else if("cancel".equals(action))
                    try {
                        engine.cancel(JsonMsg);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                else
                    System.out.println("Error -- Unknown action:   " + action);
                    //logger.error("Unknown action:   " + action);

                System.out.println("=================================================");
                //ask--limit order
                System.out.println("ask -- limit order : ");
                TreeMap<Double, PriceLevel> askLimit = engine.askOrderBook.limitOrders;
                Set<Double> askKeys = askLimit.keySet();
                for(Double k: askKeys){
                    PriceLevel pl = askLimit.get(k);
                    System.out.println(pl.price);
                    for(int i = 0; i < pl.orders.size(); i++){
                        Order o = pl.orders.get(i);
                        System.out.println("Order #id: " + o.id + " #type: " + o.type + " #market: " + o.market + " #price: " + o.price + " #volume: " + o.volume + " #locked: " + o.locked);
                    }
                }



                //bid--limit order
                System.out.println("bid -- limit order : ");
                TreeMap<Double, PriceLevel> bidLimit = engine.bidOrderBook.limitOrders;
                Set<Double> bidKeys = bidLimit.keySet();
                for(Double k: bidKeys){
                    PriceLevel pl = bidLimit.get(k);
                    System.out.println(pl.price);
                    for(int i = 0; i < pl.orders.size(); i++){
                        Order o = pl.orders.get(i);
                        System.out.println("Order #id: " + o.id + " #type: " + o.type + " #market: " + o.market + " #price: " + o.price + " #volume: " + o.volume + " #locked: " + o.locked);
                    }
                }
                System.out.println("=================================================");
            }
        };

        channel.basicConsume(QUEUE_NAME, true, consumer);

    }


}
