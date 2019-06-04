package com.github.savkk.testrail;

import io.qameta.allure.TmsLink;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestRailReportListener implements ITestListener {
    private static final Integer runId = Integer.valueOf(System.getProperty("runId"));

    @Override
    public void onTestSuccess(ITestResult result) {
        String caseId = result.getMethod().getConstructorOrMethod().getMethod().getDeclaredAnnotation(TmsLink.class).value();
        TestRailHelper.publishTestResult(runId, Integer.parseInt(caseId), TestRailHelper.StatusId.PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String tmsId = result.getMethod().getConstructorOrMethod().getMethod().getDeclaredAnnotation(TmsLink.class).value();
        TestRailHelper.publishTestResult(runId, Integer.parseInt(tmsId), TestRailHelper.StatusId.FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String tmsId = result.getMethod().getConstructorOrMethod().getMethod().getDeclaredAnnotation(TmsLink.class).value();
        TestRailHelper.publishTestResult(runId, Integer.parseInt(tmsId), TestRailHelper.StatusId.RETEST);
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
