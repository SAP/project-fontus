package com.sap.fontus.gdpr.database.contest;

import com.sap.fontus.gdpr.database.Application;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "contest", description = "contest wrong data")
public class ContestWrongData implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        System.out.printf("%s -> %s for %s%n", this.application, this.dataType, this.id);
        try(ContestAction action = new ContestAction(this.parent.getHost(), this.parent.getUsername(), this.parent.getPassword(), this.parent.getCatalog())) {
            boolean contested = action.canContest(this.application, this.dataType, this.id);
            System.out.printf("Successful? %b%n", contested);
            return null;
        }
    }

    @CommandLine.Option(
            names = {"-a", "--app"},
            required = true,
            paramLabel = "application",
            description = "Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "olat"
    )
    private SupportedApplication application;

    @CommandLine.Option(
            names = {"-t", "--type"},
            required = true,
            paramLabel = "data type",
            description = "Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "firstname"
    )
    private ContestDataType dataType;

    @CommandLine.Option(
            names = {"-i", "--id"},
            required = true,
            paramLabel = "User Id",
            description = "User ID for subject access request",
            defaultValue = "327686"
    )
    private String id;

    @CommandLine.ParentCommand
    private Application parent;

}
