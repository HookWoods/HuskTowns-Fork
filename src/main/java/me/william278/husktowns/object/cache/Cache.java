package me.william278.husktowns.object.cache;

import java.time.Instant;

public class Cache {

    private CacheStatus status;
    private final String name;
    private final long initializationTime;
    private int itemsLoaded;

    // A cache object that contains some metadata about the cache
    public Cache(String name) {
        this.name = name;
        status = CacheStatus.UNINITIALIZED;
        initializationTime = Instant.now().getEpochSecond();
        itemsLoaded = 0;
    }

    public void clearItemsLoaded() {
        itemsLoaded = 0;
    }

    public void incrementItemsLoaded() {
        itemsLoaded++;
    }

    public void decrementItemsLoaded() {
        itemsLoaded--;
    }

    public int getItemsLoaded() {
        return itemsLoaded;
    }

    public boolean hasLoaded() {
        return status == CacheStatus.LOADED;
    }

    public void setStatus(CacheStatus status) {
        this.status = status;
    }

    public CacheStatus getStatus() {
        return status;
    }

    public long getTimeSinceInitialization() {
        return Instant.now().getEpochSecond() - initializationTime;
    }

    public String getName() {
        return name;
    }

    public String getIllegalAccessMessage() {
        return "Exception attempting to access unloaded " + getName() + " cache (current state: " + getStatus().toString() + ")";
    }

    /**
     * Identifies the current status of a cache
     */
    public enum CacheStatus {
        UNINITIALIZED,
        UPDATING,
        LOADED,
        ERROR
    }

    /**
     * Signals that a cache has been accessed when it has not yet loaded
     */
    public static class CacheNotLoadedException extends IllegalStateException {

        public CacheNotLoadedException(String message) {
            super(message);
        }

    }
}
