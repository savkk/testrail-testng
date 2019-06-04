package com.github.savkk.testrail;

import com.codepine.api.testrail.TestRail;
import com.codepine.api.testrail.model.Result;
import com.codepine.api.testrail.model.ResultField;
import com.codepine.api.testrail.model.Test;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TestRailHelper {
    private static final TestRailConfig config = ConfigFactory.create(TestRailConfig.class);
    private static final TestRail TEST_RAIL = TestRail.builder(
            config.url(),
            config.user(),
            config.password())
            .build();

    private TestRailHelper() {
        throw new IllegalAccessError("Helper class");
    }

    /**
     * Получить CaseId и TestId тестов из Test Run
     *
     * @param runId - id тест рана
     * @return Map - ключ case id, значение test id
     */
    public static Map<Integer, Integer> getTestCasesIds(Integer runId) {
        List<Test> testList = TEST_RAIL.tests().list(runId).execute();
        return testList.stream().collect(Collectors.toMap(Test::getCaseId, Test::getId));
    }

    public static Result publishTestResult(int runId, int caseId, StatusId statusId) {
        Result result = new Result();
        result.setStatusId(statusId.getCode());
        result.setComment(System.getenv("BUILD_URL"));
        List<ResultField> customResultFields = TEST_RAIL.resultFields().list().execute();
        return TEST_RAIL.results().addForCase(runId, caseId, result, customResultFields).execute();
    }

    public enum StatusId {
        PASSED(1),
        BLOCKED(2),
        RETEST(4),
        FAILED(5);

        private int statusCode;

        StatusId(int statusCode) {
            this.statusCode = statusCode;
        }

        public int getCode() {
            return statusCode;
        }
    }
}
