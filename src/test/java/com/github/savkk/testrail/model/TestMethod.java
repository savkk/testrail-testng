package com.github.savkk.testrail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class TestMethod {
    private int caseId;
    private String className;
    private String methodName;
}