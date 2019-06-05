package com.github.savkk.testrail;

import org.aeonbits.owner.Config;

@Config.Sources("classpath:testrail.properties")
public interface TestRailConfig extends Config {

    @Key("tests.package")
    String testsPackage();

    @Key("testrail.url")
    String url();

    @Key("testrail.user")
    String user();

    @Key("testrail.password")
    String password();

    @Key("testrail.assignedto_id")
    Integer assignedToId();
}
