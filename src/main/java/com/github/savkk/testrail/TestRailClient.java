package com.github.savkk.testrail;

import com.codepine.api.testrail.TestRail;
import com.codepine.api.testrail.model.Result;
import com.codepine.api.testrail.model.ResultField;
import com.codepine.api.testrail.model.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TestRailClient {
    private final TestRail testRail;

    public TestRailClient(String endPoint, String user, String password) {
        testRail = TestRail.builder(endPoint, user, password).build();
    }

    /**
     * Получить CaseId и TestId тестов из Test Run
     *
     * @param runId - id тест рана
     * @return Map - ключ case id, значение test id
     */
    public Map<Integer, Integer> getTestCasesIds(Integer runId) {
        List<Test> testList = testRail.tests().list(runId).execute();
        return testList.stream().collect(Collectors.toMap(Test::getCaseId, Test::getId));
    }

    public Map<Integer, Integer> getTestCasesIds(int runId, List<Integer> testIds) {
        List<Test> testList = testRail.tests().list(runId).execute();
        return testList
                .stream()
                .filter(test ->
                        testIds.contains(test.getId()))
                .collect(Collectors.toMap(Test::getCaseId, Test::getId));
    }

    public Result publishTestResult(int runId, int caseId, StatusId statusId, Integer assignedToId) {
        Result result = new Result();
        result.setStatusId(statusId.getCode());
        result.setComment(System.getenv("BUILD_URL"));
        if (assignedToId != null) {
            result.setAssignedtoId(assignedToId);
        }
        List<ResultField> customResultFields = testRail.resultFields().list().execute();
        return testRail.results().addForCase(runId, caseId, result, customResultFields).execute();
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
