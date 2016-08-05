package com.matching.exception;

/**
 * Created by root on 3/9/16.
 */
public class InvalidOrderError extends Exception {
    public InvalidOrderError()
    {
        super();
    }
    public InvalidOrderError(String message)
    {
        super(message);
    }
}