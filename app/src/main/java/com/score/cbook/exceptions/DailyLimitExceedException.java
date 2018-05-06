package com.score.cbook.exceptions;

public class DailyLimitExceedException extends Exception {

    @Override
    public String toString() {
        return "daily promize limit exceeded";
    }

}
