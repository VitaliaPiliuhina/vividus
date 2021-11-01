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

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.mobileapp.action.NetworkActions.Mode;
import org.vividus.mobileapp.action.NetworkActions.State;
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
    @MethodSource("dataProvider")
    void shouldFailForEnableNetworkConnectionForNotIOS(Mode mode, State state)
    {
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> networkConditionsSteps.changeNetworkConnection(mode, state));
        assertEquals(String.format("%s is not supported for IOS", state), iae.getMessage());
    }

    @ParameterizedTest
    @MethodSource("dataProviderForIOS")
    void shouldEnableNetworkConnectionForIOS(Mode mode, State state)
    {
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        networkConditionsSteps.changeNetworkConnection(mode, state);
        verify(networkActions).changeNetworkConnectionState(mode, state);
    }

    @ParameterizedTest
    @MethodSource("dataProviderForAndroid")
    void shouldEnableNetworkConnectionForAndroid(Mode mode, State state)
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        networkConditionsSteps.changeNetworkConnection(mode, state);
        verify(networkActions).changeNetworkConnectionState(mode, state);
    }

    private static Stream<Arguments> dataProviderForAndroid()
    {
        return Stream.of(Arguments.of(Mode.DISABLE, State.WIFI), Arguments.of(Mode.ENABLE, State.WIFI),
                Arguments.of(Mode.DISABLE, State.DATA), Arguments.of(Mode.ENABLE, State.DATA),
                Arguments.of(Mode.DISABLE, State.AIRPLANE_MODE), Arguments.of(Mode.ENABLE, State.AIRPLANE_MODE),
                Arguments.of(Mode.DISABLE, State.ALL), Arguments.of(Mode.ENABLE, State.ALL));
    }

    private static Stream<Arguments> dataProviderForIOS()
    {
        return Stream.of(Arguments.of(Mode.DISABLE, State.WIFI), Arguments.of(Mode.ENABLE, State.WIFI),
                Arguments.of(Mode.DISABLE, State.DATA), Arguments.of(Mode.ENABLE, State.DATA));
    }

    private static Stream<Arguments> dataProvider()
    {
        return Stream.of(Arguments.of(Mode.DISABLE, State.ALL), Arguments.of(Mode.ENABLE, State.ALL),
                Arguments.of(Mode.DISABLE, State.AIRPLANE_MODE), Arguments.of(Mode.ENABLE, State.AIRPLANE_MODE));
    }
}
