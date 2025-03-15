package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.data.HashComparisonResult;
import com.lx862.rphelper.data.Log;
import com.lx862.rphelper.data.PackEntry;
import com.lx862.rphelper.network.NetworkManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HashManager {
    /* Holds the remote hash cache */
    private static final HashMap<String, String> hashCache = new HashMap<>();

    public static void addHashCache(PackEntry packEntry, String hash) {
        hashCache.put(packEntry.uniqueId(), hash);
    }

    public static String getCachedRemoteHash(PackEntry packEntry) {
        return hashCache.get(packEntry.uniqueId());
    }

    public static String fetchRemoteHash(URL url) throws IOException {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        NetworkManager.setRequestTimeout(httpUrlConnection);

        try {
            return IOUtils.toString(httpUrlConnection.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (UnknownHostException e) {
            return null;
        } catch (IOException e) {
            Log.LOGGER.error(e);
            return null;
        }
    }

    public static HashComparisonResult compareHash(PackEntry packEntry, File localFile, boolean ignoreRemoteCache) {
        if(packEntry.sha1Url != null) {
            return HashManager.compareRemoteHash(packEntry, localFile, true);
        } else {
            if(packEntry.sha1 == null) {
                return HashComparisonResult.NOT_AVAIL;
            } else {
                return HashManager.compareLocalHash(packEntry, localFile);
            }
        }
    }

    public static String getFileHash(File file) {
        if(!file.exists()) return null;

        try(FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.sha1Hex(fis).trim();
        } catch (Exception e) {
            return null;
        }
    }

    public static HashComparisonResult compareLocalHash(PackEntry packEntry, File localFile) {
        if(packEntry.sha1 == null) return HashComparisonResult.NOT_AVAIL;

        String localSha1 = getFileHash(localFile);
        if(localSha1 == null && localFile.exists()) {
            PackManager.logPackWarn(packEntry, "Failed to obtain local SHA1, hope pack is up to date?");
            return HashComparisonResult.MATCH;
        }

        if(packEntry.sha1.equals(localSha1)) {
            PackManager.logPackInfo(packEntry, "Hash OK: " + localSha1);
        } else {
            PackManager.logPackWarn(packEntry, "Hash Different! Expected: " + packEntry.sha1 + ", Local: " + localSha1);
        }
        return packEntry.sha1.equals(localSha1) ? HashComparisonResult.MATCH : HashComparisonResult.MISMATCH;
    }

    public static HashComparisonResult compareRemoteHash(PackEntry packEntry, File localFile) {
        return compareRemoteHash(packEntry, localFile, false);
    }

    public static HashComparisonResult compareRemoteHash(PackEntry packEntry, File localFile, boolean ignoreCache) {
        if(packEntry.sha1Url == null) return HashComparisonResult.NOT_AVAIL;

        try {
            String cachedRemoteSha1 = getCachedRemoteHash(packEntry);
            String remoteSha1 = ignoreCache || cachedRemoteSha1 == null ? fetchRemoteHash(packEntry.sha1Url) : cachedRemoteSha1;
            String localSha1 = getFileHash(localFile);

            if(localSha1 == null && localFile.exists()) {
                PackManager.logPackWarn(packEntry, "Failed to obtain local SHA1, hope local one is up to date?");
                return HashComparisonResult.MATCH;
            }

            if(remoteSha1 == null) {
                PackManager.logPackWarn(packEntry, "Cannot obtain remote SHA1 from URL " + packEntry.sha1Url);
                return HashComparisonResult.NOT_AVAIL;
            }

            addHashCache(packEntry, remoteSha1);
            if(remoteSha1.equals(localSha1)) {
                PackManager.logPackInfo(packEntry, "Hash OK: " + remoteSha1);
            } else {
                PackManager.logPackWarn(packEntry, "Hash Different! Expected: " + remoteSha1 + ", Local: " + localSha1);
            }
            return remoteSha1.equals(localSha1) ? HashComparisonResult.MATCH : HashComparisonResult.MISMATCH;
        } catch (Exception e) {
            Log.LOGGER.error(e);
        }
        return HashComparisonResult.NOT_AVAIL;
    }
}
