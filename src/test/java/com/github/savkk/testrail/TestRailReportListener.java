package com.github.savkk.testrail;

import com.github.savkk.testrail.config.TestRailConfig;
import io.qameta.allure.TmsLink;
import org.aeonbits.owner.ConfigFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestRailReportListener implements ITestListener {
    private static final TestRailConfig config = ConfigFactory.create(TestRailConfig.class);
    private final TestRailClient testRailClient = new TestRailClient(config.url(), config.user(), config.password());
    private final int runId = config.runId();
    private final Integer assignedTo = config.assignedToId();

    @Override
    public void onTestSuccess(ITestResult result) {
        String caseId = result.getMethod().getConstructorOrMethod().getMethod().getDeclaredAnnotation(TmsLink.class).value();
        testRailClient.publishTestResult(runId, Integer.parseInt(caseId), TestRailClient.StatusId.PASSED, assignedTo);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String tmsId = result.getMethod().getConstructorOrMethod().getMethod().getDeclaredAnnotation(TmsLink.class).value();
        testRailClient.publishTestResult(runId, Integer.parseInt(tmsId), TestRailClient.StatusId.FAILED, assignedTo);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String tmsId = result.getMethod().getConstructorOrMethod().getMethod().getDeclaredAnnotation(TmsLink.class).value();
        testRailClient.publishTestResult(runId, Integer.parseInt(tmsId), TestRailClient.StatusId.RETEST, assignedTo);
    }

    @Override
    public void onFinish(ITestContext context) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onTestStart(ITestResult result) {
    }
}
