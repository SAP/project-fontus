package com.sap.fontus.gdpr.database;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Performs GDPR tainting related operations on a database",
        name = "GDPR Tainting Helper",
        mixinStandardHelpOptions = true,
        version = "0.0.1"
)
public class Application implements Callable<Void> {

    @CommandLine.Option(
            names = {"-m", "--mode"},
            required = true,
            paramLabel = "Mode of Operation",
            description = "Mode of operation. Valid values:  ${COMPLETION-CANDIDATES}",
            defaultValue = "expiry"
    )
    private Mode mode;

    @CommandLine.Option(
            names = {"-d", "--host"},
            required = true,
            paramLabel = "Database host",
            description = "Hostname of Database Server with port",
            defaultValue = "127.0.0.1:3306"
    )
    private String host;

    @CommandLine.Option(
            names = {"-u", "--username"},
            required = true,
            paramLabel = "Database user",
            description = "User to login into the database server",
            defaultValue = "openolat"
    )
    private String username;

    @CommandLine.Option(
            names = {"-p", "--password"},
            required = true,
            paramLabel = "Database Password",
            description = "Password of the database user",
            defaultValue = "olat"
    )
    private String password;

    @CommandLine.Option(
            names = {"-c", "--catalog"},
            required = true,
            paramLabel = "Database catalog",
            description = "What catalog is the data stored in?",
            defaultValue = "olat"
    )
    private String catalog;


    public static void main(String[] args) {
        new CommandLine(new Application())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }

    @Override
    public Void call() throws Exception {

        switch (this.mode) {
            case EXPIRY:
                Processor expiredProcessor = new Processor(this.host, this.username, this.password, this.catalog, new ExpiredTaintColumnGatherer());
                expiredProcessor.run();
                break;
            case STATISTICS:
                StatisticsGatherer gatherer = new StatisticsGatherer();
                Processor statisticsProcessor = new Processor(this.host, this.username, this.password, this.catalog, gatherer);
                statisticsProcessor.run();
                gatherer.printStatistics();
                break;
            case SUBJECT_ACCESS_REQUEST:
                Processor accessRequestProcessor = new Processor(this.host, this.username, this.password, this.catalog, new SubjectAccessRequestGatherer("360448"));
                accessRequestProcessor.run();
                break;
            default:
                System.out.printf("Mode %s is invalid!%n", this.mode);
        }
        return null;
    }
}
