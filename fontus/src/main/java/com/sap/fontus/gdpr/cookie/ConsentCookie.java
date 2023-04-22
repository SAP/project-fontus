package com.sap.fontus.gdpr.cookie;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import com.sap.fontus.sql.tainter.QueryParameters;
import com.sap.fontus.utils.Pair;


import java.time.Instant;
import java.util.*;

public class ConsentCookie {
    public List<Purpose> getPurposes() {
        return this.purposes;
    }

    public Instant getCreated() {
        return Instant.ofEpochSecond(this.created);
    }

    public void setPurposes(List<Purpose> purposes) {
        this.purposes = purposes;
    }
    public void setCreated(long created) {
        this.created = created;
    }
    private static final Genson genson = new GensonBuilder().useFields(true, VisibilityFilter.PRIVATE).create();
    private List<Purpose> purposes;
    private long created;

    public ConsentCookie() {
        this.purposes = new ArrayList<>(1);
        this.created = Instant.now().getEpochSecond();

    }
    private static final Cache<String,ConsentCookie> cookieCache = Caffeine.newBuilder().build();;

    @Override
    public int hashCode() {
        return Objects.hash(this.purposes, this.created);
    }
    @Override
    public String toString() {
        return String.format("ConsentCookie{purposes=%s, created=%d}", this.purposes, this.created);
    }

    public static ConsentCookie parse(String encoded) {
        return cookieCache.get(encoded, (ignored) -> {
            byte[] bs = Base64.getDecoder().decode(encoded);
            String value = new String(bs);
            return genson.deserialize(value, ConsentCookie.class);
        });
    }

    private static final String cookieName = "GDPRCONSENT";

    public static boolean isConsentCookie(String name) { return name.equals(cookieName); }

    public static String getConsentCookieName() { return cookieName; }
}
