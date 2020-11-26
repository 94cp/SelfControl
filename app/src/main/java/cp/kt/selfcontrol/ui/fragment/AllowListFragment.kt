package cp.kt.selfcontrol.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cp.kt.selfcontrol.R
import cp.kt.selfcontrol.adapter.AllowItemRecyclerViewAdapter
import cp.kt.selfcontrol.data.AppEntity
import cp.kt.selfcontrol.util.AppHelper
import cp.kt.selfcontrol.util.Constant
import cp.kt.selfcontrol.util.SPUtil

class AllowListFragment : Fragment() {
    private var allowApps: Set<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_allow_list, container, false)

        val dm = view.context.resources.displayMetrics
        val width = (dm.widthPixels / dm.density).toInt()
        val columnCount: Int = width / 112

        allowApps = SPUtil.getSet(Constant.SP.ALLOW_LIST)

        // Set the adapter
        if (view is RecyclerView) {
            val data: MutableList<AppEntity> = mutableListOf()

            data.add(AppEntity(getString(R.string.user_app), null, null, null, false))
            for (app in AppHelper.getInstance(view.context).userApps) {
                data.add(
                    AppEntity(
                        null,
                        app.loadIcon(view.context.packageManager),
                        app.loadLabel(view.context.packageManager).toString(),
                        app.processName,
                        allowApps?.contains(app.processName) ?: false
                    )
                )
            }

            data.add(AppEntity(getString(R.string.system_app), null, null, null, false))
            for (app in AppHelper.getInstance(view.context).systemApps) {
                data.add(
                    AppEntity(
                        null,
                        app.loadIcon(view.context.packageManager),
                        app.loadLabel(view.context.packageManager).toString(),
                        app.processName,
                        allowApps?.contains(app.processName) ?: false
                    )
                )
            }

            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                val allowItemAdapter = AllowItemRecyclerViewAdapter(
                    R.layout.fragment_header,
                    R.layout.fragment_allow_item,
                    data
                )

                allowItemAdapter.setOnItemClickListener { adapter, view, position ->
                    if (view.id != R.id.allowCheckBox) {
                        val checkBox = view.findViewById<CheckBox>(R.id.allowCheckBox)
                        val app = adapter.data[position] as AppEntity

                        allowApps?.let {
                            val set = it.toHashSet()

                            if (checkBox.isChecked) {
                                set.remove(app.packageName)
                            } else {
                                app.packageName?.let { it1 -> set.add(it1) }
                            }

                            SPUtil.saveSet(Constant.SP.ALLOW_LIST, set)
                            allowApps = SPUtil.getSet(Constant.SP.ALLOW_LIST)

                            checkBox.isChecked = !checkBox.isChecked
                        }
                    }
                }

                allowItemAdapter.setOnItemChildClickListener { adapter, view, position ->
                    if (view.id == R.id.allowCheckBox) {
                        val checkBox = view.findViewById<CheckBox>(R.id.allowCheckBox)
                        val app = adapter.data[position] as AppEntity

                        allowApps?.let {
                            val set = it.toHashSet()

                            if (!checkBox.isChecked) {
                                set.remove(app.packageName)
                            } else {
                                app.packageName?.let { it1 -> set.add(it1) }
                            }

                            SPUtil.saveSet(Constant.SP.ALLOW_LIST, set)
                            allowApps = SPUtil.getSet(Constant.SP.ALLOW_LIST)
                        }
                    }
                }

                adapter = allowItemAdapter
            }
        }
        return view
    }
}