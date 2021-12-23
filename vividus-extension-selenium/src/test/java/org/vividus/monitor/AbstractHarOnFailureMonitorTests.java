/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.monitor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.browserup.harreader.model.Har;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.annotations.When;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.proxy.IProxy;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.util.json.JsonUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AbstractHarOnFailureMonitorTests
{
    private static final String I_DO_ACTION = "I do action";
    private static final String WHEN_STEP_METHOD = "whenStep";
    private static final String NO_HAR_ON_FAILURE_META_NAME = "noHarOnFailure";
    private static final Meta EMPTY_META = new Meta();

    @Mock private IProxy proxy;
    @Mock private EventBus eventBus;
    @Mock private Har har;
    @Mock private JsonUtils jsonUtils;
    @Mock private RunContext runContext;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private GenericHarOnFailureMonitor genericHarOnFailureMonitor;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractHarOnFailureMonitorTests.class);

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(eventBus, runContext, webDriverProvider);
    }

    @TestFactory
    Stream<DynamicTest> shouldProcessStepWithAnnotation() throws NoSuchMethodException
    {
        return Stream.of(getTakingHarMethod(),
                        AbstractHarOnFailureMonitorTests.TestSteps.class.getDeclaredMethod("innerWhenStep"))
                .flatMap(method ->
                {
                    reset(runContext);
                    mockScenarioAndStoryMeta(EMPTY_META);
                    return Stream.of(
                            dynamicTest("beforePerformingProcessesStepWithAnnotation",
                                    () -> genericHarOnFailureMonitor.beforePerforming(I_DO_ACTION, false, method)),
                            dynamicTest("afterPerformingProcessesStepWithAnnotation",
                                    () -> genericHarOnFailureMonitor.afterPerforming(I_DO_ACTION, false, method))
                    );
                });
    }

    private Method getTakingHarMethod() throws NoSuchMethodException
    {
        return getClass().getDeclaredMethod(WHEN_STEP_METHOD);
    }

    @TestFactory
    Stream<DynamicTest> shouldIgnoreStepWithoutAnnotation() throws NoSuchMethodException
    {
        return Stream.of(getClass().getDeclaredMethod("anotherWhenStep"), null)
                .flatMap(method -> Stream.of(
                        dynamicTest("beforePerformingIgnoresStepWithoutAnnotation",
                                () -> genericHarOnFailureMonitor.beforePerforming(I_DO_ACTION, false, method)),
                        dynamicTest("afterPerformingIgnoresStepWithoutAnnotation",
                                () -> genericHarOnFailureMonitor.afterPerforming(I_DO_ACTION, false, method))));
    }

    @Test
    void shouldEnableHarIfNoRunningScenario() throws NoSuchMethodException, IOException
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, EMPTY_META);
        genericHarOnFailureMonitor.beforePerforming(I_DO_ACTION, false, getTakingHarMethod());
        genericHarOnFailureMonitor.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
    }

    @Test
    void shouldNotTakeHarIfMethodAnnotatedButStoryHasNoHarOnFailure() throws NoSuchMethodException, IOException
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, new Meta(List.of(NO_HAR_ON_FAILURE_META_NAME)));
        genericHarOnFailureMonitor.beforePerforming(I_DO_ACTION, false, getTakingHarMethod());
        genericHarOnFailureMonitor.onAssertionFailure(null);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldNotTakeHarIfMethodAnnotatedButScenarioHasNoHarOnFailure() throws NoSuchMethodException, IOException
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, EMPTY_META);
        mockScenarioMeta(runningStory, new Meta(List.of(NO_HAR_ON_FAILURE_META_NAME)));
        genericHarOnFailureMonitor.beforePerforming(I_DO_ACTION, false, getTakingHarMethod());
        genericHarOnFailureMonitor.onAssertionFailure(null);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldTakeHarAttachment() throws NoSuchMethodException, IOException
    {
        genericHarOnFailureMonitor.setHarOnFailure(true);
        enableHarPublishing(true);
        genericHarOnFailureMonitor.setProxy(proxy);
        byte[] data = jsonUtils.toJsonAsByteArray(har);
        GenericHarOnFailureMonitor spy = spy(genericHarOnFailureMonitor);
        spy.setHarOnFailure(true);
        doReturn(Optional.of(har)).when(spy).takeAssertionFailureHar();
        spy.onAssertionFailure(mock(AssertionFailedEvent.class));
        verify(eventBus).post(argThat((ArgumentMatcher<AttachmentPublishEvent>) event ->
        {
            Attachment attachment = event.getAttachment();
            return Arrays.equals(data, attachment.getContent());
        }));
        assertThat(logger.getLoggingEvents(), empty());
    }

    private void enableHarPublishing(boolean webDriverInitialized) throws NoSuchMethodException
    {
        mockScenarioAndStoryMeta(EMPTY_META);
        genericHarOnFailureMonitor.beforePerforming(I_DO_ACTION, false, getClass().getDeclaredMethod(WHEN_STEP_METHOD));
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(webDriverInitialized);
    }

    private void mockScenarioAndStoryMeta(Meta meta)
    {
        RunningStory runningStory = mock(RunningStory.class);
        mockStoryMeta(runningStory, meta);
        mockScenarioMeta(runningStory, meta);
    }

    private void mockStoryMeta(RunningStory runningStory, Meta meta)
    {
        when(runContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getStory()).thenReturn(new Story(null, null, meta, null, null));
    }

    private void mockScenarioMeta(RunningStory runningStory, Meta meta)
    {
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningScenario.getScenario()).thenReturn(new Scenario("test scenario", meta));
    }

    @PublishHarOnFailure
    @When(I_DO_ACTION)
    void whenStep()
    {
        // nothing to do
    }

    @When(I_DO_ACTION)
    void anotherWhenStep()
    {
        // nothing to do
    }

    @PublishHarOnFailure
    static class TestSteps
    {
        @When(I_DO_ACTION)
        void innerWhenStep()
        {
            // nothing to do
        }
    }
}
