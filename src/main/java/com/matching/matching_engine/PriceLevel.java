package com.matching.matching_engine;

import java.util.ArrayList;

/**
 * Created by root on 3/8/16.
 */
public class PriceLevel {
    public double price;
    public ArrayList<Order> orders;


    PriceLevel(double p){
        price = p;
        orders = new ArrayList<Order>();
    }

    Order top(){
        return orders.get(0);
    }

    boolean isEmpty(){
        return orders.isEmpty();
    }

    void add(Order o){
        orders.add(o);
    }

    void remove(Order o){
        orders.remove(o);
    }


    Order find(int id){
        for(Order o: orders)
            if(o.id == id)
                return o;
        return null;
    }


}
