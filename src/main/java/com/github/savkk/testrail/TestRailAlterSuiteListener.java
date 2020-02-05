package com.github.savkk.testrail;

import com.github.savkk.testrail.config.TestRailConfig;
import com.github.savkk.testrail.model.TestMethod;
import io.qameta.allure.TmsLink;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TestRailAlterSuiteListener implements IAlterSuiteListener {
    private static final TestRailConfig config = ConfigFactory.create(TestRailConfig.class);
    private final TestRailClient testRailClient = new TestRailClient(config.url(), config.user(), config.password());

    private static final XmlSuite.ParallelMode PARALLEL_MODE = config.parallelMode();
    private static final int THREAD_COUNT = config.threadCount();
    private static final int DATA_PROVIDER_THREAD_COUNT = config.dataProviderThreadCount();

    @Override
    public void alter(List<XmlSuite> suites) {
        if (!config.testrailEnabled()) {
            return;
        }
        Integer runId = Optional
                .ofNullable(config.runId())
                .orElseThrow(() -> new IllegalStateException("Не указан обязательный параметр runId"));
        log.info("TestRail run id = {}", runId);

        List<Integer> testIdsList = config.testIds();
        log.info("TestRail selected tests: {}", testIdsList);

        Map<Integer, Integer> testsForRun;
        if (testIdsList != null) {
            testsForRun = testRailClient.getTestCasesIds(runId, testIdsList);
        } else {
            testsForRun = testRailClient.getTestCasesIds(runId);
        }

        Set<Integer> cases = testsForRun.keySet();
        log.info("TestRail cases for run: {}", cases);
        List<TestMethod> methodsForRun = getTestMethodsForRun(cases);

        if (methodsForRun.isEmpty()) {
            throw new IllegalArgumentException("Нет автотестов для запуска");
        }
        suites.clear();
        suites.add(getVirtualSuite("TestRail runId - " + runId, methodsForRun));
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
                (Integer.parseInt(method.getAnnotation(TmsLink.class).value().replaceFirst("C", "")),
                        method.getDeclaringClass().getName(),
                        method.getName())));

        return testMethods.stream().filter(testMethod -> testsForRun.contains(testMethod.getCaseId())).collect(Collectors.toList());
    }
}
