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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarEntry;
import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.json.JsonUtils;

public class GenericHarOnFailureMonitor extends AbstractHarOnFailureMonitor
{
    @Inject private IProxy proxy;
    private List<HarEntry> harEntries = new ArrayList<>();

    public GenericHarOnFailureMonitor(EventBus eventBus, JsonUtils jsonUtils, RunContext runContext,
            IWebDriverProvider webDriverProvider)
    {
        super(eventBus, jsonUtils, runContext, webDriverProvider);
    }

    @Override
    protected Optional<Har> takeAssertionFailureHar()
    {
        return Optional.of(createHarWithUniqueEntries(proxy.getRecordedData()));
    }

    public void setProxy(IProxy proxy)
    {
        this.proxy = proxy;
    }

    private Har createHarWithUniqueEntries(Har har)
    {
        Har newHar;
        if (0 != harEntries.size() && !har.getLog().getEntries().equals(harEntries))
        {
            newHar = addHarLog(har);
            newHar.getLog().getEntries().removeAll(harEntries);
        }
        else
        {
            newHar = har;
        }
        setHarEntries(har.getLog().getEntries());
        return newHar;
    }

    public void setHarEntries(List<HarEntry> entries)
    {
        this.harEntries.clear();
        this.harEntries.addAll(entries);
    }

    private Har addHarLog(Har proxyHar)
    {
        Har newHar = new Har();
        newHar.getLog().getEntries().addAll(proxyHar.getLog().getEntries());
        newHar.getLog().setBrowser(proxyHar.getLog().getBrowser());
        newHar.getLog().setVersion(proxyHar.getLog().getVersion());
        newHar.getLog().setComment(proxyHar.getLog().getComment());
        newHar.getLog().setCreator(proxyHar.getLog().getCreator());
        newHar.getLog().getPages().addAll(proxyHar.getLog().getPages());
        return newHar;
    }
}
