package com.iti.tripplanner;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class NetworkAdapter {

    private static RequestQueue mRequestQueue;

    static RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(context);
        return mRequestQueue;
    }

}
