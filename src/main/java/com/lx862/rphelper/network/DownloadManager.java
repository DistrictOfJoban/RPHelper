package com.lx862.rphelper.network;

import com.lx862.rphelper.data.Log;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DownloadManager {
    public static final int FAILED_TIMEOUT = 5;

    public static void download(URL url, File outputLocation, Consumer<Double> callback, Consumer<String> finishedCallback) throws IOException {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();

        try {
            int httpCode = httpUrlConnection.getResponseCode();
            if(httpCode >= 400) {
                Log.error("Received Error Code " + httpCode + " while downloading from " + url + "!");
                httpUrlConnection.disconnect();
                finishedCallback.accept("HTTP Error " + httpCode);
                return;
            }
        } catch (UnknownHostException e) {
            Log.error("Cannot resolve host " + url + "!");
            httpUrlConnection.disconnect();
            finishedCallback.accept("Can't resolve host for URL.");
            return;
        }

        long totalPackSize = httpUrlConnection.getContentLength();
        AtomicInteger totalDownloaded = new AtomicInteger();

        boolean downloadSuccess = true;
        boolean successSoFar = false;

        int retryCount = 0;

        while(!successSoFar && retryCount < 3) {
            AtomicInteger thisPartDownloaded = new AtomicInteger();
            boolean partDownloaded = NetworkManager.downloadPart(outputLocation, url, (byteDownloaded) -> {
                thisPartDownloaded.addAndGet(byteDownloaded);
                double dlProgress = totalPackSize == -1 ? -1 : (double)(thisPartDownloaded.get()) / totalPackSize;
                callback.accept(dlProgress);
            }, -1, totalPackSize);

            if(partDownloaded) {
                successSoFar = true;
                downloadSuccess = true;
                totalDownloaded.addAndGet(thisPartDownloaded.get());
                continue;
            }

            retryCount++;
            downloadSuccess = false;

            try {
                Log.warn("Failed to download, retrying in {} seconds...", FAILED_TIMEOUT);
                Thread.sleep(FAILED_TIMEOUT * 1000);
            } catch (Exception ignored) {
            }
        }

        httpUrlConnection.disconnect();

        // Merge files
        if(downloadSuccess) {
            finishedCallback.accept(null);
        }
    }
}
