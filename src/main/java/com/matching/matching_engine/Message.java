package com.matching.matching_engine;

import java.util.TreeMap;

/**
 * Created by root on 3/9/16.
 */
public class Message {
    String target;
    TreeMap<String, Integer> msg;
    boolean persistent;

    Message(String target, TreeMap<String, Integer> msg, boolean persistent){
        this.target = target;
        this.msg = msg;
        this.persistent = persistent;
    }

}
