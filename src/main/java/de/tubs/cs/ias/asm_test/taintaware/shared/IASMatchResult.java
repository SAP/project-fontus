package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

public interface IASMatchResult {
    int end();
    int end(int group);
    IASStringable group();
    IASStringable group(int group);
    int groupCount();
    int start();
    int start(int group);
}
