package com.sap.fontus.sql.tainter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryParameters {

    private final List<ParameterType> types;
    private final HashMap<Integer, TaintAssignment> indices;
    private boolean indicesCalculated = false;
    QueryParameters() {
        this.types = new ArrayList<>();
        this.indices = new HashMap<>();
    }
    void addParameter(ParameterType type) {
        System.out.printf("Adding JDBC param of type: %s%n", type);
        this.types.add(type);
        //Thread.dumpStack();
    }

    private void computeIndices() {
        int currentIdx = 1;
        int oldIdx = 1;
        for(ParameterType type : this.types) {
            switch (type) {
                case ASSIGNMENT:
                    this.indices.put(oldIdx, new TaintAssignment(oldIdx, currentIdx, currentIdx+1, type));
                    currentIdx += 2;
                    break;
                case WHERE:
                    this.indices.put(oldIdx, new TaintAssignment(oldIdx, currentIdx, type));
                    currentIdx++;
                    break;
                case SUBSELECT_WHERE:
                    //TODO: This needs additional handling if there are several parameters in the nested query.
                    //Example select
                    this.indices.put(oldIdx, new TaintAssignment(oldIdx, currentIdx, type));
                    currentIdx++;
                    break;
                case ASSIGNMENT_SUBSELECT:
                case QUERY_SUBSELECT:
                    //TODO: This needs additional handling if there are several parameters in the nested query.
                    this.indices.put(oldIdx, new TaintAssignment(oldIdx, currentIdx, currentIdx+1, type));
                    currentIdx += 2;
                    break;
                default:
                    throw new IllegalStateException(String.format("Unknown parameter type: '%s'", type));

            }

            oldIdx++;
        }
    }

    public int getParameterCount() {
        return this.types.size();
    }

    public TaintAssignment computeAssignment(int idx) {
        if(!this.indicesCalculated) {
            this.computeIndices();
            this.indicesCalculated = true;
        }
        return this.indices.get(idx);
    }
}
