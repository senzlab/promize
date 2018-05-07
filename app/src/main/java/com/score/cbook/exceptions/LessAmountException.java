package com.score.cbook.exceptions;

public class LessAmountException extends Exception {

    @Override
    public String toString() {
        return "invalid amount";
    }

}
