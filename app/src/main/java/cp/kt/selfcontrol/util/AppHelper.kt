package cp.kt.selfcontrol.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class AppHelper {
    companion object {
        private var instance: AppHelper? = null

        fun getInstance(context: Context): AppHelper {
            return instance ?: synchronized(this) {
                instance ?: build(context).also { AppHelper.instance = it }
            }
        }

        private fun build(context: Context): AppHelper {
            val app = AppHelper()
            app.allApps =
                context.packageManager.getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES)
            return app
        }
    }

    lateinit var allApps: List<ApplicationInfo>

    val systemApps: List<ApplicationInfo>
        get() {
            var systemApps: MutableList<ApplicationInfo> = mutableListOf()
            for (apk in allApps) {
                if (apk.flags.and(ApplicationInfo.FLAG_SYSTEM) != 0) {
                    systemApps.add(apk)
                }
            }
            return systemApps
        }

    val userApps: List<ApplicationInfo>
        get() {
            var userApps: MutableList<ApplicationInfo> = mutableListOf()
            for (apk in allApps) {
                if (apk.flags.and(ApplicationInfo.FLAG_SYSTEM) == 0) {
                    userApps.add(apk)
                }
            }
            return userApps
        }

    fun getApp(packageName: String): ApplicationInfo? {
        for (apk in allApps) {
            if (apk.processName == packageName) {
                return apk
            }
        }
        return null
    }
}