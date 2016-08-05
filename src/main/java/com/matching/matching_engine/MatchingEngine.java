package com.matching.matching_engine;

import com.google.gson.JsonObject;
import com.matching.exception.ArgumentError;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by root on 3/2/16.
 */
public class MatchingEngine {
    //OrderBookManager orderBookManager;
    public OrderBook askOrderBook, bidOrderBook;
    String code;
    List<Message> queueDebug;

    Channel channel1;
    String queueName1;

    Channel channel2;
    String queueName2;

    public MatchingEngine(String code) throws Exception {
        //orderBookManager = new orderBookManager(String code);
        askOrderBook = new OrderBook(code, "ask");  //0 -- ask
        bidOrderBook = new OrderBook(code, "bid");  //1 -- bid
        this.code = code;
        queueDebug = new ArrayList<Message>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel1 = connection.createChannel();
        queueName1 = "peatio.orderbook.slave";
        channel1.queueDeclare(queueName1, true, false, false, null);

        channel2 = connection.createChannel();
        queueName2 = "peatio.orderbook.slave";
        channel2.queueDeclare(queueName2, true, false, false, null);

    }

    public void submit(JsonObject JsonMsg) throws Exception{
        Order order;
        JsonObject orderDetail = JsonMsg.getAsJsonObject("order");

        //System.out.println(orderDetail.get("ord_type").getAsString());
        if(orderDetail.get("ord_type").getAsString().equals("limit"))
            order = new LimitOrder(orderDetail);
        else
            order = new MarketOrder(orderDetail);

        if(orderDetail.get("type").getAsString().equals("bid")) {
            match(order, askOrderBook);
            addOrCancel(order, bidOrderBook);
        }
        else {
            match(order, bidOrderBook);
            addOrCancel(order, askOrderBook);
        }
    }

    void addOrCancel(Order order , OrderBook book){
        if(!order.isFilled()) {
            if(order instanceof LimitOrder)
                try {
                    book.add(order);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            else
                publishCancel(order, "fill or kill market order");
        }
    }

    void publishCancel(Order od, String reason){
        System.out.println(code + " cancel order ##" + od.id + "- reason: #" + reason);

        TreeMap<String, Integer> tr = new TreeMap<String, Integer>();
        tr.put("cancel", od.id);
        Message msg = new Message("order_processor", tr, false);

        queueDebug.add(msg);



    }

    void match(Order od, OrderBook ob) throws Exception {
        System.out.println(od.volume <= 0.0);
        if(od.isFilled())
            return;

        Order topOrder = ob.getTop();
        if(topOrder == null)
            return;

        System.out.println("match topOrder: price " + topOrder.price + " volume " + topOrder.volume);

        double[] result = od.tradeWith(topOrder, ob);
        if(result != null){

            try{
                ob.fillTop(result);
                od.fill(result);
            }
            catch(Exception e){
                e.printStackTrace();
            }

            publish(od, topOrder, result);
            match(od, ob);
        }
    }

    void publish(Order od, Order odCounter, double[] trade){
        Order ask, bid;
        if(od.type.equals("bid")) {
            bid = od;
            ask = odCounter;
        }
        else{
            bid = odCounter;
            ask = od;
        }

        DecimalFormat df = new DecimalFormat("#.00");

        double price = Double.parseDouble(df.format(trade[0]));
        double volume = Double.parseDouble(df.format(trade[1]));
        double funds = trade[2];

        System.out.println(code + " new trade - ask: " + ask.id + " bid: " + bid.id + " price: " + price + " volume: " + volume + " funds: " + funds);

        // queue

    }

    public void cancel(JsonObject JsonMsg) throws Exception {
        Order order;
        JsonObject orderDetail = JsonMsg.getAsJsonObject("order");

        if(orderDetail.get("ord_type").getAsString().equals("limit"))
            order = new LimitOrder(orderDetail);
        else
            order = new MarketOrder(orderDetail);

        if(orderDetail.get("type").getAsString().equals("bid"))
            cancelOrder(order, bidOrderBook);
        else
            cancelOrder(order, askOrderBook);
    }

    void cancelOrder(Order order, OrderBook orderbook) throws Exception {
        try {
            Order o = orderbook.remove(order);
            if(o == null){
                System.out.println("Cannot find order##" + order.id + "to cancel, skip.");
            }
            else {
                publishCancel(o, "cancelled by user");
            }
        }
        catch(ArgumentError e){
            e.printStackTrace();
        }
    }


}
