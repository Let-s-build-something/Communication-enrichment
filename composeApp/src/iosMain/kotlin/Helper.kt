
import cocoapods.FirebaseCore.FIRApp
import cocoapods.FirebaseMessaging.FIRMessaging
import cocoapods.FirebaseMessaging.FIRMessagingDelegateProtocol
import cocoapods.GoogleSignIn.GIDConfiguration
import cocoapods.GoogleSignIn.GIDSignIn
import koin.commonModule
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.context.startKoin
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationOptions
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

/**
 * initializes iOS app this includes:
 * koin
 */
fun doAppInit() {
    startKoin {
        modules(commonModule)
    }
}

/**
 * Callback when iOS application has finished launching
 * Space for additional "lazy" initialization
 */
@OptIn(ExperimentalForeignApi::class)
fun <T> T.onIOSApplication(
    application: UIApplication,
    launchOptions: Map<Any?, *>?
): Boolean where T: UNUserNotificationCenterDelegateProtocol,
                 T: FIRMessagingDelegateProtocol,
                 T: UIApplicationDelegateProtocol {
    val authOptions: UNAuthorizationOptions = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
    UNUserNotificationCenter.currentNotificationCenter().run {
        delegate = this@onIOSApplication
        requestAuthorizationWithOptions(
            options = authOptions,
            completionHandler = { _, _ -> }
        )
    }
    FIRMessaging.messaging().delegate = this
    application.registerForRemoteNotifications()

    return configureFirebase()
}

/**
 *
 */
@OptIn(ExperimentalForeignApi::class)
suspend fun <T> T.onNewNotificationRequest(
    center: UNUserNotificationCenter,
    notification: UNNotification
) where T: UIApplicationDelegateProtocol,
        T: FIRMessagingDelegateProtocol {
    val content = notification.request.content
    val data = content.userInfo

    //println("onNewNotificationRequest, data: $data")
    //configureNotification(center)
}

/**
 * This notification is called whenever a notification is received on the iOS side.
 * We then edit it accordingly and return it back to the iOS side.
 */
fun onNewNotificationReceived(
    request: UNNotificationRequest? = null,
    content: UNMutableNotificationContent?
): UNMutableNotificationContent? {
    // iOS notification interception here
    return content
}

@OptIn(ExperimentalForeignApi::class)
private fun configureFirebase(): Boolean {
    FIRApp.configure()

    GIDSignIn.sharedInstance.configuration = GIDConfiguration(
        clientID = FIRApp.defaultApp()?.options?.clientID ?: return false
    )
    return true
}