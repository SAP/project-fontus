package com.sap.fontus.taintaware.unified;

import java.util.regex.MatchResult;

public class IASMatchResultImpl implements IASMatchResult {
    private final IASString string;
    private final MatchResult matchResult;

    IASMatchResultImpl(IASString string, MatchResult matchResult) {
        this.string = string;
        this.matchResult = matchResult;
    }

    @Override
    public int end() {
        return this.matchResult.end();
    }

    @Override
    public int end(int group) {
        return this.matchResult.end(group);
    }

    @Override
    public IASString group() {
        return this.string.substring(this.start(), this.end());
    }

    @Override
    public IASString group(int group) {
        int start = this.start(group);
        int end = this.end(group);
        if(start == -1 || end == -1) {
            return null;
        }
        return this.string.substring(this.start(group), this.end(group));
    }

    @Override
    public int groupCount() {
        return this.matchResult.groupCount();
    }

    @Override
    public int start() {
        return this.matchResult.start();
    }

    @Override
    public int start(int group) {
        return this.matchResult.start(group);
    }

    @Override
    public MatchResult toMatchResult() {
        return this.matchResult;
    }
}
