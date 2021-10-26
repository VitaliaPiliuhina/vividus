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
    @EnumSource(value = NetworkActions.State.class, names = { "ON", "OFF" })
    void shouldFailForChangeNetworkConnectionForNotAndroid(NetworkActions.State state)
    {
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> networkConditionsSteps.changeNetworkConnection(state));
        assertEquals("Turn On/Off the Network Connection is supported for Android only", iae.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = NetworkActions.State.class, names = { "ON", "OFF" })
    void shouldChangeNetworkConnection(NetworkActions.State state)
    {
        when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(true);
        networkConditionsSteps.changeNetworkConnection(state);
        verify(networkActions).changeNetworkConnection(state);
    }

    @ParameterizedTest
    @EnumSource(value = NetworkActions.State.class, names = { "ON", "OFF" })
    void shouldSwitchWiFi(NetworkActions.State state)
    {
        networkConditionsSteps.switchWiFi(state);
        verify(networkActions).switchWiFi(state);
    }
}
