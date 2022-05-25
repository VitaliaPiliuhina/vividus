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

package org.vividus.ui.monitor;

import java.lang.reflect.Method;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.proxy.har.HarOnFailureManager;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.json.JsonUtils;

public class PublishingHarOnFailureMonitor extends AbstractPublishingAttachmentOnFailureMonitor
{
    private boolean publishHarOnFailure;

    private final JsonUtils jsonUtils;
    private final HarOnFailureManager harOnFailureManager;

    public PublishingHarOnFailureMonitor(EventBus eventBus, JsonUtils jsonUtils, RunContext runContext,
            IWebDriverProvider webDriverProvider, HarOnFailureManager harOnFailureManager)
    {
        super(runContext, webDriverProvider, eventBus, "noHarOnFailure", "Unable to capture HAR");
        this.jsonUtils = jsonUtils;
        this.harOnFailureManager = harOnFailureManager;
    }

    @Override
    protected Optional<Attachment> createAttachment()
    {
        return harOnFailureManager.takeHar()
                .map(har -> new Attachment(jsonUtils.toJsonAsBytes(har), "har.har"));
    }

    @Override
    protected boolean isPublishingEnabled(Method method)
    {
        return publishHarOnFailure || getAnnotation(method, PublishHarOnFailure.class).isPresent();
    }

    public void setPublishHarOnFailure(boolean publishHarOnFailure)
    {
        this.publishHarOnFailure = publishHarOnFailure;
    }
}
