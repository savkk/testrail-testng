package com.github.savkk.testrail;

import io.qameta.allure.TmsLink;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TestNGRunner {
    private static final String TEST_IDS_SEPARATOR = ",";
    private static final XmlSuite.ParallelMode PARALLEL_MODE = XmlSuite.ParallelMode.NONE;
    private static final int THREAD_COUNT = 2;
    private static final int DATA_PROVIDER_THREAD_COUNT = 2;

    public static void main(String[] args) {
        String runIdParam = Optional
                .ofNullable(System.getProperty("runId"))
                .orElseThrow(() -> new IllegalStateException("Не указан обязательный параметр runId"));
        int runId = Integer.parseInt(runIdParam);
        log.info("TestRail run id = {}", runId);

        String testIdsParam = System.getProperty("testIds");
        List<String> testIdsList = null;
        if (testIdsParam != null && !testIdsParam.isEmpty()) {
            String[] testIds = testIdsParam.split(TEST_IDS_SEPARATOR);
            testIdsList = Arrays.stream(testIds).map(String::trim).collect(Collectors.toList());
            log.info("TestRail selected tests: {}", testIdsList);
        }

        if (System.getProperty("allure.results.directory") == null) {
            System.setProperty("allure.results.directory", "target/allure-results");
        }

        Map<Integer, Integer> testsForRun;
        if (testIdsList != null) {
            testsForRun = TestRailHelper.getTestCasesIds(runId, testIdsList);
        } else {
            testsForRun = TestRailHelper.getTestCasesIds(runId);
        }

        Set<Integer> cases = testsForRun.keySet();
        log.info("TestRail cases for run: {}", cases);
        List<TestMethod> methodsForRun = getTestMethodsForRun(cases);

        if (methodsForRun.isEmpty()) {
            throw new IllegalArgumentException("Нет автотестов для запуска");
        }

        XmlSuite suite = getVirtualSuite("TestRail runId - " + runId, methodsForRun);

        TestNG testNG = new TestNG();
        testNG.setOutputDirectory("target/test-output");
        log.debug("Set virtual suite with methods - {}", methodsForRun);
        testNG.setXmlSuites(Collections.singletonList(suite));
        log.debug("Set report listener");
        testNG.setListenerClasses(Collections.singletonList(TestRailReportListener.class));
        try {
            testNG.run();
        } catch (Exception e) {
            log.error("", e);
            System.exit(-1);
        }
        System.exit(testNG.getStatus());
    }

    /**
     * Метод подготовки вируального набора тестов
     *
     * @param suiteName     - название набора
     * @param methodsForRun - список тестовых методов
     * @return - виртуальный набор тестов
     */
    private static XmlSuite getVirtualSuite(String suiteName, List<TestMethod> methodsForRun) {
        XmlSuite suite = new XmlSuite();
        suite.setName(suiteName);
        suite.setAllowReturnValues(true);

        if (PARALLEL_MODE != XmlSuite.ParallelMode.NONE) {
            log.info("parallel mode - {}, thread count - {}, dataprovider thred count - {}", PARALLEL_MODE, THREAD_COUNT, DATA_PROVIDER_THREAD_COUNT);
            suite.setParallel(PARALLEL_MODE);
            suite.setThreadCount(THREAD_COUNT);
            suite.setDataProviderThreadCount(DATA_PROVIDER_THREAD_COUNT);
        }
        methodsForRun = methodsForRun.stream()
                .sorted(Comparator.comparing(TestMethod::getClassName))
                .collect(Collectors.toList());

        List<XmlClass> classes = new ArrayList<>();
        XmlClass xmlClass = null;
        List<XmlInclude> methods = null;
        for (TestMethod testMethod : methodsForRun) {
            if (xmlClass == null || !xmlClass.getName().equals(testMethod.getClassName())) {
                log.debug("Test class - {}", testMethod.getClassName());
                xmlClass = new XmlClass(testMethod.getClassName());
                classes.add(xmlClass);
                methods = new ArrayList<>();
                xmlClass.setIncludedMethods(methods);
            }
            log.debug(" - test method - {}", testMethod.getMethodName());
            methods.add(new XmlInclude(testMethod.getMethodName()));
        }

        XmlTest test = new XmlTest(suite);
        test.setXmlClasses(classes);
        return suite;
    }


    /**
     * @param testsForRun - набор id тестов из TestRail
     * @return - список тестовых методов, входящих в набор из TestRail
     */
    private static List<TestMethod> getTestMethodsForRun(Set<Integer> testsForRun) {
        final TestRailConfig config = ConfigFactory.create(TestRailConfig.class);
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(config.testsPackage()))
                .setScanners(new MethodAnnotationsScanner()));
        Set<Method> methodsAnnotatedWith = reflections.getMethodsAnnotatedWith(TmsLink.class);

        ArrayList<TestMethod> testMethods = new ArrayList<>();

        methodsAnnotatedWith.forEach(method -> testMethods.add(new TestMethod
                (Integer.valueOf(method.getAnnotation(TmsLink.class).value().replaceFirst("C", "")),
                        method.getDeclaringClass().getName(),
                        method.getName())));

        return testMethods.stream().filter(testMethod -> testsForRun.contains(testMethod.getCaseId())).collect(Collectors.toList());
    }
}
