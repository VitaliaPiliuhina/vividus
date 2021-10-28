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

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.mobileapp.action.NetworkActions.State;
import org.vividus.selenium.manager.GenericWebDriverManager;

public final class NetworkConditionsSteps
{
    private final NetworkActions networkActions;
    private final GenericWebDriverManager genericWebDriverManager;

    private NetworkConditionsSteps(GenericWebDriverManager genericWebDriverManager, NetworkActions networkActions)
    {
        this.genericWebDriverManager = genericWebDriverManager;
        this.networkActions = networkActions;
    }

    /**
     * Turn <b>direction</b> for device
     * @param state to be executed
     */
    @When("I enable '$state' state")
    public void enableNetworkConnection(State state)
    {
        Validate.isTrue(genericWebDriverManager.isAndroidNativeApp(),
                String.format("Enable %s is supported for Android only", state));
        networkActions.enableNetworkConnectionState(state);
    }

    /**
     * Turn <b>direction</b> for device
     * @param state to be executed
     */
    @When("I disable '$state' state")
    public void disableNetworkConnection(State state)
    {
        Validate.isTrue(genericWebDriverManager.isAndroidNativeApp(),
                String.format("Disable %s is supported for Android only", state));
        networkActions.disableNetworkConnectionState(state);
    }

    /**
     * Turn <b>direction</b> Wi-Fi
     * @param mode to be executed
     */
    @When("I `$mode` Wi-Fi for IOS")
    public void switchWiFi(String mode)
    {
        Validate.isTrue(genericWebDriverManager.isIOSNativeApp(), "This step is for IOS only");
        networkActions.switchWiFiForIOS(mode);
    }
}
