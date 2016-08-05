package com.matching.matching_engine;

/**
 * Created by root on 3/7/16.
 */
public class OrderBookManager {
    OrderBook askOrder, bidOrder;

    OrderBookManager(String code) throws Exception{
        askOrder = new OrderBook(code, "ask");  //0 -- ask
        bidOrder = new OrderBook(code, "bid");  //1 -- bid

    }






}
