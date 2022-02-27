package com.sap.fontus.gdpr.database;

import com.sap.fontus.gdpr.database.expired.ExpiredData;
import com.sap.fontus.gdpr.database.statistics.Statistics;
import com.sap.fontus.gdpr.database.accessrequest.SubjectAccessRequest;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Performs GDPR tainting related operations on a database",
        name = "GDPR Tainting Helper",
        mixinStandardHelpOptions = true,
        version = "0.0.1",
        subcommands = {
            SubjectAccessRequest.class,
            ExpiredData.class,
            Statistics.class
        }
)
public class Application implements Callable<Void> {

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

    public String getHost() {
        return this.host;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getCatalog() {
        return this.catalog;
    }

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
        return null;
    }
}
