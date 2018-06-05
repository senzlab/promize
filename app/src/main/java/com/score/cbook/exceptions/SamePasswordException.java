package com.score.cbook.exceptions;

/**
 * Exception when throw invalid balance
 *
 * @author erangaeb@gmail.com (eranga bandara)
 */
public class SamePasswordException extends Exception {

    @Override
    public String toString() {
        return "Invalid password";
    }

}
