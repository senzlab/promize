package com.score.cbook.util;

import com.score.cbook.pojo.SenzMsg;
import com.score.senzc.pojos.Senz;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    public static String toJson(SenzMsg senzMsg) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("Uid", senzMsg.getUid());
        jsonParam.put("Msg", senzMsg.getMsg());

        return jsonParam.toString().replaceAll("\\\\","");
    }

    public static Senz toSenz(String jsonStr) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonStr);
        return SenzParser.parse(jsonObj.getString("Msg"));
    }
}
