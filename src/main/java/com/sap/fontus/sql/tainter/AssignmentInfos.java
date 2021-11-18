package com.sap.fontus.sql.tainter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sap.fontus.Constants.TAINT_PREFIX;


public class AssignmentInfos {

    private LinkedHashMap<AssignmentVariable, AssignmentValue> assignmentInfos;
    private List<String> temporaryAssignVariables;
    private List<AssignmentValue> temporaryAssignValues;
    private LinkedHashMap<Integer, Integer> indices;

    public AssignmentInfos() {
        assignmentInfos = new LinkedHashMap<>();
        temporaryAssignVariables = new ArrayList<>();
        temporaryAssignValues = new ArrayList<>();
    }

    public void setAssignmentVariableWithPlaceholder(String variableName) {
        assignmentInfos.put(new AssignmentVariable(variableName), null);
    }

    public AssignmentValue getAssignmentValue(AssignmentVariable assignmentVariable) {
        return assignmentInfos.get(assignmentVariable);
    }

    public Map<AssignmentVariable, AssignmentValue> getAssignmentInfos() {
        return assignmentInfos;
    }

    public LinkedHashMap<String, String> getAssignmentInfosAsString() {
        LinkedHashMap<String, String> temporary = new LinkedHashMap<>();
        assignmentInfos.forEach((k,v) -> temporary.put(k.getName(), v.getValueAsString()));
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
        indices = new LinkedHashMap<>();
        if (temporaryAssignVariables.size() == temporaryAssignValues.size()) {
            for (int i = 0; i < temporaryAssignVariables.size(); i++) {
                if (!temporaryAssignVariables.get(i).contains(TAINT_PREFIX)) {
                    if (temporaryAssignValues.get(i).getValueAsString().equalsIgnoreCase("?")) {
                        indices.put(countOriginalColumns, i+1);
                    }
                    countOriginalColumns++;
                }
                assignmentInfos.put(new AssignmentVariable(temporaryAssignVariables.get(i)), temporaryAssignValues.get(i));
            }
        }
        return indices;
    }
}
