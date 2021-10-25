package org.vividus.bdd.mobileapp.steps;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.selenium.manager.GenericWebDriverManager;

public class NetworkConditionsSteps
{
    private final NetworkActions networkActions;
    private final GenericWebDriverManager genericWebDriverManager;

    private NetworkConditionsSteps(
            GenericWebDriverManager genericWebDriverManager, NetworkActions networkActions)
    {
        this.genericWebDriverManager = genericWebDriverManager;
        this.networkActions = networkActions;
    }

    /**
     * Turn <b>direction</b> Wi-Fi and Mobile Network for device
     *
     * @param action to be executed
     */
    @When("I turn `$action` the Network Connection")
    public void changeNetworkConnection(NetworkActions.State action)
    {
        Validate.isTrue(genericWebDriverManager.isAndroidNativeApp(), "Turn On/Off the Network Connection is supported for Android only");
        networkActions.changeNetworkConnection(action);
    }

    /**
     * Turn <b>direction</b> Wi-Fi
     *
     * @param action to be executed
     */
    @When("I turn `$action` Wi-Fi")
    public void switchWiFi(NetworkActions.State action)
    {
        networkActions.switchWiFi(action);
    }
}
