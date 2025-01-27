/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.selenium;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.vividus.context.RunContext;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.manager.IWebDriverManagerContext;

public class VividusWebDriverFactory extends AbstractVividusWebDriverFactory
{
    private final IWebDriverFactory webDriverFactory;

    private List<WebDriverEventListener> webDriverEventListeners;

    public VividusWebDriverFactory(boolean remoteExecution, IWebDriverManagerContext webDriverManagerContext,
            RunContext runContext, Optional<Set<DesiredCapabilitiesConfigurer>> desiredCapabilitiesConfigurers,
            IWebDriverFactory webDriverFactory, IProxy proxy)
    {
        super(remoteExecution, webDriverManagerContext, runContext, proxy, desiredCapabilitiesConfigurers);
        this.webDriverFactory = webDriverFactory;
    }

    @Override
    protected WebDriver createWebDriver(DesiredCapabilities desiredCapabilities)
    {
        WebDriver webDriver = isRemoteExecution()
                ? webDriverFactory.getRemoteWebDriver(desiredCapabilities)
                : webDriverFactory.getWebDriver(desiredCapabilities);

        EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(webDriver);
        webDriverEventListeners.forEach(eventFiringWebDriver::register);
        return eventFiringWebDriver;
    }

    public void setWebDriverEventListeners(List<WebDriverEventListener> webDriverEventListeners)
    {
        this.webDriverEventListeners = Collections.unmodifiableList(webDriverEventListeners);
    }
}
