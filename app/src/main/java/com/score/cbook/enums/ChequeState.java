package com.score.cbook.enums;

public enum ChequeState {
    NONE(0),
    TRANSFER(1),
    DEPOSIT(2);

    private int state;

    ChequeState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public static ChequeState valueOfState(int state) {
        for (ChequeState s : ChequeState.values()) {
            if (s.state == state) {
                return s;
            }
        }

        return null;
    }
}
