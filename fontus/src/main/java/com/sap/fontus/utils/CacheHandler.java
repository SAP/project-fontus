package com.sap.fontus.utils;

import com.sap.fontus.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum CacheHandler {
    INSTANCE;
    private final Set<Integer> hashes;
    private final File cacheFolder;
    private long misses = 0L;
    private long hits = 0L;

    CacheHandler() {
        this.hashes = ConcurrentHashMap.newKeySet();
        this.cacheFolder = new File("./tmp/agent/cache");
        if(!this.cacheFolder.exists()) {
            this.cacheFolder.mkdirs();
        }
        populate();
    }

    public static CacheHandler get() {
        return INSTANCE;
    }

    private void populate() {
        try {
            for (final File f : Objects.requireNonNull(this.cacheFolder.listFiles())) {

                if (!f.isFile()) {
                    continue;
                }

                String fileName = f.getName();
                // Assumption: all files of the form <hash>.class
                String hash = fileName.substring(0, fileName.indexOf("."));
                this.hashes.add(Integer.valueOf(hash));

            }
        } catch(Exception ex) {
            System.out.printf("Error while populating cache: %s - %s%n", ex.getClass().getName(), ex.getMessage());
        }
    }
    public void put(int hash, byte[] classFileBuffer, String className) throws IOException {
        File cb = new File(String.format("./tmp/agent/cache/%d.%s%s", hash, className.replace("/", "."), Constants.CLASS_FILE_SUFFIX));
        boolean created = cb.createNewFile();
        if(!created) {
            System.out.printf("Tried to put file with hash %d into cache but file exists?%n", hash);
        }
        Files.write(cb.toPath(), classFileBuffer);
        this.hashes.add(hash);
    }

    public byte[] fetchFromCache(int hash, String className) throws IOException {
        if(!this.hashes.contains(hash)) {
            throw new IllegalStateException(String.format("Trying to fetch file with hash %d from cache despite it not exisiting!!!", hash));
        }
        File cb = new File(String.format("./tmp/agent/cache/%d.%s%s", hash, className.replace("/", "."), Constants.CLASS_FILE_SUFFIX));
        if(!cb.isFile()) {
            throw new IllegalStateException(String.format("Trying to fetch file ('%s') which does not exist!!!", cb.getName()));
        }
        return Files.readAllBytes(cb.toPath());
    }

    public boolean isCached(int hash) {
        boolean isCached = this.hashes.contains(hash);
        if(isCached) {
            this.hits++;
        } else {
            this.misses++;
        }
        if((this.hits + this.misses)%1000L == 0L) {
            System.out.printf("Cache stats: %d/%d (h/m) (size: %d)%n", this.hits, this.misses, this.hashes.size());
        }
        return isCached;
    }
}
