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
    private static final String TOGGLE_IS_OFF_VALUE = "1";
    private static final String TOGGLE_IS_ON_VALUE = "0";
    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;

    public NetworkActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
    }

    public void changeNetworkConnectionState(NetworkToggle networkToggle, Mode mode)
    {
        if (genericWebDriverManager.isAndroid())
        {
            webDriverProvider.getUnwrapped(AndroidDriver.class).setConnection(NetworkToggle.ON == networkToggle
                    ? mode.getEnabledConnectionState()
                    : mode.getDisabledConnectionState());
        }
        if (genericWebDriverManager.isIOS())
        {
            changeNetworkConnectionStateForIOS(webDriverProvider, mode, networkToggle);
        }
    }

    private void changeNetworkConnectionStateForIOS(IWebDriverProvider webDriverProvider, Mode mode,
            NetworkToggle networkToggle)
    {
        Map<String, Object> value = Map.of("bundleId", "com.apple.Preferences");
        IOSDriver driver = webDriverProvider.getUnwrapped(IOSDriver.class);
        driver.executeScript("mobile: activateApp", value);
        driver.findElementByAccessibilityId(mode.getiOSElementId()).click();
        WebElement switchBtn = driver.findElementByIosClassChain(
                String.format("**/XCUIElementTypeSwitch[`label == \"%s\"`]", mode.getiOSElementId()));
        String switchStatus = switchBtn.getAttribute("value");
        boolean wiFiTurnedEnable = NetworkToggle.ON == networkToggle;
        if (TOGGLE_IS_OFF_VALUE.equals(switchStatus) && wiFiTurnedEnable
                || TOGGLE_IS_ON_VALUE.equals(switchStatus) && !wiFiTurnedEnable)
        {
            switchBtn.click();
        }
        else
        {
            LOGGER.atInfo().addArgument(mode).addArgument(networkToggle).log("{} is already {}.");
        }
    }

    public enum Mode
    {
        WIFI("Wi-Fi", new ConnectionStateBuilder().withWiFiEnabled().build(),
                new ConnectionStateBuilder().withWiFiDisabled().build()),
        MOBILE_DATA("Mobile Data", new ConnectionStateBuilder().withDataEnabled().build(),
                new ConnectionStateBuilder().withDataDisabled().build()),
        AIRPLANE_MODE(null, new ConnectionStateBuilder().withAirplaneModeEnabled().build(),
                new ConnectionStateBuilder().withAirplaneModeDisabled().build()),
        ALL(null, new ConnectionStateBuilder().withWiFiEnabled().withDataEnabled().build(),
                new ConnectionStateBuilder().withWiFiDisabled().withDataDisabled().build());

        private final String iOSElementId;
        private final ConnectionState disabledConnectionState;
        private final ConnectionState enabledConnectionState;

        Mode(String iOSElementId, ConnectionState enabledConnectionState, ConnectionState disabledConnectionState)
        {
            this.iOSElementId = iOSElementId;
            this.enabledConnectionState = enabledConnectionState;
            this.disabledConnectionState = disabledConnectionState;
        }

        public ConnectionState getDisabledConnectionState()
        {
            return disabledConnectionState;
        }

        public ConnectionState getEnabledConnectionState()
        {
            return enabledConnectionState;
        }

        public String getiOSElementId()
        {
            return iOSElementId;
        }
    }

    public enum NetworkToggle
    {
        ON, OFF
    }
}
