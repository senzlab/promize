package com.score.cbook.util;

import com.score.cbook.pojo.SenzMsg;
import com.score.senzc.pojos.Senz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtil {
    public static String toJson(SenzMsg senzMsg) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("Uid", senzMsg.getUid());
        jsonParam.put("Msg", senzMsg.getMsg());

        return jsonParam.toString().replaceAll("\\\\", "");
    }

    public static Senz toSenz(String jsonStr) throws JSONException {
        if (jsonStr != null && !jsonStr.isEmpty()) {
            JSONObject jsonObj = new JSONObject(jsonStr);
            return SenzParser.parse(jsonObj.getString("Msg"));
        }

        throw new JSONException("Null jsonstr");
    }

    public static ArrayList<Senz> toSenzes(String jsonStr) {
        ArrayList<Senz> senzes = new ArrayList<>();
        if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
                JSONArray jsonArr = new JSONArray(jsonStr);
                for (int i = 0; i < jsonArr.length(); i++) {
                    try {
                        senzes.add(SenzParser.parse(jsonArr.getJSONObject(i).getString("Msg")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return senzes;
    }
}
