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

import java.util.Map;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import io.appium.java_client.ios.IOSDriver;

public class NetworkActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkActions.class);

    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;

    public NetworkActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
    }

    public void changeNetworkConnectionState(Mode mode, State state)
    {
        mode.changeNetworkConnectionState(genericWebDriverManager, webDriverProvider, state);
    }

    public enum State
    {
        WIFI("Wi-Fi",
                new ConnectionStateBuilder().withWiFiEnabled().build(),
                new ConnectionStateBuilder().withWiFiDisabled().build()),
        DATA("Mobile Data",
            new ConnectionStateBuilder().withDataEnabled().build(),
            new ConnectionStateBuilder().withDataDisabled().build()),
        AIRPLANE_MODE("Airplane Mode",
            new ConnectionStateBuilder().withAirplaneModeEnabled().build(),
            new ConnectionStateBuilder().withAirplaneModeDisabled().build()),
        ALL("All",
            new ConnectionStateBuilder().withWiFiEnabled().withDataEnabled().build(),
            new ConnectionStateBuilder().withWiFiDisabled().withDataDisabled().build());

        private final ConnectionState disableConnectionState;
        private final ConnectionState enableConnectionState;
        private final String id;

        State(String id, ConnectionState enableConnectionState, ConnectionState disableConnectionState)
        {
            this.id = id;
            this.enableConnectionState = enableConnectionState;
            this.disableConnectionState = disableConnectionState;
        }

        public ConnectionState getDisableConnectionState()
        {
            return disableConnectionState;
        }

        public ConnectionState getEnableConnectionState()
        {
            return enableConnectionState;
        }

        public String getId()
        {
            return id;
        }
    }

    public enum Mode
    {
        DISABLE
        {
            @Override
            public void changeNetworkConnectionState(GenericWebDriverManager genericWebDriverManager,
                    IWebDriverProvider webDriverProvider, State state)
            {
                if (genericWebDriverManager.isAndroid())
                {
                    webDriverProvider.getUnwrapped(AndroidDriver.class)
                            .setConnection(state.getDisableConnectionState());
                }
                if (genericWebDriverManager.isIOS())
                {
                    changeNetworkConnectionStateForIOS(webDriverProvider, state, Mode.DISABLE);
                }
            }
        },
        ENABLE
        {
            @Override
            public void changeNetworkConnectionState(GenericWebDriverManager genericWebDriverManager,
                    IWebDriverProvider webDriverProvider, State state)
            {
                if (genericWebDriverManager.isAndroid())
                {
                    webDriverProvider.getUnwrapped(AndroidDriver.class).setConnection(state.getEnableConnectionState());
                }
                if (genericWebDriverManager.isIOS())
                {
                    changeNetworkConnectionStateForIOS(webDriverProvider, state, Mode.ENABLE);
                }
            }
        };

        public abstract void changeNetworkConnectionState(GenericWebDriverManager genericWebDriverManager,
                IWebDriverProvider webDriverProvider, State state);

        public void changeNetworkConnectionStateForIOS(IWebDriverProvider webDriverProvider, State state, Mode mode)
        {
            Map<String, Object> value = Map.of("bundleId", "com.apple.Preferences");
            IOSDriver driver = webDriverProvider.getUnwrapped(IOSDriver.class);
            driver.executeScript("mobile: activateApp", value);
            driver.findElementById(state.getId()).click();
            WebElement switchBtn = driver.findElementByXPath("//XCUIElementTypeSwitch");
            String switchStatus = switchBtn.getAttribute("value");
            boolean wiFiTurnedEnable = Mode.ENABLE.equals(mode);
            if ("1".equalsIgnoreCase(switchStatus) && wiFiTurnedEnable
                    || "0".equalsIgnoreCase(switchStatus) && !wiFiTurnedEnable)
            {
                switchBtn.click();
            }
            else
            {
                LOGGER.atInfo().addArgument(state).addArgument(mode).log("{} is already {}.");
            }
        }
    }
}
