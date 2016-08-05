package com.matching.matching_engine;

import com.google.gson.JsonObject;
import com.matching.exception.ExceedSumLimit;
import com.matching.exception.NotEnoughVolume;

/**
 * Created by root on 3/7/16.
 */
public class MarketOrder extends Order {

    MarketOrder(JsonObject order){
        super(order);
    }

    boolean isFilled(){
        return volume <= 0 || locked <= 0;
    }

    double[] tradeWith(Order od, OrderBook ob){
        double[] result = null;
        double tradePrice;
        double tradeVolume;
        double tradeFunds;

        if(od instanceof LimitOrder) {
            System.out.println("this order's type " + this.type + " price " + this.price + " Coming order's type " + od.type + " price " + od.price);
            tradePrice = od.price;
            tradeVolume = Math.min(Math.min(volume, od.volume), volumeLimit(tradePrice));
            tradeFunds = tradePrice * tradeVolume;
            result = new double[]{tradePrice, tradeVolume, tradeFunds};
        }
        else{
            double price = ob.bestLimitPrice();
            if(price > 0){
                tradePrice = price;
                tradeVolume = Math.min(Math.min(Math.min(volume, od.volume), volumeLimit(tradePrice)), od.volumeLimit(tradePrice));
                tradeFunds = tradePrice * tradeVolume;
                result = new double[]{tradePrice, tradeVolume, tradeFunds};
            }
        }
        return result;
    }

    void fill(double[] trade) throws NotEnoughVolume, ExceedSumLimit {
        if(trade[1] > volume)
            throw new NotEnoughVolume();

        volume -= trade[1];

        double funds = type.equals("ask") ? trade[1] : trade[2];
        if(funds > locked)
            throw new ExceedSumLimit();

        locked -= funds;
    }

    String attributes() {
        return "id:" + id + ",timestamp:" + timestamp +
                ",type:" + type + ",locked:" + locked +
                ",volume:" + volume + ",market:" + market +
                ",ord_type:" + "market";
    }

}
