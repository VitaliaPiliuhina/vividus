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
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebElement;
import org.vividus.mobileapp.action.NetworkActions.Mode;
import org.vividus.mobileapp.action.NetworkActions.State;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import io.appium.java_client.ios.IOSDriver;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class NetworkConditionsActionsTests
{
    private static final String VALUE = "value";
    private static final String XCUIELEMENT_TYPE_SWITCH = "//XCUIElementTypeSwitch";
    private final TestLogger logger = TestLoggerFactory.getTestLogger(NetworkActions.class);
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @InjectMocks private NetworkActions networkActions;

    private static Stream<Arguments> dataProviderForAndroid()
    {
        return Stream.of(Arguments.of(Mode.DISABLE, State.WIFI), Arguments.of(Mode.ENABLE, State.WIFI),
                Arguments.of(Mode.DISABLE, State.DATA), Arguments.of(Mode.ENABLE, State.DATA),
                Arguments.of(Mode.DISABLE, State.AIRPLANE_MODE), Arguments.of(Mode.ENABLE, State.AIRPLANE_MODE),
                Arguments.of(Mode.DISABLE, State.ALL), Arguments.of(Mode.ENABLE, State.ALL));
    }

    @ParameterizedTest
    @MethodSource("dataProviderForAndroid")
    void testChangeConnectionStateBuilderForAndroid(
            Mode mode, State state)
    {
        AndroidDriver driver = mock(AndroidDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.getUnwrapped(AndroidDriver.class)).thenReturn(driver);
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        networkActions.changeNetworkConnectionState(mode, state);
        verify(driver).setConnection(any(ConnectionState.class));
    }

    @ParameterizedTest
    @CsvSource({ "ENABLE, DATA,'1'", "DISABLE, DATA,'0'", "ENABLE, WIFI,'1'", "DISABLE, WIFI,'0'" })
    void testChangeConnectionStateBuilderForIOS(Mode mode, State state, String toggleState)
    {
        WebElement switchBtn = changeNetworkConnection(mode, state, toggleState);
        verify(switchBtn).click();
    }

    @ParameterizedTest
    @CsvSource({ "DISABLE, DATA, '1'", "ENABLE, DATA, '0'", "DISABLE, WIFI, '1'", "ENABLE, WIFI, '0'" })
    void testAlreadyModifiedWiFiToggleForIOS(Mode mode, State state, String toggleState)
    {
        changeNetworkConnection(mode, state, toggleState);
        assertThat(logger.getLoggingEvents(), is(List.of(info("{} is already {}.", state, mode))));
    }

    private WebElement changeNetworkConnection(Mode mode, State state, String toggleState)
    {
        IOSDriver driver = mock(IOSDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        WebElement wiFiElement = mock(WebElement.class);
        WebElement switchBtn = mock(WebElement.class);
        when(webDriverProvider.getUnwrapped(IOSDriver.class)).thenReturn(driver);
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(driver.findElementById(state.getId())).thenReturn(wiFiElement);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_SWITCH)).thenReturn(switchBtn);
        when(switchBtn.getAttribute(VALUE)).thenReturn(toggleState);
        networkActions.changeNetworkConnectionState(mode, state);
        return switchBtn;
    }
}
