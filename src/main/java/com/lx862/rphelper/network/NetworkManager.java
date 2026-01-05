package com.lx862.rphelper.network;

import com.lx862.rphelper.config.Config;
import com.lx862.rphelper.data.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.function.Consumer;

public class NetworkManager {
    public static void setRequestTimeout(HttpURLConnection httpURLConnection) {
        httpURLConnection.setConnectTimeout(Config.getRequestTimeoutSec() * 1000);
        httpURLConnection.setReadTimeout(Config.getRequestTimeoutSec() * 1000);
    }

    public static boolean downloadPart(File outputFile, URL url, Consumer<Integer> callback, long byteOffset, long chunkLength) {
        int thisDownloaded = 0;

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            HttpURLConnection nhr = (HttpURLConnection) url.openConnection();
            nhr.setReadTimeout(30 * 1000);
            nhr.setConnectTimeout(30 * 1000);

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
                Log.LOGGER.error("", e);
                return false;
            } finally {
                nhr.disconnect();
            }

            return true;
        } catch (Exception e) {
            Log.LOGGER.error("", e);
            return false;
        }
    }
}
