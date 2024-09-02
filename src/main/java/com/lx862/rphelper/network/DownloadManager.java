package com.lx862.rphelper.network;

import com.lx862.rphelper.data.Log;

import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DownloadManager {
    public static final int PER_CHUNK = 1024 * 1024 * 15;
    public static final int FAILED_TIMEOUT = 5;

    private static void mergePartFile(File outputLocation, int parts) {
        try(RandomAccessFile raf = new RandomAccessFile(outputLocation, "rw")) {
            for(int i = 0; i < parts; i++) {
                File partFile = new File(outputLocation + ".part" + i);
                try(FileInputStream fis = new FileInputStream(partFile)) {
                    raf.seek((long) i * PER_CHUNK);
                    raf.write(fis.readAllBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cleanupPartFile(File outputLocation, int parts) {
        for(int i = 0; i < parts; i++) {
            File partFile = new File(outputLocation + ".part" + i);
            partFile.delete();
        }
    }

    public static void download(URL url, File outputLocation, Consumer<Double> callback, Consumer<String> finishedCallback) throws IOException {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setRequestProperty("Range", "bytes=0-1");

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

        boolean supportsHttpRange = httpUrlConnection.getResponseCode() == 206;
        long totalPackSize = supportsHttpRange ? Long.parseLong(httpUrlConnection.getHeaderField("Content-Range").split("/")[1]) : httpUrlConnection.getContentLength();

        int totalParts = !supportsHttpRange ? 1 : (int)Math.ceil(totalPackSize / (double)PER_CHUNK);
        AtomicInteger totalDownloaded = new AtomicInteger();

        boolean successfulSoFar = true;
        for(int i = 0; i < totalParts; i++) {
            if(!successfulSoFar) break;
            int byteOffset = supportsHttpRange ? i * PER_CHUNK : -1;
            long chunkLength = supportsHttpRange ? PER_CHUNK : totalPackSize == -1 ? Long.MAX_VALUE : totalPackSize;

            File partFile = new File(outputLocation + ".part" + i);
            if(partFile.exists() && partFile.length() == chunkLength) {
                Log.info("Found " + partFile.getName() + ", continuing...");
                totalDownloaded.addAndGet(PER_CHUNK);
                continue;
            }

            boolean success = false;
            int retryCount = 0;

            while(!success && retryCount < 3) {
                AtomicInteger thisPartDownloaded = new AtomicInteger();
                boolean partDownloaded = NetworkManager.downloadPart(partFile, url, (byteDownloaded) -> {
                    thisPartDownloaded.addAndGet(byteDownloaded);
                    double dlProgress = totalPackSize == -1 ? -1 : (double)(totalDownloaded.get() + thisPartDownloaded.get()) / totalPackSize;
                    callback.accept(dlProgress);
                }, byteOffset, chunkLength);

                if(partDownloaded) {
                    success = true;
                    successfulSoFar = true;
                    totalDownloaded.addAndGet(thisPartDownloaded.get());
                    continue;
                }

                retryCount++;
                successfulSoFar = false;

                try {
                    Log.warn("Failed to download part " + i + ", retrying in " + FAILED_TIMEOUT + " seconds...");
                    Thread.sleep(FAILED_TIMEOUT * 1000);
                } catch (Exception ignored) {
                }
            }
        }

        httpUrlConnection.disconnect();

        // Merge files
        if(successfulSoFar) {
            if(outputLocation.exists()) outputLocation.delete();
            mergePartFile(outputLocation, totalParts);
            cleanupPartFile(outputLocation, totalParts);
            finishedCallback.accept(null);
        }
    }
}
