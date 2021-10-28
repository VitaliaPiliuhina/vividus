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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.vividus.mobileapp.action.NetworkActions.State;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import io.appium.java_client.ios.IOSDriver;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class NetworkConditionsActionsTests
{
    private static final String VALUE = "value";
    private static final String XCUIELEMENT_TYPE_CELL = "//XCUIElementTypeCell[@name='Wi-Fi']";
    private static final String XCUIELEMENT_TYPE_SWITCH = "//XCUIElementTypeSwitch";

    @Mock
    private IWebDriverProvider webDriverProvider;
    @Mock
    private GenericWebDriverManager genericWebDriverManager;
    @InjectMocks
    private NetworkActions networkActions;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(NetworkActions.class);

    @ParameterizedTest
    @EnumSource(State.class)
    void testDisableConnectionStateBuilder(State state)
    {
        AndroidDriver driver = mock(AndroidDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.getUnwrapped(AndroidDriver.class)).thenReturn(driver);
        networkActions.disableNetworkConnectionState(state);
        verify(driver).setConnection(any(ConnectionState.class));
    }

    @ParameterizedTest
    @EnumSource(State.class)
    void testEnableConnectionStateBuilder(State state)
    {
        AndroidDriver driver = mock(AndroidDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.getUnwrapped(AndroidDriver.class)).thenReturn(driver);
        networkActions.enableNetworkConnectionState(state);
        verify(driver).setConnection(any(ConnectionState.class));
    }

    @ParameterizedTest
    @CsvSource({ "'1', ON", "'0', OFF" })
    void testWiFiToggleForIOS(String toggleState, String state)
    {
        IOSDriver driver = mock(IOSDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        MobileElement wiFiElement = mock(MobileElement.class);
        MobileElement switchBtn = mock(MobileElement.class);
        when(webDriverProvider.getUnwrapped(IOSDriver.class)).thenReturn(driver);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_CELL)).thenReturn(wiFiElement);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_SWITCH)).thenReturn(switchBtn);
        when(switchBtn.getAttribute(VALUE)).thenReturn(toggleState);
        networkActions.switchWiFiForIOS(state);
        verify(switchBtn).click();
    }

    @ParameterizedTest
    @CsvSource({ "'0', ON", "'1', OFF" })
    void testAlreadyModifiedWiFiToggleForIOS(String toggleState, String mode)
    {
        IOSDriver driver = mock(IOSDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        MobileElement wiFiElement = mock(MobileElement.class);
        MobileElement switchBtn = mock(MobileElement.class);
        when(webDriverProvider.getUnwrapped(IOSDriver.class)).thenReturn(driver);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_CELL)).thenReturn(wiFiElement);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_SWITCH)).thenReturn(switchBtn);
        when(switchBtn.getAttribute(VALUE)).thenReturn(toggleState);
        networkActions.switchWiFiForIOS(mode);
        assertThat(logger.getLoggingEvents(), is(List.of(info("Wi-Fi is already {}.", mode))));
    }
}
