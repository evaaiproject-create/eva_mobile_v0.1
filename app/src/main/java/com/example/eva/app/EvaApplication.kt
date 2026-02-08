package com.example.eva.app

import android.app.Application

/*
================================================================================
EVA APPLICATION - APPLICATION CLASS
================================================================================

PURPOSE:
    This is the Application class for the Eva app.
    It's created once when the app starts and lives for the entire app lifecycle.

WHAT IS AN APPLICATION CLASS?
    - Created before any Activity
    - Lives as long as the app is running
    - Good place for app-wide initialization
    - Access via (context.applicationContext as EvaApplication)

CURRENT USAGE:
    Currently minimal, but can be extended to:
    - Initialize crash reporting (e.g., Firebase Crashlytics)
    - Set up dependency injection (e.g., Hilt, Koin)
    - Configure logging
    - Initialize analytics

NOTE:
    This class is referenced in AndroidManifest.xml:
    android:name=".app.EvaApplication"

================================================================================
*/

class EvaApplication : Application() {

    /*
    ================================================================================
    LIFECYCLE
    ================================================================================
    */

    /**
     * Called when the application is starting.
     *
     * This is called before any activity, service, or receiver objects
     * have been created.
     *
     * WHAT TO DO HERE:
     *     - Initialize libraries that need early setup
     *     - Set up crash reporting
     *     - Configure logging
     *     - DO NOT do heavy work (delays app startup)
     */
    override fun onCreate() {
        super.onCreate()

        // Store application instance for global access if needed
        instance = this

        // Initialize any app-wide components here
        // Example: initializeCrashReporting()
        // Example: initializeAnalytics()
    }

    /*
    ================================================================================
    COMPANION OBJECT
    ================================================================================
    */

    companion object {
        /**
         * Global application instance.
         *
         * Allows access to application context from anywhere.
         *
         * USAGE:
         *     val context = EvaApplication.instance
         *
         * WARNING: Use sparingly! Prefer passing context through constructors.
         */
        lateinit var instance: EvaApplication
            private set
    }
}
