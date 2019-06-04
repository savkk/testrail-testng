package com.github.savkk.testrail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class TestMethod {
    private int caseId;
    private int testId;
    private String className;
    private String methodName;
}