package com.example.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos

/** Ligne « À suivre » : clé stable (id + occurrence) pour survivre aux doublons éventuels. */
private data class QueueRow(val key: String, val track: AudioTrackEntity, val queueIndex: Int)

private fun buildRows(queue: List<AudioTrackEntity>, currentIndex: Int): List<QueueRow> {
    if (queue.isEmpty()) return emptyList()
    val occurrences = HashMap<Long, Int>()
    val rows = ArrayList<QueueRow>()
    for (index in (currentIndex + 1)..queue.lastIndex) {
        val track = queue[index]
        val occurrence = occurrences[track.id] ?: 0
        occurrences[track.id] = occurrence + 1
        rows.add(QueueRow(key = "${track.id}#$occurrence", track = track, queueIndex = index))
    }
    return rows
}

/**
 * État du glisser-déposer. Les échanges se font sur une copie locale de la liste (réponse
 * immédiate, sans attendre le lecteur) ; le déplacement n'est transmis au lecteur qu'au dépôt.
 */
private class ReorderState(
    private val listState: LazyListState,
    val rows: SnapshotStateList<QueueRow>
) {
    var draggedKey by mutableStateOf<String?>(null)
        private set
    var dragOffset by mutableFloatStateOf(0f)

    fun start(key: String) {
        draggedKey = key
        dragOffset = 0f
    }

    fun clear() {
        draggedKey = null
        dragOffset = 0f
    }

    fun dragBy(delta: Float) {
        dragOffset += delta
        swapIfNeeded()
    }

    /** Échange la ligne déplacée avec sa voisine quand son centre dépasse celui de la voisine. */
    fun swapIfNeeded() {
        val key = draggedKey ?: return
        val from = rows.indexOfFirst { it.key == key }
        if (from < 0) return
        val visible = listState.layoutInfo.visibleItemsInfo
        val dragged = visible.firstOrNull { it.key == key } ?: return
        val center = dragged.offset + dragged.size / 2f + dragOffset
        val next = rows.getOrNull(from + 1)?.let { row -> visible.firstOrNull { it.key == row.key } }
        val previous = rows.getOrNull(from - 1)?.let { row -> visible.firstOrNull { it.key == row.key } }
        val topBefore = dragged.offset + dragOffset

        when {
            next != null && center > next.offset + next.size / 2f -> {
                rows.add(from + 1, rows.removeAt(from))
                dragOffset = topBefore - (dragged.offset + next.size)
            }
            previous != null && center < previous.offset + previous.size / 2f -> {
                rows.add(from - 1, rows.removeAt(from))
                dragOffset = topBefore - previous.offset
            }
        }
    }
}

