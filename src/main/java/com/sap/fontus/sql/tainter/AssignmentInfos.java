package com.sap.fontus.sql.tainter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sap.fontus.Constants.TAINT_PREFIX;


public class AssignmentInfos {

    private final LinkedHashMap<AssignmentVariable, AssignmentValue> assignmentInfos;
    private List<String> temporaryAssignVariables;
    private List<AssignmentValue> temporaryAssignValues;
    private LinkedHashMap<Integer, Integer> indices;

    public AssignmentInfos() {
        this.assignmentInfos = new LinkedHashMap<>();
        this.temporaryAssignVariables = new ArrayList<>();
        this.temporaryAssignValues = new ArrayList<>();
    }

    public void setAssignmentVariableWithPlaceholder(String variableName) {
        this.assignmentInfos.put(new AssignmentVariable(variableName), null);
    }

    public AssignmentValue getAssignmentValue(AssignmentVariable assignmentVariable) {
        return this.assignmentInfos.get(assignmentVariable);
    }

    public Map<AssignmentVariable, AssignmentValue> getAssignmentInfos() {
        return this.assignmentInfos;
    }

    public LinkedHashMap<String, String> getAssignmentInfosAsString() {
        LinkedHashMap<String, String> temporary = new LinkedHashMap<>();
        this.assignmentInfos.forEach((k, v) -> temporary.put(k.getName(), v.getValueAsString()));
        return temporary;
    }

    public void setTemporaryAssignVariables(List<String> temporaryAssignVariables) {
        this.temporaryAssignVariables = temporaryAssignVariables;
    }

    public void setTemporaryAssignValues(List<AssignmentValue> temporaryAssignValues) {
        this.temporaryAssignValues = temporaryAssignValues;
    }

    public LinkedHashMap<Integer, Integer> getIndices() {
        int countOriginalColumns = 1;
        this.indices = new LinkedHashMap<>();
        if (this.temporaryAssignVariables.size() == this.temporaryAssignValues.size()) {
            for (int i = 0; i < this.temporaryAssignVariables.size(); i++) {
                if (!this.temporaryAssignVariables.get(i).contains(TAINT_PREFIX)) {
                    if (this.temporaryAssignValues.get(i).getValueAsString().equalsIgnoreCase("?")) {
                        this.indices.put(countOriginalColumns, i+1);
                    }
                    countOriginalColumns++;
                }
                this.assignmentInfos.put(new AssignmentVariable(this.temporaryAssignVariables.get(i)), this.temporaryAssignValues.get(i));
            }
        }
        return this.indices;
    }
}
