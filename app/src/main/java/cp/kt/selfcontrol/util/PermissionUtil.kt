package cp.kt.selfcontrol.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment


object PermissionUtil {
    const val ACCESSIBILITY_CODE = 100
    const val OVERLAY_CODE = 101
    const val USAGE_ACCESS_CODE = 102

    // 判断当前应用的悬浮窗权限是否开启
    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    // 判断当前应用的辅助功能服务是否开启
    fun canAccessibility(context: Context): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        )
        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return services.toLowerCase().contains(context.packageName.toLowerCase())
        }
        return false
    }

    fun canUsageAccess(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )

            if (mode == AppOpsManager.MODE_DEFAULT) {
                context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            } else {
                mode == AppOpsManager.MODE_ALLOWED
            }
        } catch (e: PackageManager.NameNotFoundException) {
            true
        }
    }

    // 打开系统无障碍权限设置
    fun requestAccessibilityPermission(fragment: Fragment, requestCode: Int = ACCESSIBILITY_CODE) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        fragment.startActivityForResult(intent, requestCode)
    }

    // 打开系统悬浮窗权限设置
    fun requestOverlayPermission(fragment: Fragment, requestCode: Int = OVERLAY_CODE) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:" + fragment.context?.packageName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        fragment.startActivityForResult(intent, requestCode)
    }

    // 打开查看应用使用情况权限设置
    fun requestUsageAccessPermission(fragment: Fragment, requestCode: Int = USAGE_ACCESS_CODE) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        fragment.startActivityForResult(intent, requestCode)
    }
}