package com.sap.fontus.sql.tainter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.sanitizer.SqlLexerToken;
import com.sap.fontus.utils.Pair;
import com.sap.fontus.utils.stats.Statistics;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

import java.util.List;
import java.util.Objects;

public enum QueryCache {
    INSTANCE;
    private int hits = 0;
    private int misses = 0;
    private final Cache<String, Pair<String, QueryParameters>> queryCache;
    private final boolean collectStatistics;

    QueryCache() {
        this.collectStatistics = Configuration.getConfiguration().collectStats();
        this.queryCache = Caffeine.newBuilder().build();
    }

    public Pair<String, QueryParameters> parseQuery(String query) {
        if (this.collectStatistics) {
            Statistics.INSTANCE.incrementTotalQueries();
        }
        return this.queryCache.get(query, (q) -> {

            StatementTainter tainter = new StatementTainter();
            Statements stmts = null;
            try {
                stmts = CCJSqlParserUtil.parseStatements(query);
                stmts.accept(tainter);
            } catch (JSQLParserException jsqlParserException) {
                jsqlParserException.printStackTrace();
            }
            if (this.collectStatistics) {
                Statistics.INSTANCE.incrementRewrittenQueries();
                // This uses a different SQL parser, probably not ideal
                List<SqlLexerToken> tokens = SqlLexerToken.getLexerTokens(query);
                Statistics.INSTANCE.incrementTotalQueryLength(tokens.size());
                tokens = SqlLexerToken.getLexerTokens(Objects.requireNonNull(stmts).toString());
                Statistics.INSTANCE.incrementRewrittenQueryLength(tokens.size());
            }

            return new Pair<>(Objects.requireNonNull(stmts).toString().trim(), tainter.getParameters());
        });
    }

}
