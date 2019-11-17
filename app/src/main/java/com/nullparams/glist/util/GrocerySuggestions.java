package com.nullparams.glist.util;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GrocerySuggestions {

    public static String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("groceries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static ArrayList<String> getGrocerySuggestions(Context context) {

        ArrayList<String> mGroceries = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset(context));

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject arrayObject = jsonArray.getJSONObject(i);
                String item = arrayObject.getString("name");
                mGroceries.add(item);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mGroceries;
    }
}
