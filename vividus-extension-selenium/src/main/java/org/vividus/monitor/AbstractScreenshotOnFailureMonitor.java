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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.RunContext;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.Screenshot;

public abstract class AbstractScreenshotOnFailureMonitor extends AbstractFileOnFailureMonitor
{
    private static final String NO_SCREENSHOT_ON_FAILURE_META_NAME = "noScreenshotOnFailure";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScreenshotOnFailureMonitor.class);

    private List<String> debugModes;

    private final ThreadLocal<Boolean> takeScreenshotOnFailureEnabled = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final EventBus eventBus;

    public AbstractScreenshotOnFailureMonitor(EventBus eventBus, RunContext runContext,
            IWebDriverProvider webDriverProvider)
    {
        super(runContext, webDriverProvider, NO_SCREENSHOT_ON_FAILURE_META_NAME);
        this.eventBus = eventBus;
    }

    @Override
    protected void attachFileOnFailure()
    {
        try
        {
            takeAssertionFailureScreenshot("Assertion_Failure").ifPresent(screenshot ->
            {
                Attachment attachment = new Attachment(screenshot.getData(), screenshot.getFileName());
                eventBus.post(new AttachmentPublishEvent(attachment));
            });
        }
        // CHECKSTYLE:OFF
        catch (RuntimeException e)
        {
            LOGGER.error("Unable to take a screenshot", e);
        }
    }

    @Override
    protected boolean takeAssertionFailure(Method method)
    {
        if (method != null)
        {
            AnnotatedElement annotatedElement = method.isAnnotationPresent(TakeScreenshotOnFailure.class) ? method
                    : method.getDeclaringClass();
            TakeScreenshotOnFailure annotation = annotatedElement.getAnnotation(TakeScreenshotOnFailure.class);
            if (annotation != null)
            {
                String debugModeProperty = annotation.onlyInDebugMode();
                return debugModeProperty.isEmpty() || debugModes != null
                                                       && debugModes.stream().anyMatch(debugModeProperty::equals);
            }
        }
        return false;
    }

    protected abstract Optional<Screenshot> takeAssertionFailureScreenshot(String screenshotName);

    public void setDebugModes(List<String> debugModes)
    {
        this.debugModes = debugModes;
    }

    public ThreadLocal<Boolean> takeFileOnFailureEnabled()
    {
        return takeScreenshotOnFailureEnabled;
    }
}
