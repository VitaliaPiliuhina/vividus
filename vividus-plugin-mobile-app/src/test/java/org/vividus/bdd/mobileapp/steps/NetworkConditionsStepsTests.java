package org.vividus.bdd.mobileapp.steps;

import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.selenium.manager.GenericWebDriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
public class NetworkConditionsStepsTests
{
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private NetworkActions networkActions;
    @InjectMocks private NetworkConditionsSteps networkConditionsSteps;

    @ParameterizedTest
    @EnumSource(value = NetworkActions.State.class, names = {"ON", "OFF"})
    void shouldFailForChangeNetworkConnectionForNotAndroid(NetworkActions.State state)
    {
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> networkConditionsSteps.changeNetworkConnection(state));
        assertEquals("Turn On/Off the Network Connection is supported for Android only", iae.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = NetworkActions.State.class, names = {"ON", "OFF"})
    void shouldChangeNetworkConnection(NetworkActions.State state)
    {
        when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(true);
        networkConditionsSteps.changeNetworkConnection(state);
        verify(networkActions).changeNetworkConnection(state);
    }

    @ParameterizedTest
    @EnumSource(value = NetworkActions.State.class, names = {"ON", "OFF"})
    void shouldSwitchWiFi(NetworkActions.State state)
    {
        networkConditionsSteps.switchWiFi(state);
        verify(networkActions).switchWiFi(state);
    }
}

