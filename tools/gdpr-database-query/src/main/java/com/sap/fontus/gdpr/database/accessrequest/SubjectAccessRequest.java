package com.sap.fontus.gdpr.database.accessrequest;

import com.sap.fontus.gdpr.database.Application;
import com.sap.fontus.gdpr.database.Processor;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "sar", description = "Subject Access Request")
public class SubjectAccessRequest implements Callable<Void> {
    SubjectAccessRequest() {

    }
    @CommandLine.ParentCommand
    private Application parent;

    @CommandLine.Option(
            names = {"-i", "--id"},
            required = true,
            paramLabel = "User Id",
            description = "User ID for subject access request",
            defaultValue = "360448"
    )
    private String id;

    @Override
    public Void call() throws java.sql.SQLException {
        Processor processor = new Processor(this.parent.getHost(), this.parent.getUsername(), this.parent.getPassword(), this.parent.getCatalog(), new SubjectAccessRequestGatherer(this.id));
        processor.run();
        return null;
    }
}
