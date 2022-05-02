package com.sap.fontus.sql.tainter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.utils.Pair;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

import java.util.WeakHashMap;

public enum QueryCache {
    INSTANCE;
    private int hits = 0;
    private int misses = 0;
    private final Cache<String, Pair<String, QueryParameters>> queryCache;

    QueryCache() {
        this.queryCache = Caffeine.newBuilder().build();
    }

    public Pair<String, QueryParameters> parseQuery(String query) {
        Pair<String, QueryParameters> parsed = this.queryCache.getIfPresent(query);
        if (parsed != null) {
            this.hits++;
            if (this.hits + this.misses % 1000 == 0) {
                System.out.printf("QueryCache: %d/%d h/m%n", this.hits, this.misses);
            }
            return parsed;
        }
        StatementTainter tainter = new StatementTainter();
        Statements stmts = null;
        try {
            stmts = CCJSqlParserUtil.parseStatements(query);
            stmts.accept(tainter);
        } catch (JSQLParserException jsqlParserException) {
            jsqlParserException.printStackTrace();
        }
        Pair<String, QueryParameters> pair = new Pair<>(stmts.toString().trim(), tainter.getParameters());
        this.queryCache.put(query, pair);
        this.misses++;
        if (this.hits + this.misses % 1000 == 0) {
            System.out.printf("QueryCache: %d/%d h/m%n", this.hits, this.misses);
        }
        return pair;
    }
}
