package com.lx862.jbrph.network;

import com.lx862.jbrph.RPHelperClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
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
    public static void download(URL url, File outputLocation, Consumer<Double> callback, Consumer<Boolean> finishedCallback) throws IOException {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setRequestProperty("Range", "bytes=0-1");
        httpUrlConnection.connect();

        long totalPackSize = Long.parseLong(httpUrlConnection.getHeaderField("Content-Range").split("/")[1]);
        AtomicInteger totalDownloaded = new AtomicInteger();

        int totalParts = (int)Math.ceil(totalPackSize / (double)PER_CHUNK);

        boolean fullSuccess = true;

        for(int i = 0; i < totalParts; i++) {
            if(!fullSuccess) break;
            boolean success = false;
            int retryCount = 0;

            while(!success) {
                if(retryCount >= 3) {
                    RPHelperClient.LOGGER.error("[JBRPH] Cannot download after 3 attempts, giving up.");
                    fullSuccess = false;
                    break;
                }

                File partFile = new File(outputLocation + ".part" + i);
                AtomicInteger thisPartDownloaded = new AtomicInteger();
                boolean partDownloaded = downloadPart(partFile, url, (byteDownloaded) -> {
                    thisPartDownloaded.addAndGet(byteDownloaded);
                    callback.accept((double)(totalDownloaded.get() + thisPartDownloaded.get()) / totalPackSize);
                }, (long) i * PER_CHUNK);

                if(partDownloaded) {
                    success = true;
                    totalDownloaded.addAndGet(thisPartDownloaded.get());
                    continue;
                }

                retryCount++;

                try {
                    RPHelperClient.LOGGER.warn("[JBRPH] Failed to download part " + i + ", retrying in " + FAILED_TIMEOUT + " seconds...");
                    Thread.sleep(FAILED_TIMEOUT * 1000);
                } catch (Exception ignored) {
                }
            }
        }

        // Merge files
        if(fullSuccess) {
            mergePartFile(outputLocation, totalParts);
        }

        cleanupPartFile(outputLocation, totalParts);
        finishedCallback.accept(fullSuccess);
        httpUrlConnection.disconnect();
    }

    public static boolean downloadPart(File outputFile, URL url, Consumer<Integer> callback, long byteOffset) {
        int thisDownloaded = 0;

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            HttpURLConnection nhr = (HttpURLConnection) url.openConnection();
            nhr.setConnectTimeout(10 * 1000);
            nhr.setReadTimeout(10 * 1000);
            nhr.setRequestProperty("Range", "bytes=" + byteOffset + "-" + (byteOffset + PER_CHUNK));

            try(BufferedInputStream ns = new BufferedInputStream(nhr.getInputStream())) {
                byte[] dataBuffer = new byte[1024];
                while(true) {
                    int br = ns.read(dataBuffer, 0, Math.min(PER_CHUNK - thisDownloaded, 1024));
                    if(br == -1) break;
                    if(thisDownloaded >= PER_CHUNK) {
                        break;
                    }

                    bos.write(dataBuffer, 0, br);

                    thisDownloaded += br;
                    callback.accept(br);
                }
            } catch (SocketException | SocketTimeoutException e) {
                RPHelperClient.LOGGER.error("[JBRPH] Timed out while downloading!");
                nhr.disconnect();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                nhr.disconnect();
                return false;
            }

            nhr.disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
