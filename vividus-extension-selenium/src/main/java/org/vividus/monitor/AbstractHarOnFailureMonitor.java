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
import java.util.Map;
import java.util.Optional;

import com.browserup.harreader.model.Har;
import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.json.JsonUtils;

public abstract class AbstractHarOnFailureMonitor extends AbstractFileOnFailureMonitor
{
    private static final String NO_HAR_ON_FAILURE_META_NAME = "noHarOnFailure";

    private boolean harOnFailure = true;

    private final ThreadLocal<Boolean> takeHarOnFailureEnabled = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final EventBus eventBus;
    private final JsonUtils jsonUtils;
    private final IAttachmentPublisher attachmentPublisher;

    public AbstractHarOnFailureMonitor(EventBus eventBus, JsonUtils jsonUtils, RunContext runContext,
            IWebDriverProvider webDriverProvider, IAttachmentPublisher attachmentPublisher)
    {
        super(runContext, webDriverProvider, NO_HAR_ON_FAILURE_META_NAME);
        this.eventBus = eventBus;
        this.jsonUtils = jsonUtils;
        this.attachmentPublisher = attachmentPublisher;
    }

    @Override
    protected void attachFileOnFailure() throws IOException
    {
        takeAssertionFailureHar().ifPresent(har -> {
            Attachment attachment = new Attachment(jsonUtils.toJsonAsByteArray(har), "har.har");
            eventBus.post(new AttachmentPublishEvent(attachment));
        });
        addViewer();
    }

    protected void addViewer() throws IOException
    {
        Har har =  takeAssertionFailureHar().get();
        attachmentPublisher.publishAttachment("/templates/har-viewer.html",
                Map.of("har", jsonUtils.toJson(har)), "s3u/har-view");
        attachmentPublisher.publishAttachment("/templates/harView.html",
                Map.of("har", jsonUtils.toJson(har)), "HAR Viewer");
    }

    @Override
    protected boolean takeAssertionFailure(Method method)
    {
        if (!harOnFailure && method != null)
        {
            AnnotatedElement annotatedElement = method.isAnnotationPresent(PublishHarOnFailure.class) ?
                    method :
                    method.getDeclaringClass();
            PublishHarOnFailure annotation = annotatedElement.getAnnotation(PublishHarOnFailure.class);
            return annotation != null;
        }
        return harOnFailure;
    }

    public void setHarOnFailure(boolean harOnFailure)
    {
        this.harOnFailure = harOnFailure;
    }

    protected abstract Optional<Har> takeAssertionFailureHar() throws IOException;

    public ThreadLocal<Boolean> takeFileOnFailureEnabled()
    {
        return takeHarOnFailureEnabled;
    }
}
