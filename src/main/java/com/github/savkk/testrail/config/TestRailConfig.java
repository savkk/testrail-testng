package com.github.savkk.testrail.config;

import org.aeonbits.owner.Config;
import org.testng.xml.XmlSuite;

import java.util.List;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:testrail.properties"})
public interface TestRailConfig extends Config {

    @DefaultValue("false")
    @Key("testrail.enabled")
    boolean testrailEnabled();

    @Key("tests.package")
    String testsPackage();

    @DefaultValue("NONE")
    @Key("parallel.mode")
    XmlSuite.ParallelMode parallelMode();

    @DefaultValue("1")
    @Key("parallel.thread.count")
    Integer threadCount();

    @DefaultValue("1")
    @Key("parallel.data_provider_thread.count")
    Integer dataProviderThreadCount();

    @Key("testrail.url")
    String url();

    @Key("testrail.user")
    String user();

    @Key("testrail.password")
    String password();

    @Key("testrail.assignedto_id")
    Integer assignedToId();

    @Key("testrail.run_id")
    Integer runId();

    @Key("testIds")
    @Separator(",")
    List<Integer> testIds();
}
