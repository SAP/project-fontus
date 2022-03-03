package com.sap.fontus.gdpr.database.defaultValues;

import com.sap.fontus.gdpr.database.Application;
import com.sap.fontus.gdpr.database.Processor;
import com.sap.fontus.gdpr.database.accessrequest.SubjectAccessRequestGatherer;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "defaults", description = "Finds default values")
public class DefaultValues  implements Callable<Void> {

    @CommandLine.Option(
            names = {"-i", "--id"},
            required = true,
            paramLabel = "Default User Id",
            description = "User ID if unitialized",
            defaultValue = "FONTUS_CHANGE_ME"
    )
    private String id;

    @Override
    public Void call() throws Exception {
        Processor processor = new Processor(this.parent.getHost(), this.parent.getUsername(), this.parent.getPassword(), this.parent.getCatalog(), new DefaultValueGatherer(this.id));
        processor.run();
        return null;
    }

    @CommandLine.ParentCommand
    private Application parent;

}
