/*
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: Apache-2.0
 */

package org.calyxos.datura.utils

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.toBitmap
import org.calyxos.datura.models.App

object CommonUtils {

    fun getAllPackages(context: Context): List<App> {
        val applicationList = mutableListOf<App>()
        val packageManager = context.packageManager

        val packageList = packageManager.getInstalledPackages(
            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
        ).filter { it.requestedPermissions?.contains(Manifest.permission.INTERNET) == true }

        packageList.forEach { packageInfo ->
            val app = App(
                packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                packageInfo.packageName,
                packageInfo.applicationInfo.loadIcon(packageManager).toBitmap(96, 96),
                packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            )
            applicationList.add(app)
        }
        applicationList.sortBy { it.name }
        return applicationList.toList()
    }
}
