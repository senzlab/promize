package com.score.cbook.interfaces;

import com.score.senzc.pojos.Senz;

import java.util.List;

public interface IContractExecutorListener {
    void onFinishTask(List<Senz> senzes);
}
