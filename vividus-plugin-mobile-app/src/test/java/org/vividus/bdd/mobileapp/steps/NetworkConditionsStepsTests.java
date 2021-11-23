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

package org.vividus.bdd.mobileapp.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.mobileapp.action.NetworkActions.Mode;
import org.vividus.mobileapp.action.NetworkActions.NetworkToggle;
import org.vividus.selenium.manager.GenericWebDriverManager;

@ExtendWith(MockitoExtension.class)
class NetworkConditionsStepsTests
{
    @Mock
    private GenericWebDriverManager genericWebDriverManager;
    @Mock
    private NetworkActions networkActions;
    @InjectMocks
    private NetworkConditionsSteps networkConditionsSteps;

    @ParameterizedTest
    @EnumSource(value = Mode.class, names = {"WIFI_AND_MOBILE_DATA", "AIRPLANE_MODE"})
    void shouldFailForEnableNetworkConnectionForNotIOS(Mode mode)
    {
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> networkConditionsSteps.changeNetworkConnection(NetworkToggle.OFF, mode));
        assertEquals(String.format("%s is not supported for iOS", mode), iae.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = Mode.class, names = {"WIFI", "MOBILE_DATA"})
    void shouldEnableNetworkConnectionForIOS(Mode mode)
    {
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        networkConditionsSteps.changeNetworkConnection(NetworkToggle.ON, mode);
        verify(networkActions).changeNetworkConnectionState(NetworkToggle.ON, mode);
    }

    @ParameterizedTest
    @EnumSource(Mode.class)
    void shouldEnableNetworkConnectionForAndroid(Mode mode)
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        networkConditionsSteps.changeNetworkConnection(NetworkToggle.OFF, mode);
        verify(networkActions).changeNetworkConnectionState(NetworkToggle.OFF, mode);
    }
}
