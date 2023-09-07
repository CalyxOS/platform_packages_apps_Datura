/*
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: Apache-2.0
 */

package org.calyxos.datura.applist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.calyxos.datura.R
import org.calyxos.datura.main.MainActivityViewModel
import javax.inject.Inject

@AndroidEntryPoint(Fragment::class)
class AppListFragment : Hilt_AppListFragment(R.layout.fragment_app_list) {

    private val viewModel: MainActivityViewModel by activityViewModels()

    @Inject
    lateinit var appListRVAdapter: AppListRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recycler View
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.appList.collect { appListRVAdapter.submitList(it) }
        }
        view.findViewById<RecyclerView>(R.id.recyclerView).adapter = appListRVAdapter
    }
}
