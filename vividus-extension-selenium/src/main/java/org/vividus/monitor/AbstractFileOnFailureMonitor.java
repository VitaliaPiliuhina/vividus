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
import java.lang.reflect.Method;
import java.util.Optional;

import com.google.common.eventbus.Subscribe;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.steps.NullStepMonitor;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;

public abstract class AbstractFileOnFailureMonitor  extends NullStepMonitor
{
    private final String metaName;
    private final RunContext runContext;
    private final IWebDriverProvider webDriverProvider;

    public  AbstractFileOnFailureMonitor(RunContext runContext, IWebDriverProvider webDriverProvider, String metaName)
    {
        this.runContext = runContext;
        this.webDriverProvider = webDriverProvider;
        this.metaName = metaName;
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method)
    {
        if (takeAssertionFailure(method) && !isStoryFileNoHarOnFailureMeta()
                && !isScenarioHasNoFileOnFailureMeta())
        {
            enableFileOnFailure();
        }
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method)
    {
        if (takeAssertionFailure(method))
        {
            disableFileOnFailure();
        }
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event) throws IOException
    {
        if (takeFileOnFailureEnabled().get() && webDriverProvider.isWebDriverInitialized())
        {
            attachFileOnFailure();
        }
    }

    private boolean isStoryFileNoHarOnFailureMeta()
    {
        RunningStory runningStory = runContext.getRunningStory();
        return runningStory.getStory().getMeta().hasProperty(metaName);
    }

    private boolean isScenarioHasNoFileOnFailureMeta()
    {
        return Optional.of(runContext.getRunningStory())
                .map(RunningStory::getRunningScenario)
                .map(RunningScenario::getScenario)
                .map(Scenario::getMeta)
                .map(m -> m.hasProperty(metaName)).orElse(Boolean.FALSE);
    }

    private void enableFileOnFailure()
    {
        takeFileOnFailureEnabled().set(Boolean.TRUE);
    }

    private void disableFileOnFailure()
    {
        takeFileOnFailureEnabled().set(Boolean.FALSE);
    }

    protected abstract boolean takeAssertionFailure(Method method);

    protected abstract void attachFileOnFailure() throws IOException;

    protected abstract ThreadLocal<Boolean> takeFileOnFailureEnabled();
}
