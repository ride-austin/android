package com.rideaustin;

import android.os.Bundle;
import android.support.test.internal.runner.listener.InstrumentationResultPrinter;
import android.support.test.internal.runner.listener.InstrumentationRunListener;

import org.apache.commons.lang3.StringUtils;
import org.junit.runner.Description;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Prints tests annotations to execution plan.
 * Annotations may be used by ADB commands, report generation, etc.
 *
 * https://medium.com/medisafe-tech-blog/running-android-ui-tests-53e85e5c8da8
 */
public class AnnotationsPrinter extends InstrumentationRunListener {

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);

        Collection<Annotation> annotations = description.getAnnotations();
        if (annotations == null) {
            return;
        }

        Bundle bundle = new Bundle();
        StringBuilder stringBuilder = new StringBuilder();
        String testCases = "";
        boolean comm = false;
        for (Annotation annotation : annotations) {
            if (comm) stringBuilder.append(",");
            stringBuilder.append(annotation.annotationType().getSimpleName());
            if (annotation instanceof TestCases) {
                testCases = StringUtils.join(((TestCases) annotation).value(), ",");
            }
            comm = true;
        }

        bundle.putString("annotations", stringBuilder.toString());
        bundle.putString("testCases", testCases);
        getInstrumentation().sendStatus(InstrumentationResultPrinter.REPORT_VALUE_RESULT_START, bundle);
    }
}