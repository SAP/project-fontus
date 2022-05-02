package com.sap.fontus.sql.tainter;

import java.util.*;

public class QueryParameters {

    private final List<ParameterType> types;
    private final HashMap<Integer, TaintAssignment> indices;
    private boolean indicesCalculated = false;

    private final Deque<StatementType> stateStack;

    QueryParameters() {
        this.types = new ArrayList<>();
        this.indices = new HashMap<>();
        this.stateStack = new ArrayDeque<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        QueryParameters that = (QueryParameters) o;
        return this.indicesCalculated == that.indicesCalculated && this.types.equals(that.types) && this.indices.equals(that.indices) && this.stateStack.equals(that.stateStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.types, this.indices, this.indicesCalculated, this.stateStack);
    }

    public void begin(StatementType type) {
        //System.out.printf("Pushing %s onto stack%n", type);
        this.stateStack.push(type);
    }

    public void end(StatementType type) {
        //System.out.printf("Popping %s from stack%n", type);
        StatementType top = this.stateStack.pop();
        if(top != type) {
            throw new IllegalStateException(String.format("Trying to pop '%s' but currently '%s' on top of stack", type, top));
        }

    }

    void addParameter(ParameterType type) {
        //System.out.printf("Adding JDBC param of type: %s%n", type);
        this.types.add(type);
        //Thread.dumpStack();
    }

    private void computeIndices() {
        if(!this.stateStack.isEmpty()) {
            throw new IllegalStateException("Trying to compute indices despite state stack not being empty");
        }
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
