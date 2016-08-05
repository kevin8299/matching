package com.matching.matching_engine;

import com.google.gson.JsonObject;
import com.matching.exception.NotEnoughVolume;

/**
 * Created by root on 3/7/16.
 */
public class LimitOrder extends Order {

    public LimitOrder(JsonObject order){
        super(order);
    }

    boolean isFilled(){
        return volume <= 0.0;
    }

    double[] tradeWith(Order od, OrderBook ob){
        double[] result = null;
        double tradePrice;
        double tradeVolume;
        double tradeFunds;

        if(od instanceof LimitOrder) {
            System.out.println("this order's type " + this.type + " price " + this.price + " Coming order's type " + od.type + " price " + od.price);
            if (isCrossed(od.price)) {
                tradePrice = od.price;
                tradeVolume = Math.min(volume, od.volume);
                tradeFunds = tradePrice * tradeVolume;
                result = new double[]{tradePrice, tradeVolume, tradeFunds};
            }
        }
        else{
            tradeVolume = Math.min(Math.min(volume, od.volume), od.volumeLimit(price));
            tradeFunds  = price*tradeVolume;
            result = new double[]{price, tradeVolume, tradeFunds};
        }

        return result;
    }

    boolean isCrossed(double price){
        boolean result ;
        if("ask".equals(type))
            result = price >= this.price;
        else
            result = price <= this.price;

        return result;
    }

    void fill(double[] trade) throws NotEnoughVolume {
        System.out.println("Traded volume to sbtract : " + trade[1]);
        if(trade[1] > volume)
            throw new NotEnoughVolume();
        else
            volume -= trade[1];
    }

    String attributes(){
        return "id:" + id + ",timestamp:" + timestamp +
                ",type:" + type + ",locked:" + locked +
                ",volume:" + volume + ",market:" + market +
                ",ord_type:" + "limit";


    }

}
