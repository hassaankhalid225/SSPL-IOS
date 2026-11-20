package com.sspl.ui.members

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.core.models.DirectoryMemberRecord
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SkeletonVariant
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatTile
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.LoadMoreFooter
import com.sspl.ui.components.PageIntroHero
import org.koin.compose.koinInject

private fun formatCount(n: Int): String {
    val s = n.toString()
    return s.reversed().chunked(3).joinToString(",").reversed()
}

@Composable
fun MembersDirectoryScreen(
    navController: NavController,
    publicRepository: PublicRepository = koinInject()
) {
    val viewModel: MembersDirectoryViewModel = viewModel { MembersDirectoryViewModel(publicRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats = state.statistics
    val listState = rememberLazyListState()

    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
        }
    }
    LaunchedEffect(nearEnd, state.canLoadMore) {
        if (nearEnd && state.canLoadMore && state.loadMoreError == null) viewModel.loadMore()
    }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "hero") {
                val total = stats?.let { it.lifeMembers + it.regularMembers + it.deceasedMembers }
                PageIntroHero(
                    eyebrow = "Members directory",
                    eyebrowIcon = SsplIcons.Groups,
                    title = "Our surgical community",
                    summary = "Search SSP Lahore members by name, membership number, or PMDC.",
                    stats = if (stats != null) listOf(
                        formatCount(total ?: 0) to "Members",
                        formatCount(stats.lifeMembers) to "Life",
                        formatCount(stats.regularMembers) to "Regular"
                    ) else emptyList()
                )
            }
            if (stats != null) {
                item(key = "stats") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            value = formatCount(stats.numberOfSpeakers),
                            label = "Speakers",
                            icon = SsplIcons.WorkspacePremium,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            value = formatCount(stats.deceasedMembers),
                            label = "Deceased",
                            icon = SsplIcons.Badge,
                            tint = AppTheme.colors.muted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item(key = "search") {
                SearchBar(
                    query = state.query,
                    onQueryChange = viewModel::onQuery,
                    placeholder = "Name, membership no, or PMDC",
                    onClear = { viewModel.onQuery("") }
                )
            }
            item(key = "filters") {
                SegmentedTabs(
                    options = MemberFilter.entries.map { if (it == MemberFilter.ALL) "All" else it.label },
                    selectedIndex = state.filter.ordinal,
                    onSelect = { viewModel.onFilter(MemberFilter.entries[it]) }
                )
            }

            when (val s = state.members) {
                is LoadState.Loading -> item(key = "skeleton") {
                    SkeletonList(count = 6, variant = SkeletonVariant.AVATAR_LIST, contentPadding = 0.dp)
                }
                is LoadState.Error -> item(key = "error") {
                    ErrorState(title = "Members didn't load", message = s.message, onRetry = viewModel::retry)
                }
                is LoadState.Ready -> {
                    item(key = "count") {
                        Text(
                            "Showing ${s.data.size} of ${formatCount(state.totalRecords)}",
                            style = AppTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                    if (s.data.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                title = "No members found",
                                message = "Adjust your search or filters to see more members.",
                                icon = if (state.query.isNotBlank()) SsplIcons.SearchOff else SsplIcons.PersonSearch,
                                actionText = if (state.query.isNotBlank() || state.filter != MemberFilter.ALL) "Clear filters" else null,
                                onActionClick = { viewModel.onQuery(""); viewModel.onFilter(MemberFilter.ALL) }
                            )
                        }
                    } else {
                        items(s.data, key = { "m_" + it.id }) { m -> MemberRow(m) }
                        if (state.loadingMore || state.loadMoreError != null) {
                            item(key = "more") {
                                LoadMoreFooter(
                                    loading = state.loadingMore,
                                    error = state.loadMoreError,
                                    onRetry = viewModel::loadMore,
                                    circleAvatar = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(m: DirectoryMemberRecord) {
    val user = m.account?.user
    val name = user?.fullName?.takeIf { it != "Member" } ?: "Unknown member"
    val role = m.role.orEmpty()
    val (label, variant) = when (role) {
        "DECEASED_MEMBER" -> "Deceased" to BadgeVariant.NEUTRAL
        "LIFE_MEMBER" -> "Life member" to BadgeVariant.SUCCESS
        else -> "Regular" to BadgeVariant.PRIMARY
    }
    val memNo = m.account?.memNo?.takeIf { it.isNotBlank() } ?: "N/A"
    val details = listOfNotNull(user?.designation, user?.institute, user?.city).filter { it.isNotBlank() }.joinToString(" · ")
    ListRowCard(
        title = name,
        subtitle = "Membership no: $memNo",
        meta = listOfNotNull(
            user?.pmdcNumber?.takeIf { it.isNotBlank() }?.let { "PMDC $it" },
            details.takeIf { it.isNotBlank() }
        ).joinToString(" · ").ifBlank { null },
        leading = { AvatarImage(url = user?.profilePicture, name = name.removePrefix("Dr.").removePrefix("Mr").trim(), size = 46.dp) },
        trailing = { StatusBadge(label, variant = variant) }
    )
}
