package com.sap.fontus.gdpr.database.data;

import com.sap.fontus.gdpr.database.Application;
import com.sap.fontus.gdpr.database.Processor;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "stats", description = "Collect Statistics")
public class DataSize implements Callable<Void> {

    @Override
    public Void call() throws java.sql.SQLException {
        DataSizeGatherer gatherer = new DataSizeGatherer();
        Processor statisticsProcessor = new Processor(this.parent.getHost(), this.parent.getUsername(), this.parent.getPassword(), this.parent.getCatalog(),  gatherer);
        statisticsProcessor.run();
        gatherer.printStatistics();
        return null;
    }

    @CommandLine.ParentCommand
    private Application parent;
}
