/*
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: Apache-2.0
 */

package org.calyxos.datura.applist

import android.net.NetworkPolicyManager
import android.net.NetworkPolicyManager.POLICY_REJECT_ALL
import android.net.NetworkPolicyManager.POLICY_REJECT_CELLULAR
import android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND
import android.net.NetworkPolicyManager.POLICY_REJECT_VPN
import android.net.NetworkPolicyManager.POLICY_REJECT_WIFI
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import org.calyxos.datura.R
import org.calyxos.datura.models.App
import javax.inject.Inject
import javax.inject.Singleton

class AppListRVAdapter @Inject constructor(
    appListDiffUtil: AppListDiffUtil,
    private val networkPolicyManager: NetworkPolicyManager
) : ListAdapter<App, AppListRVAdapter.ViewHolder>(appListDiffUtil) {

    private val TAG = AppListRVAdapter::class.java.simpleName

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    @Singleton
    class AppListDiffUtil @Inject constructor() : DiffUtil.ItemCallback<App>() {

        override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: App, newItem: App): Boolean {
            return when {
                oldItem.icon != newItem.icon -> false
                oldItem.name != newItem.name -> false
                oldItem.packageName != newItem.packageName -> false
                oldItem.systemApp != newItem.systemApp -> false
                oldItem.uid != newItem.uid -> false
                else -> true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_app_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getItem(position)
        val context = holder.view.context

        // Map of switches to their policy
        val mapOfViewAndPolicy = mapOf(
            R.id.mainSwitch to POLICY_REJECT_ALL,
            R.id.backgroundSwitch to POLICY_REJECT_METERED_BACKGROUND,
            R.id.wifiSwitch to POLICY_REJECT_WIFI,
            R.id.mobileSwitch to POLICY_REJECT_CELLULAR,
            R.id.vpnSwitch to POLICY_REJECT_VPN
        )

        holder.view.apply {
            findViewById<ImageView>(R.id.appIcon).background = app.icon.toDrawable(resources)
            findViewById<TextView>(R.id.appName).text = app.name
            findViewById<TextView>(R.id.appPkgName).text = app.packageName

            // Expand layout on root view click
            expandLayout(holder.view, app.isExpanded)
            setOnClickListener {
                if (it.isVisible && app.requestsInternetPermission) {
                    currentList.find { a -> a.packageName == app.packageName }?.isExpanded =
                        !app.isExpanded

                    expandLayout(holder.view, app.isExpanded)
                }
            }

            // Switches, Checked/0 == Allowed to connect to internet (default)
            mapOfViewAndPolicy.forEach { (viewID, policy) ->
                findViewById<MaterialSwitch>(viewID).apply {
                    setOnCheckedChangeListener(null)
                    isEnabled = app.requestsInternetPermission
                    isChecked =
                        (networkPolicyManager.getUidPolicy(app.uid) and policy) == 0 &&
                        app.requestsInternetPermission

                    setOnCheckedChangeListener { view, isChecked ->
                        if (view.isVisible) {
                            if (isChecked) {
                                networkPolicyManager.removeUidPolicy(app.uid, policy)
                            } else {
                                networkPolicyManager.addUidPolicy(app.uid, policy)
                            }

                            // Reflect appropriate settings status
                            updateSettingsMode(holder.view, mapOfViewAndPolicy.keys)
                        }
                    }
                }
            }

            updateSettingsMode(this, mapOfViewAndPolicy.keys)
        }
    }

    private fun expandLayout(rootView: View, expand: Boolean) {
        rootView.apply {
            val settingsMode = findViewById<TextView>(R.id.settingsMode)

            findViewById<LinearLayout>(R.id.expandLayout).apply {
                if (!expand) {
                    settingsMode.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_down,
                        0
                    )
                    this.visibility = View.GONE
                } else {
                    settingsMode.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_up,
                        0
                    )
                    this.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateSettingsMode(rootView: View, switches: Set<Int>) {
        rootView.apply {
            val settingsMode = findViewById<TextView>(R.id.settingsMode)

            if (switches.all { findViewById<MaterialSwitch>(it).isChecked }) {
                settingsMode.text = context.getString(R.string.default_settings)
            } else {
                settingsMode.text = context.getString(R.string.custom_settings)
            }
        }
    }
}
