package com.score.cbook.interfaces;

import com.score.senzc.pojos.Senz;

import java.util.List;

public interface ISendingComHandler {
    void send(Senz senz);

    void sendInOrder(List<Senz> senzList);

}
