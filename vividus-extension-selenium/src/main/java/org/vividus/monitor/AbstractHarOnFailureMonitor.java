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

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

import com.browserup.harreader.model.Har;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.NullStepMonitor;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.util.json.JsonUtils;

public abstract class AbstractHarOnFailureMonitor extends NullStepMonitor
{
    private static final String NO_HAR_ON_FAILURE_META_NAME = "noHarOnFailure";

    private boolean harOnFailure;

    private final ThreadLocal<Boolean> takeHarOnFailureEnabled = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final EventBus eventBus;
    private final RunContext runContext;
    private final JsonUtils jsonUtils;
    private final IWebDriverProvider webDriverProvider;

    public AbstractHarOnFailureMonitor(EventBus eventBus, JsonUtils jsonUtils,
            RunContext runContext, IWebDriverProvider webDriverProvider)
    {
        this.eventBus = eventBus;
        this.runContext = runContext;
        this.jsonUtils = jsonUtils;
        this.webDriverProvider = webDriverProvider;
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method)
    {
        if (takeHarOnFailure(method) && !isStoryHasNoHarOnFailureMeta()
                && !isScenarioHasNoScreenshotsOnFailureMeta())
        {
            enableHarOnFailure();
        }
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method)
    {
        if (takeHarOnFailure(method))
        {
            disableHarOnFailure();
        }
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event) throws IOException
    {
        if (takeHarOnFailureEnabled.get() && webDriverProvider.isWebDriverInitialized())
        {
            takeAssertionFailureHar().ifPresent(har -> {
                Attachment attachment = new Attachment(jsonUtils.toJsonAsByteArray(har), "har.har");
                eventBus.post(new AttachmentPublishEvent(attachment));
            });
        }
    }

    private boolean takeHarOnFailure(Method method)
    {
        if (!harOnFailure && method != null)
        {
            AnnotatedElement annotatedElement = method.isAnnotationPresent(PublishHarOnFailure.class) ? method
                    : method.getDeclaringClass();
            PublishHarOnFailure annotation = annotatedElement.getAnnotation(PublishHarOnFailure.class);
            return annotation != null;
        }
        return harOnFailure;
    }

    public void setHarOnFailure(boolean harOnFailure)
    {
        this.harOnFailure = harOnFailure;
    }

    private void enableHarOnFailure()
    {
        takeHarOnFailureEnabled.set(Boolean.TRUE);
    }

    private void disableHarOnFailure()
    {
        takeHarOnFailureEnabled.set(Boolean.FALSE);
    }

    protected abstract Optional<Har> takeAssertionFailureHar() throws IOException;

    private boolean isStoryHasNoHarOnFailureMeta()
    {
        RunningStory runningStory = runContext.getRunningStory();
        return runningStory.getStory().getMeta().hasProperty(NO_HAR_ON_FAILURE_META_NAME);
    }

    private boolean isScenarioHasNoScreenshotsOnFailureMeta()
    {
        return Optional.of(runContext.getRunningStory())
                .map(RunningStory::getRunningScenario)
                .map(RunningScenario::getScenario)
                .map(Scenario::getMeta)
                .map(m -> m.hasProperty(NO_HAR_ON_FAILURE_META_NAME)).orElse(Boolean.FALSE);
    }
}
