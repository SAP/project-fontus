package com.sap.fontus.gdpr.database.expired;

import com.sap.fontus.gdpr.database.Application;
import com.sap.fontus.gdpr.database.Processor;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "expired", description = "Collect Expired Data")
public class ExpiredData implements Callable<Void> {
    @CommandLine.ParentCommand
    private Application parent;

    @Override
    public Void call() throws java.sql.SQLException {
        Processor processor = new Processor(this.parent.getHost(), this.parent.getUsername(), this.parent.getPassword(), this.parent.getCatalog(), new ExpiredTaintColumnGatherer());
        processor.run();
        return null;
    }
}
