package cp.kt.selfcontrol.data

import android.graphics.drawable.Drawable
import com.chad.library.adapter.base.entity.SectionEntity

class AppEntity(
    var header: String?,
    var appIcon: Drawable?,
    var appName: String?,
    var packageName: String?,
    var isInAllowList: Boolean = false
) : SectionEntity {
    override val isHeader: Boolean
        get() = header?.isNotBlank() ?: false
}
