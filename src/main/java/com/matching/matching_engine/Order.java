package com.matching.matching_engine;

import com.google.gson.JsonObject;
import com.matching.exception.ExceedSumLimit;
import com.matching.exception.NotEnoughVolume;

/**
 * Created by root on 3/7/16.
 */
public abstract class Order {
    public int id;
    public int timestamp;
    public String type;
    public double volume;
    public double price;
    public double locked;
    public String market;

    Order(JsonObject order){
        this.id = order.get("id").getAsInt();
        this.market = order.get("market").getAsString();
        this.price = order.get("price").getAsDouble();
        this.timestamp = order.get("timestamp").getAsInt();
        this.type = order.get("type").getAsString();
        this.volume = order.get("volume").getAsDouble();
    }

    abstract boolean isFilled();
    abstract double[] tradeWith(Order o, OrderBook ob);
    abstract void fill(double[] trade) throws NotEnoughVolume, ExceedSumLimit;
    abstract String attributes();

    double volumeLimit(double price){  //just for market order
        return "ask".equals(type) ? locked : locked/price;
    }



}
