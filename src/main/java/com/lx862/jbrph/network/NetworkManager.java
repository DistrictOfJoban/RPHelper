package com.lx862.jbrph.network;

import java.net.HttpURLConnection;

public class NetworkManager {
    public static void setRequestTimeout(HttpURLConnection httpURLConnection) {
        httpURLConnection.setConnectTimeout(10 * 1000);
        httpURLConnection.setReadTimeout(10 * 1000);
    }
}
