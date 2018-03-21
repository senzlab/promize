package com.score.senz;

import com.score.senzc.pojos.Senz;


interface ISenzService {
    void sendSenz(in Senz senz);
    void sendSenzes(in List<Senz> senzList);
}
