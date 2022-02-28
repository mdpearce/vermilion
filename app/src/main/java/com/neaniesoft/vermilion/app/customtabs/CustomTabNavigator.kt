package com.neaniesoft.vermilion.app.customtabs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.get
import com.neaniesoft.vermilion.app.R
import com.neaniesoft.vermilion.app.VermilionScreen
import com.neaniesoft.vermilion.utils.logger
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Clock

/**
 * This custom navigator routes to Custom Tabs for a given uri passed in as an argument.
 *
 * It is heavily inspired by https://medium.com/@chadschultz/using-chrome-custom-tabs-with-the-navigation-component-from-android-jetpack-187b53014793
 */
@Navigator.Name("custom_tab")
class CustomTabNavigator(
    private val context: Context,
    private val clock: Clock
) : Navigator<CustomTabNavigator.Destination>() {

    private val logger by logger()

    companion object {
        const val KEY_URI = "uri"
        private const val DEFAULT_PACKAGE = "com.android.chrome"
    }

    private var session: CustomTabsSession? = null

    private val urisInProgressStartTimes: MutableMap<Uri, Long> = mutableMapOf()

    private val throttleTimeoutMs: Long = 2000L

    val customTabsCallback by lazy {
        object : CustomTabsCallback() {
            override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                when (navigationEvent) {
                    NAVIGATION_ABORTED, NAVIGATION_FAILED, NAVIGATION_FINISHED -> {
                        with(urisInProgressStartTimes.entries) {
                            remove(first())
                        }
                    }
                }
            }
        }
    }

    private val upIconBitmap by lazy {
        requireNotNull(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_baseline_arrow_back_24
            )
        ).toBitmap()
    }

    override fun createDestination(): Destination = Destination(this)

    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        val uriString =
            requireNotNull(args?.getString(KEY_URI)) { "The navigation component should enforce that the uri is not null" }
        val uri = URLDecoder.decode(uriString, "utf-8").toUri()

        if (!shouldAllowLaunch(uri)) {
            return null
        }

        buildCustomTabsIntent(destination).launchUrl(context, uri)

        return null // don't add to back stack, custom tabs manages this
    }

    override fun popBackStack() = true // managed by custom tabs

    private fun buildCustomTabsIntent(destination: Destination): CustomTabsIntent =
        CustomTabsIntent.Builder().apply {
            val session = session
            if (session != null) {
                setSession(session)
            }

            setColorScheme(destination.colorScheme)
            val defaultColorSchemeParamsBuilder = CustomTabColorSchemeParams.Builder()
            if (destination.toolbarColor != 0) {
                val color = ContextCompat.getColor(context, destination.toolbarColor)
                defaultColorSchemeParamsBuilder.setToolbarColor(color)
            }
            if (destination.navigationBarColor != 0) {
                val color = ContextCompat.getColor(context, destination.navigationBarColor)
                defaultColorSchemeParamsBuilder.setNavigationBarColor(color)
            }
            setDefaultColorSchemeParams(defaultColorSchemeParamsBuilder.build())
            setStartAnimations(context, destination.enterAnim, 0)
            setExitAnimations(context, 0, destination.exitAnim)
            if (destination.upInsteadOfClose) {
                setCloseButtonIcon(upIconBitmap)
            }
            if (destination.addDefaultShareMenuItem) {
                setShareState(SHARE_STATE_ON)
            }
        }.build()
            .apply {
                intent.putExtra(
                    Intent.EXTRA_REFERRER,
                    Uri.parse("android-app://${context.packageName}")
                )
            }

    /**
     * Ensure we don't navigate to the same uri if the user double-taps quickly on a link
     */
    private fun shouldAllowLaunch(uri: Uri): Boolean {
        val tabStartTime = urisInProgressStartTimes[uri]
        if (tabStartTime != null) {
            if (clock.millis() - tabStartTime > throttleTimeoutMs) {
                logger.warnIfEnabled { "Throttle time exceeded" }
            } else {
                urisInProgressStartTimes.remove(uri)
                return false
            }
        }
        urisInProgressStartTimes[uri] = clock.millis()
        return true
    }

    fun warmUpBrowserInstance() {
        val connection = object : CustomTabsServiceConnection() {
            override fun onServiceDisconnected(name: ComponentName) {
            }

            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                client.warmup(0L)
                session = client.newSession(customTabsCallback)
            }
        }

        val packageName = CustomTabsClient.getPackageName(context, emptyList()) ?: DEFAULT_PACKAGE
        CustomTabsClient.bindCustomTabsService(context, packageName, connection)
    }

    @NavDestination.ClassType(Activity::class)
    class Destination(navigator: Navigator<out NavDestination>) : NavDestination(navigator) {

        var colorScheme: Int = 0

        @ColorRes
        var toolbarColor: Int = 0

        @ColorRes
        var navigationBarColor: Int = 0

        @AnimRes
        var enterAnim: Int = 0

        @AnimRes
        var exitAnim: Int = 0

        var upInsteadOfClose: Boolean = false

        var addDefaultShareMenuItem: Boolean = false

        @SuppressLint("MissingSuperCall")
        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)

            context.withStyledAttributes(attrs, R.styleable.CustomTabNavigator, 0, 0) {
                colorScheme = getInt(R.styleable.CustomTabNavigator_colorScheme, 0)
                toolbarColor = getResourceId(R.styleable.CustomTabNavigator_toolbarColor, 0)
                navigationBarColor =
                    getResourceId(R.styleable.CustomTabNavigator_navigationBarColor, 0)
                enterAnim = getResourceId(R.styleable.CustomTabNavigator_enterAnim, 0)
                exitAnim = getResourceId(R.styleable.CustomTabNavigator_exitAnim, 0)
                upInsteadOfClose =
                    getBoolean(R.styleable.CustomTabNavigator_upInsteadOfClose, false)
                addDefaultShareMenuItem =
                    getBoolean(R.styleable.CustomTabNavigator_addDefaultShareMenuItem, false)
            }
        }
    }
}

fun NavGraphBuilder.customTab(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList()
) {
    addDestination(
        CustomTabNavigator.Destination(provider[CustomTabNavigator::class].apply { warmUpBrowserInstance() })
            .apply {
                this.route = route
                arguments.forEach { (argName, argument) ->
                    addArgument(argName, argument)
                }
                deepLinks.forEach { deepLink ->
                    addDeepLink(deepLink)
                }
            }
    )
}

fun customTabRoute(uri: Uri): String =
    VermilionScreen.CustomTab.name + "/" + URLEncoder.encode(uri.toString(), "utf-8")
