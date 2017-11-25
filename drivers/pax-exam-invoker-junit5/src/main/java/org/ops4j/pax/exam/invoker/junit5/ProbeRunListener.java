/*
 * Copyright 2016 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.invoker.junit5;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;

/**
 * @author hwellmann
 *
 */
public class ProbeRunListener implements TestExecutionListener {


    private TestListener delegate;

    public ProbeRunListener(TestListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (!testIdentifier.isContainer()) {
            delegate.testStarted(toDescription(testIdentifier));
        }
    }

    /**
     * Converts test identifier name of format org.ops4j.pax.exam.junit5.BlueTest#blue1().
     * @param testIdentifier
     * @return
     */
    private TestDescription toDescription(TestIdentifier testIdentifier) {
        String uniqueId = testIdentifier.getUniqueId();
        String[] parts = uniqueId.split("/");
        if (parts.length < 2) {
            return null;
        }
        String methodName = null;
        if (parts.length >= 3) {
            String methodSegment = parts[2];
            int colon = methodSegment.indexOf(":");
            methodName = methodSegment.substring(colon + 1, methodSegment.length() - 3);
        }
        String classSegment = parts[1];
        int colon = classSegment.indexOf(":");
        String className = classSegment.substring(colon + 1, classSegment.length() - 1);

        return new TestDescription(className, methodName);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (!testIdentifier.isContainer()) {
            delegate.testIgnored(toDescription(testIdentifier));
        }
    }



    @Override
    public void executionFinished(TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult) {
        if (!testIdentifier.isContainer()) {
            TestDescription description = toDescription(testIdentifier);
            testExecutionResult.getThrowable().ifPresent(exc ->
                delegate.testFailure(new TestFailure(description, exc)));
            delegate.testFinished(description);
        }
    }
}
