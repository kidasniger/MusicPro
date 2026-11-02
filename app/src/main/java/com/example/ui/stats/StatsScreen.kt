package com.example.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(statsViewModel: StatsViewModel = viewModel()) {
    val records by statsViewModel.stats.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val week = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val weekEpochs = week.map { it.toEpochDay() }.toSet()
    val weekRecords = records.filter { it.dayEpoch in weekEpochs }
    val daily = week.associateWith { day ->
        weekRecords.filter { it.dayEpoch == day.toEpochDay() }.sumOf { it.listenedMs }
    }
    val totalWeek = daily.values.sum()
    val maxDaily = daily.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    val topTracks = records.groupBy { it.trackId }
        .map { (_, rows) -> rows.first().title to rows.sumOf { it.listenedMs } }
        .sortedByDescending { it.second }.take(5)
    val topArtists = records.groupBy { it.artist }
        .map { (name, rows) -> name to rows.sumOf { it.listenedMs } }
        .sortedByDescending { it.second }.take(5)
    val topGenres = records.filter { it.genre.isNotBlank() }
        .groupBy { it.genre }
        .map { (name, rows) -> name to rows.sumOf { it.listenedMs } }
        .sortedByDescending { it.second }.take(5)

    val activeDays = records.filter { it.listenedMs > 0L }.map { it.dayEpoch }.toSet()
    val currentAnchor = if (today.toEpochDay() in activeDays) today else today.minusDays(1)
    var currentStreak = 0
    while (currentAnchor.minusDays(currentStreak.toLong()).toEpochDay() in activeDays) currentStreak++
    val bestStreak = run {
        var best = 0
        var runLength = 0
        var previous: Long? = null
        activeDays.sorted().forEach { day ->
            runLength = if (previous != null && day == previous!! + 1L) runLength + 1 else 1
            best = maxOf(best, runLength)
            previous = day
        }
        best
    }

    val accent = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(stringResource(R.string.stats_title), color = textColor, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.stats_privacy), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        item {
            StatsCard(title = stringResource(R.string.stats_week)) {
                Text(formatDuration(totalWeek), color = textColor, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(116.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    week.forEach { day ->
                        val value = daily[day] ?: 0L
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((92f * value.toFloat() / maxDaily.toFloat()).coerceAtLeast(if (value > 0L) 5f else 3f).dp)
                                    .background(accent.copy(alpha = if (value > 0L) 1f else 0.16f), RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                            )
                            Spacer(Modifier.height(7.dp))
                            Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH).take(2), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        item {
            StatsCard(title = stringResource(R.string.stats_streak)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.stats_current_streak), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(stringResource(R.string.stats_days, currentStreak), color = textColor, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.stats_best_streak), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(stringResource(R.string.stats_days, bestStreak), color = textColor, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { StatsCard(title = stringResource(R.string.stats_top_tracks)) { RankingList(topTracks) } }
        item { StatsCard(title = stringResource(R.string.stats_top_artists)) { RankingList(topArtists) } }
        item {
            StatsCard(title = stringResource(R.string.stats_top_genres)) {
                if (topGenres.isEmpty()) {
                    Text(stringResource(R.string.stats_no_genre), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                } else {
                    RankingList(topGenres)
                }
            }
        }
        if (records.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.stats_empty),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun StatsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun RankingList(entries: List<Pair<String, Long>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        entries.forEachIndexed { index, entry ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text((index + 1).toString().padStart(2, '0'), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(entry.first, modifier = Modifier.weight(1f).padding(start = 12.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, maxLines = 1)
                Text(formatDuration(entry.second), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val minutes = (milliseconds / 60_000L).toInt()
    val hours = minutes / 60
    val remaining = minutes % 60
    return if (hours > 0) hours.toString() + " h " + remaining + " min" else minutes.toString() + " min"
}