/**
 * Mise à jour 027 : feuille plein écran « À suivre ».
 * - morceau en cours en tête ;
 * - liste réordonnable par glisser-déposer (poignée à droite) ;
 * - balayage pour retirer ;
 * - bouton « Vider la file » ;
 * - option « Lecture automatique » (réglage partagé avec Paramètres › Lecture).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    queue: List<AudioTrackEntity>,
    currentIndex: Int,
    isPlaying: Boolean,
    onPlayTrack: (AudioTrackEntity) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Lecture automatique : même réglage que Paramètres › Lecture (clé auto_play, vrai par défaut).
    val preferences = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val autoPlayFlow = remember(preferences) { preferences.autoPlay.catch { emit(true) } }
    val autoPlay by autoPlayFlow.collectAsStateWithLifecycle(initialValue = true)

    val safeCurrent = if (queue.isEmpty()) 0 else currentIndex.coerceIn(0, queue.lastIndex)
    val currentTrack = queue.getOrNull(safeCurrent)

    val baseRows = remember(queue, safeCurrent) { buildRows(queue, safeCurrent) }
    val rows = remember(baseRows) { mutableStateListOf<QueueRow>().apply { addAll(baseRows) } }
    val listState = rememberLazyListState()
    val reorder = remember(rows) { ReorderState(listState, rows) }

    // Défilement automatique quand la ligne glissée approche du bord de la liste.
    LaunchedEffect(reorder, reorder.draggedKey) {
        if (reorder.draggedKey == null) return@LaunchedEffect
        val edge = with(density) { 72.dp.toPx() }
        val step = with(density) { 10.dp.toPx() }
        while (isActive) {
            val info = listState.layoutInfo
            val dragged = info.visibleItemsInfo.firstOrNull { it.key == reorder.draggedKey }
            if (dragged != null) {
                val center = dragged.offset + dragged.size / 2f + reorder.dragOffset
                val delta = when {
                    center < info.viewportStartOffset + edge -> -step
                    center > info.viewportEndOffset - edge -> step
                    else -> 0f
                }
                if (delta != 0f) {
                    val consumed = listState.scrollBy(delta)
                    reorder.dragOffset += consumed
                    reorder.swapIfNeeded()
                }
            }
            withFrameNanos { }
        }
    }

    fun commitDrop(key: String) {
        val local = rows.indexOfFirst { it.key == key }
        val row = rows.getOrNull(local) ?: return
        val target = safeCurrent + 1 + local
        if (row.queueIndex != target) onMove(row.queueIndex, target)
    }

    fun moveRowBy(key: String, delta: Int) {
        val from = rows.indexOfFirst { it.key == key }
        val to = from + delta
        if (from < 0 || to !in rows.indices) return
        val row = rows[from]
        rows.add(to, rows.removeAt(from))
        onMove(row.queueIndex, safeCurrent + 1 + to)
    }

    val trackCountText = remember(rows.size) {
        context.resources.getQuantityString(R.plurals.queue_screen_track_count, rows.size, rows.size)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("queue_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // En-tête : fermer, titre, vider la file
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("queue_close_button")) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.queue_screen_close),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.queue_screen_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = trackCountText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = onClear,
                        enabled = rows.isNotEmpty(),
                        modifier = Modifier.testTag("queue_clear_button")
                    ) {
                        Text(text = stringResource(R.string.queue_screen_clear))
                    }
                }

                // Option « Lecture automatique »
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = autoPlay,
                                role = Role.Switch,
                                onValueChange = { enabled ->
                                    scope.launch {
                                        try {
                                            preferences.setAutoPlay(enabled)
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (_: Exception) {
                                            // Écriture impossible : le réglage garde sa valeur précédente.
                                        }
                                    }
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("queue_autoplay_toggle"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = stringResource(R.string.queue_screen_autoplay_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.queue_screen_autoplay_subtitle),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = autoPlay, onCheckedChange = null)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Morceau en cours, en tête
                    if (currentTrack != null) {
                        item(key = "current") {
                            CurrentTrackRow(track = currentTrack, isPlaying = isPlaying)
                        }
                    }

                    item(key = "up_next_header") {
                        Text(
                            text = stringResource(R.string.queue_screen_section_up_next),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp)
                        )
                    }

                    if (rows.isEmpty()) {
                        item(key = "empty") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = stringResource(R.string.queue_screen_empty_title),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                Text(
                                    text = stringResource(R.string.queue_screen_empty_body),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    items(rows, key = { it.key }) { row ->
                        val localIndex = rows.indexOfFirst { it.key == row.key }
                        UpNextRow(
                            row = row,
                            isDragged = reorder.draggedKey == row.key,
                            dragOffsetProvider = { reorder.dragOffset },
                            canMoveUp = localIndex > 0,
                            canMoveDown = localIndex in 0 until rows.lastIndex,
                            onPlay = { onPlayTrack(row.track) },
                            onRemove = {
                                rows.remove(row)
                                onRemove(row.queueIndex)
                            },
                            onMoveUp = { moveRowBy(row.key, -1) },
                            onMoveDown = { moveRowBy(row.key, 1) },
                            onDragStart = { reorder.start(row.key) },
                            onDrag = { reorder.dragBy(it) },
                            onDragEnd = {
                                reorder.draggedKey?.let { commitDrop(it) }
                                reorder.clear()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackCover(track: AudioTrackEntity, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!track.albumArtUri.isNullOrBlank()) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

@Composable
private fun CurrentTrackRow(track: AudioTrackEntity, isPlaying: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("queue_current_track")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrackCover(track = track, size = 52.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Pause,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.queue_screen_now_playing),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = track.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LazyItemScope.UpNextRow(
    row: QueueRow,
    isDragged: Boolean,
    dragOffsetProvider: () -> Float,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val track = row.track
    val removeLabel = stringResource(R.string.queue_screen_remove)
    val moveUpLabel = stringResource(R.string.queue_screen_move_up)
    val moveDownLabel = stringResource(R.string.queue_screen_move_down)
    val playLabel = stringResource(R.string.queue_screen_play_now)
    val handleLabel = stringResource(R.string.queue_screen_drag_handle, track.title)

    val currentOnRemove by rememberUpdatedState(onRemove)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.Settled) {
                false
            } else {
                currentOnRemove()
                true
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isDragged) Modifier.zIndex(1f) else Modifier.animateItem())
            .graphicsLayer {
                if (isDragged) {
                    translationY = dragOffsetProvider()
                    scaleX = 1.02f
                    scaleY = 1.02f
                    shadowElevation = 12.dp.toPx()
                }
            }
            .semantics {
                customActions = buildList {
                    add(CustomAccessibilityAction(removeLabel) { onRemove(); true })
                    if (canMoveUp) add(CustomAccessibilityAction(moveUpLabel) { onMoveUp(); true })
                    if (canMoveDown) add(CustomAccessibilityAction(moveDownLabel) { onMoveDown(); true })
                }
            }
            .testTag("queue_row"),
        enableDismissFromStartToEnd = !isDragged,
        enableDismissFromEndToStart = !isDragged,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                }
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        Surface(
            onClick = onPlay,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrackCover(track = track, size = 46.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist + " • " + track.formatDuration(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Poignée de déplacement, à droite
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .pointerInput(row.key) {
                            detectDragGestures(
                                onDragStart = { currentOnDragStart() },
                                onDrag = { change, amount ->
                                    change.consume()
                                    currentOnDrag(amount.y)
                                },
                                onDragEnd = { currentOnDragEnd() },
                                onDragCancel = { currentOnDragEnd() }
                            )
                        }
                        .testTag("queue_drag_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = handleLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
