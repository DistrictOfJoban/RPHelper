package com.lx862.rphelper.data.manager;

import com.lx862.rphelper.data.HashComparisonResult;
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
            e.printStackTrace();
            return null;
        }
    }

    public static String getFileHash(File file) {
        if(!file.exists()) return null;

        try {
            return DigestUtils.sha1Hex(new FileInputStream(file)).trim();
        } catch (Exception e) {
            return null;
        }
    }

    public static HashComparisonResult compareRemoteHash(PackEntry packEntry, File localFile) {
        return compareRemoteHash(packEntry, localFile, false);
    }

    public static HashComparisonResult compareRemoteHash(PackEntry packEntry, File localFile, boolean ignoreCache) {
        if(packEntry.hashUrl == null) return HashComparisonResult.NOT_AVAIL;

        try {
            String cachedRemoteSha1 = getCachedRemoteHash(packEntry);
            String remoteSha1 = ignoreCache || cachedRemoteSha1 == null ? fetchRemoteHash(packEntry.hashUrl) : cachedRemoteSha1;
            String localSha1 = getFileHash(localFile);

            if(localSha1 == null && localFile.exists()) {
                PackManager.logPackWarn(packEntry, "Failed to obtain local SHA1, hope local one is up to date?");
                return HashComparisonResult.MATCH;
            }

            if(remoteSha1 == null) {
                PackManager.logPackWarn(packEntry, "Cannot obtain remote SHA1 from URL " + packEntry.hashUrl);
                return HashComparisonResult.NOT_AVAIL;
            }

            addHashCache(packEntry, remoteSha1);
            if(remoteSha1.equals(localSha1)) {
                PackManager.logPackInfo(packEntry, "Hash OK: " + remoteSha1);
            } else {
                PackManager.logPackWarn(packEntry, "Hash Different! Remote: " + remoteSha1 + ", Local: " + localSha1);
            }
            return remoteSha1.equals(localSha1) ? HashComparisonResult.MATCH : HashComparisonResult.MISMATCH;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HashComparisonResult.NOT_AVAIL;
    }
}
