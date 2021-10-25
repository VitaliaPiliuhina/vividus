package org.vividus.mobileapp.action;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import io.appium.java_client.ios.IOSDriver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;

import java.util.List;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
public class NetworkConditionsActionsTest
{
    private static final String VALUE = "value";
    private static final String XCUIELEMENT_TYPE_CELL = "//XCUIElementTypeCell[@name='Wi-Fi']";
    private static final String XCUIELEMENT_TYPE_SWITCH = "//XCUIElementTypeSwitch";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @InjectMocks private NetworkActions networkActions;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(NetworkActions.class);

    @ParameterizedTest
    @EnumSource(value = NetworkActions.State.class, names = {"ON", "OFF"})
    void testConnectionStateBuilder(NetworkActions.State state)
    {
        AndroidDriver driver = mock(AndroidDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.getUnwrapped(AndroidDriver.class)).thenReturn(driver);
        networkActions.changeNetworkConnection(state);
        verify(driver).setConnection(any(ConnectionState.class));
    }

    @ParameterizedTest
    @EnumSource(value = NetworkActions.State.class, names = {"ON", "OFF"})
    void testWiFiToggleForAndroid()
    {
        ConnectionState state = mock(ConnectionState.class);
        AndroidDriver driver = mock(AndroidDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.getUnwrapped(AndroidDriver.class)).thenReturn(driver);
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        when(driver.getConnection()).thenReturn(state);
        networkActions.switchWiFi(NetworkActions.State.ON);
        verify(driver).toggleWifi();
    }

    @ParameterizedTest
    @CsvSource({
            "'1', ON",
            "'0', OFF"
    })
    void testWiFiToggleForIOS(String toggleState, NetworkActions.State state)
    {
        IOSDriver driver = mock(IOSDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        MobileElement wiFiElement = mock(MobileElement.class);
        MobileElement switchBtn = mock(MobileElement.class);
        when(webDriverProvider.getUnwrapped(IOSDriver.class)).thenReturn(driver);
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_CELL)).thenReturn(wiFiElement);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_SWITCH)).thenReturn(switchBtn);
        when(switchBtn.getAttribute(VALUE)).thenReturn(toggleState);
        networkActions.switchWiFi(state);
        verify(switchBtn).click();
    }

    @ParameterizedTest
    @CsvSource({
            "'0', ON",
            "'1', OFF"
    })
    void testAlreadyModifiedWiFiToggleForIOS(String toggleState, NetworkActions.State state)
    {
        IOSDriver driver = mock(IOSDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        MobileElement wiFiElement = mock(MobileElement.class);
        MobileElement switchBtn = mock(MobileElement.class);
        when(webDriverProvider.getUnwrapped(IOSDriver.class)).thenReturn(driver);
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_CELL)).thenReturn(wiFiElement);
        when(driver.findElementByXPath(XCUIELEMENT_TYPE_SWITCH)).thenReturn(switchBtn);
        when(switchBtn.getAttribute(VALUE)).thenReturn(toggleState);
        networkActions.switchWiFi(state);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Wi-Fi is already {}.", state.toString()))));
    }
}
