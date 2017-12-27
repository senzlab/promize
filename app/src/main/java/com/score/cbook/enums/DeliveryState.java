package com.score.cbook.enums;

public enum DeliveryState {
    NONE(0),
    PENDING(1),
    RECEIVED(2),
    DELIVERED(3);

    private int state;

    DeliveryState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public static DeliveryState valueOfState(int state) {
        for (DeliveryState ds : DeliveryState.values()) {
            if (ds.state == state) {
                return ds;
            }
        }

        return null;
    }
}
