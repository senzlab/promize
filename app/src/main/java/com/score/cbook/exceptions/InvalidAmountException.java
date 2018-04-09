package com.score.cbook.exceptions;

/**
 * Exception when throw invalid account
 *
 * @author erangaeb@gmail.com (eranga bandara)
 */
public class InvalidAmountException extends Exception {

    @Override
    public String toString() {
        return "invalid amount";
    }

}
