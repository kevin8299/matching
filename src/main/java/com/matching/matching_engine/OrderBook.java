package com.matching.matching_engine;

import com.matching.exception.ArgumentError;
import com.matching.exception.ExceedSumLimit;
import com.matching.exception.InvalidOrderError;
import com.matching.exception.NotEnoughVolume;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by root on 3/7/16.
 */
public class OrderBook {
    String market;
    String side;
    public TreeMap<Double, PriceLevel> limitOrders;
    public TreeMap<Integer, Order>  marketOrders;
    Channel channel;
    String queueName;

    OrderBook(String m, String s) throws Exception {
        market = m;
        side = s;
        limitOrders = new TreeMap<Double, PriceLevel>();
        marketOrders = new TreeMap<Integer, Order>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        queueName = "peatio.orderbook.slave";
        channel.queueDeclare(queueName, true, false, false, null);

        String params = "action:" + "new" + ",market:" + market + ",side:" + side;
        broadcast(params);
    }

    void add(Order od) throws Exception {
        if(od.volume > 0){
            if(od instanceof LimitOrder){
                if(limitOrders.containsKey(od.price)) {
                    PriceLevel pl = limitOrders.get(od.price);
                    pl.add(od);
                }
                else{
                    PriceLevel pl = new PriceLevel(od.price);
                    pl.add(od);
                    limitOrders.put(od.price, pl);
                }

                System.out.println("limitOrders listed below: ");
                Set<Double> keys = limitOrders.keySet();
                for(Double k: keys){
                    System.out.println("Price is " + k);
                    PriceLevel pl = limitOrders.get(k);
                    for(int i = 0; i < pl.orders.size(); i++)
                        System.out.println(pl.orders.get(i).id);
                }

            }
            else if(od instanceof MarketOrder)
                marketOrders.put(od.id, od);
                else
                    throw new ArgumentError("Unknown Order Type");

            String params = "action:" + "add" + ",order:" + "{" + od.attributes() + "}";
            broadcast(params);
        }
        else
            throw new InvalidOrderError();
    }

    double bestLimitPrice(){
        double res = -1;
        Order o;
        if("ask".equals(side))  // ask
            o = askLimitTop();
        else
            o = bidLimitTop();

        if(o != null)
            res = o.price;
        return res;
    }


    Order getLimitTop(){
        return "ask".equals(side) ? askLimitTop() : bidLimitTop();
    }

    Order getTop(){
        return marketOrders.size() == 0 ? getLimitTop() : marketOrders.firstEntry().getValue();
    }

    Order askLimitTop(){
        if(limitOrders.isEmpty())
            return null;
        else {
            PriceLevel pl = limitOrders.get(limitOrders.firstKey());
            return pl.top();
        }
    }

    Order bidLimitTop(){
        if(limitOrders.isEmpty())
            return null;
        else {
            PriceLevel pl = limitOrders.get(limitOrders.lastKey());
            return pl.top();
        }
    }

    void fillTop(double[] trade) throws Exception {
        Order top = getTop();
        if(top == null)
            System.out.println("No top order in empty book.");
        else{
            try{
                top.fill(trade);  // subtract volumn
            }
            catch(NotEnoughVolume e1){
                e1.printStackTrace();
            }
            catch(ExceedSumLimit e2){
                e2.printStackTrace();
            }

            System.out.println("fillTop -- top volume: " + top.volume);
            if(top.isFilled()) {
                try {
                    remove(top);
                }
                catch(ArgumentError e){
                    e.printStackTrace();
                }
            }
            else
                broadcast("action:" + "update" + ",order:" + "{" + top.attributes() + "}");
        }
    }

    Order remove(Order od) throws Exception {
        Order o = null;
        if(od instanceof LimitOrder)
            o = removeLimitOrder(od);
        else if(od instanceof MarketOrder)
            o = removeMarketOrder(od);
        else
            throw new ArgumentError("No Such Order to Remove !");
        return o;
    }

    Order removeLimitOrder(Order od) throws Exception {
        Order o = null;
        PriceLevel pl = limitOrders.get(od.price);
        if(pl != null){
            o = pl.find(od.id);
            if(o != null) {
                pl.remove(o);
                if(pl.isEmpty())
                    limitOrders.remove(od.price);
                broadcast("action:" + "remove" + "order:" + "{" + od.attributes() + "}");
            }
        }
        return o;
    }

    Order removeMarketOrder(Order od) throws Exception {
        Order o = marketOrders.get(od.id);
        if(o != null) {
            marketOrders.remove(o.id);
            broadcast("action:" + "remove" + "order:" + "{" + od.attributes() + "}");
        }
        return o;
    }

    void broadcast(String data) throws Exception {
        System.out.println("OrderBook broadcast: " + data);
        channel.basicPublish("", queueName, null, data.getBytes("UTF-8"));
    }



}
