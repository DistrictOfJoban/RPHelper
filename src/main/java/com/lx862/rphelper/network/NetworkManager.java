package com.lx862.rphelper.network;

import com.lx862.rphelper.config.Config;

import java.net.HttpURLConnection;

public class NetworkManager {
    public static void setRequestTimeout(HttpURLConnection httpURLConnection) {
        httpURLConnection.setConnectTimeout(Config.getRequestTimeoutSec() * 1000);
        httpURLConnection.setReadTimeout(Config.getRequestTimeoutSec() * 1000);
    }
}
