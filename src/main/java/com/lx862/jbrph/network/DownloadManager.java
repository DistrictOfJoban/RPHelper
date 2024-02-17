package com.lx862.jbrph.network;

import com.lx862.jbrph.data.Log;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
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

        boolean allPartSuccessful = true;
        for(int i = 0; i < totalParts; i++) {
            if(!allPartSuccessful) break;
            boolean success = false;
            int retryCount = 0;

            while(!success && retryCount < 3) {
                File partFile = new File(outputLocation + ".part" + i);

                int byteOffset = supportsHttpRange ? i * PER_CHUNK : -1;
                long chunkLength = supportsHttpRange ? PER_CHUNK : totalPackSize == -1 ? Long.MAX_VALUE : totalPackSize;

                AtomicInteger thisPartDownloaded = new AtomicInteger();
                boolean partDownloaded = downloadPart(partFile, url, (byteDownloaded) -> {
                    thisPartDownloaded.addAndGet(byteDownloaded);
                    double dlProgress = totalPackSize == -1 ? -1 : (double)(totalDownloaded.get() + thisPartDownloaded.get()) / totalPackSize;
                    callback.accept(dlProgress);
                }, byteOffset, chunkLength);

                if(partDownloaded) {
                    success = true;
                    allPartSuccessful = true;
                    totalDownloaded.addAndGet(thisPartDownloaded.get());
                    continue;
                }

                retryCount++;
                allPartSuccessful = false;

                try {
                    Log.warn("Failed to download part " + i + ", retrying in " + FAILED_TIMEOUT + " seconds...");
                    Thread.sleep(FAILED_TIMEOUT * 1000);
                } catch (Exception ignored) {
                }
            }
        }

        // Merge files
        if(allPartSuccessful) {
            Files.deleteIfExists(outputLocation.toPath());
            mergePartFile(outputLocation, totalParts);
        }

        cleanupPartFile(outputLocation, totalParts);
        finishedCallback.accept(allPartSuccessful ? null : "Download interrupted!");
        httpUrlConnection.disconnect();
    }

    public static boolean downloadPart(File outputFile, URL url, Consumer<Integer> callback, long byteOffset, long chunkLength) {
        int thisDownloaded = 0;

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            HttpURLConnection nhr = (HttpURLConnection) url.openConnection();
            NetworkManager.setRequestTimeout(nhr);

            // Http Range Support
            boolean supportRange = byteOffset != -1;
            if(supportRange) nhr.setRequestProperty("Range", "bytes=" + byteOffset + "-" + (byteOffset + chunkLength));

            try(BufferedInputStream ns = new BufferedInputStream(nhr.getInputStream())) {
                byte[] dataBuffer = new byte[1024];
                while(true) {
                    int nextReadLength = (int)Math.min(chunkLength - thisDownloaded, 1024);
                    int byteRead = ns.read(dataBuffer, 0, nextReadLength);
                    if(byteRead == -1 || thisDownloaded >= chunkLength) break;

                    bos.write(dataBuffer, 0, byteRead);
                    thisDownloaded += byteRead;
                    callback.accept(byteRead);
                }
            } catch (SocketException | SocketTimeoutException e) {
                Log.error("Timed out while downloading!");
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                nhr.disconnect();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
