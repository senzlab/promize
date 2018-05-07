package com.score.cbook.exceptions;

public class ExceedAmountException extends Exception {

    @Override
    public String toString() {
        return "invalid amount";
    }

}
