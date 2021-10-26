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

package org.vividus.mobileapp.action;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import io.appium.java_client.ios.IOSDriver;

public class NetworkActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkActions.class);

    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;

    public NetworkActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager)
    {
        this.genericWebDriverManager = genericWebDriverManager;
        this.webDriverProvider = webDriverProvider;
    }

    public void changeNetworkConnection(State action)
    {
        action.changeWiFiAndDataConditions(genericWebDriverManager, webDriverProvider);
    }

    public void switchWiFi(State action)
    {
        action.switchWiFiToggle(genericWebDriverManager, webDriverProvider);
    }

    public enum State
    {
        ON
        {
            @Override
            public void switchWiFiToggle(GenericWebDriverManager genericWebDriverManager,
                    IWebDriverProvider webDriverProvider)
            {
                if (genericWebDriverManager.isAndroid()
                        && !webDriverProvider.getUnwrapped(AndroidDriver.class).getConnection().isWiFiEnabled())
                {
                    webDriverProvider.getUnwrapped(AndroidDriver.class).toggleWifi();
                }
                if (genericWebDriverManager.isIOS())
                {
                    switchWiFiForIOS(webDriverProvider, true);
                }
            }

            @Override
            public void changeWiFiAndDataConditions(GenericWebDriverManager genericWebDriverManager,
                    IWebDriverProvider webDriverProvider)
            {
                webDriverProvider.getUnwrapped(AndroidDriver.class)
                        .setConnection(new ConnectionStateBuilder().withWiFiEnabled().withDataEnabled().build());
            }
        },

        OFF
        {
            @Override
            public void switchWiFiToggle(GenericWebDriverManager genericWebDriverManager,
                    IWebDriverProvider webDriverProvider)
            {
                if (genericWebDriverManager.isAndroid()
                        && webDriverProvider.getUnwrapped(AndroidDriver.class).getConnection().isWiFiEnabled())
                {
                    webDriverProvider.getUnwrapped(AndroidDriver.class).toggleWifi();
                }
                if (genericWebDriverManager.isIOS())
                {
                    switchWiFiForIOS(webDriverProvider, false);
                }
            }

            @Override
            public void changeWiFiAndDataConditions(GenericWebDriverManager genericWebDriverManager,
                    IWebDriverProvider webDriverProvider)
            {
                webDriverProvider.getUnwrapped(AndroidDriver.class)
                        .setConnection(new ConnectionStateBuilder().withWiFiDisabled().withDataDisabled().build());
            }
        };

        public abstract void switchWiFiToggle(GenericWebDriverManager genericWebDriverManager,
                IWebDriverProvider webDriverProvider);

        public abstract void changeWiFiAndDataConditions(GenericWebDriverManager genericWebDriverManager,
                IWebDriverProvider webDriverProvider);

        public void switchWiFiForIOS(IWebDriverProvider webDriverProvider, boolean isNeedWiFiOn)
        {
            HashMap<String, Object> value = new HashMap<>();
            value.put("bundleId", "com.apple.Preferences");
            IOSDriver driver = webDriverProvider.getUnwrapped(IOSDriver.class);
            driver.executeScript("mobile: launchApp", value);
            driver.executeScript("mobile: activateApp", value);
            driver.findElementByXPath("//XCUIElementTypeCell[@name='Wi-Fi']").click();
            MobileElement switchBtn = (MobileElement) driver.findElementByXPath("//XCUIElementTypeSwitch");
            String switchStatus = switchBtn.getAttribute("value");
            if ("1".equalsIgnoreCase(switchStatus) && isNeedWiFiOn
                    || "0".equalsIgnoreCase(switchStatus) && !isNeedWiFiOn)
            {
                switchBtn.click();
            }
            else
            {
                LOGGER.atInfo().addArgument(isNeedWiFiOn ? ON.toString() : OFF.toString()).log("Wi-Fi is already {}.");
            }
        }
    }
}
