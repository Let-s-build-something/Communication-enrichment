package base.navigation

import kotlinx.serialization.Serializable

/** Main holder of all navigation nodes */
sealed class NavigationNode {

    /**
     * Equivalent of route within the navigation.
     * Can be used to check whether current destination matches a specific Node.
     */
    val route
        get() = this::class.qualifiedName

    /**
     * Deeplink is the appendix of a brand link, which is shared between all client apps.
     * Null if deeplink isn't supported
     */
    abstract val deepLink: String?

    /** screen for both login and signup */
    @Serializable
    data object Login: NavigationNode() {
        override val deepLink: String = "/login"
    }

    /** Easter-egg screen, just because */
    @Serializable
    data object Water: NavigationNode() {
        override val deepLink: String? = null
    }

    /** dashboard screen for user information and general user-related actions */
    @Serializable
    data object AccountDashboard: NavigationNode() {
        override val deepLink: String = "/account-dashboard"
    }

    /** home screen of the whole app */
    @Serializable
    data object Home: NavigationNode() {
        override val deepLink: String = "/"
    }
}