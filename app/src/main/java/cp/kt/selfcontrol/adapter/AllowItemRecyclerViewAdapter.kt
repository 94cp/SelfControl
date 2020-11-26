package cp.kt.selfcontrol.adapter

import android.widget.CheckBox
import androidx.annotation.LayoutRes
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import cp.kt.selfcontrol.R
import cp.kt.selfcontrol.data.AppEntity


class AllowItemRecyclerViewAdapter(
    @LayoutRes sectionHeadResId: Int,
    @LayoutRes layoutResId: Int,
    data: MutableList<AppEntity>?
) : BaseSectionQuickAdapter<AppEntity, BaseViewHolder>(
    sectionHeadResId,
    layoutResId,
    data
) {

    init {
        addChildClickViewIds(R.id.allowCheckBox);
    }

    override fun convert(holder: BaseViewHolder, item: AppEntity) {
        holder.setImageDrawable(R.id.appIconImageView, item.appIcon)
        holder.setText(R.id.appNameTextView, item.appName)
        holder.getView<CheckBox>(R.id.allowCheckBox).isChecked = item.isInAllowList
    }

    override fun convertHeader(helper: BaseViewHolder, item: AppEntity) {
        helper.setText(R.id.headerTextView, item.header)
    }
}