package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.R
import kotlinx.coroutines.delay

/**
 * Mise à jour 095 — analyseur Markdown maison, sans dépendance externe :
 * titres, gras, italique, code en ligne, listes, liens et blocs de code.
 * L'interface affiche ces blocs ; « Copier » confirme visuellement 1,5 s ;
 * les liens passent par un aperçu dans l'application avant d'en sortir.
 */

data class MdRun(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: String? = null
)

sealed class MdBlock {
    data class MdHeading(val level: Int, val runs: List<MdRun>) : MdBlock()
    data class MdParagraph(val runs: List<MdRun>) : MdBlock()
    data class MdBulletList(val items: List<List<MdRun>>) : MdBlock()
    data class MdCodeBlock(val language: String, val code: String) : MdBlock()
}

private val INLINE_REGEX = Regex(
    "\\[([^\\]]+)\\]\\((https?://[^)\\s]+)\\)" + // [texte](lien)
        "|\\*\\*(.+?)\\*\\*" +                     // **gras**
        "|\\*([^*\\n]+?)\\*" +                     // *italique*
        "|`([^`\\n]+)`" +                          // `code`
        "|(https?://[^\\s<>()\"]+)"                // URL nue
)

/** Découpe une ligne en segments stylés (gras, italique, code, lien). */
internal fun parseInline(line: String): List<MdRun> {
    val runs = ArrayList<MdRun>()
    var cursor = 0
    for (match in INLINE_REGEX.findAll(line)) {
        if (match.range.first > cursor) {
            runs += MdRun(line.substring(cursor, match.range.first))
        }
        val (text, bold, italic, code, link) = when {
            match.groupValues[2].isNotEmpty() ->
                Quint(match.groupValues[1], link = match.groupValues[2])
            match.groupValues[3].isNotEmpty() -> Quint(match.groupValues[3], bold = true)
            match.groupValues[4].isNotEmpty() -> Quint(match.groupValues[4], italic = true)
            match.groupValues[5].isNotEmpty() -> Quint(match.groupValues[5], code = true)
            else -> Quint(match.value, link = match.value)
        }
        runs += MdRun(text, bold, italic, code, link)
        cursor = match.range.last + 1
    }
    if (cursor < line.length) runs += MdRun(line.substring(cursor))
    return runs.ifEmpty { listOf(MdRun(line)) }
}

private data class Quint(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: String? = null
)

/** Analyse un texte Markdown complet en blocs affichables. */
fun parseMarkdown(text: String): List<MdBlock> {
    val blocks = ArrayList<MdBlock>()
    val lines = text.lines()
    var paragraph = ArrayList<String>()
    var bullets = ArrayList<List<MdRun>>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks += MdBlock.MdParagraph(parseInline(paragraph.joinToString(" ").trim()))
            paragraph = ArrayList()
        }
    }

    fun flushBullets() {
        if (bullets.isNotEmpty()) {
            blocks += MdBlock.MdBulletList(bullets)
            bullets = ArrayList()
        }
    }

    var index = 0
    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()
        when {
            trimmed.startsWith("```") -> {
                flushParagraph()
                flushBullets()
                val language = trimmed.removePrefix("```").trim()
                val code = ArrayList<String>()
                index++
                while (index < lines.size && !lines[index].trim().startsWith("```")) {
                    code += lines[index]
                    index++
                }
                index++ // saute la clôture ```
                blocks += MdBlock.MdCodeBlock(language, code.joinToString("\n"))
            }
            trimmed.matches(Regex("#{1,3}\\s+.+")) -> {
                flushParagraph()
                flushBullets()
                val level = trimmed.takeWhile { it == '#' }.length
                blocks += MdBlock.MdHeading(level, parseInline(trimmed.dropWhile { it == '#' }.trim()))
                index++
            }
            trimmed.matches(Regex("[-*•]\\s+.+")) -> {
                flushParagraph()
                bullets += parseInline(trimmed.substring(2).trim())
                index++
            }
            trimmed.matches(Regex("\\d+[.)]\\s+.+")) -> {
                flushParagraph()
                bullets += parseInline(trimmed.substringAfter(' ').trim())
                index++
            }
            trimmed.isBlank() -> {
                flushParagraph()
                flushBullets()
                index++
            }
            else -> {
                flushBullets()
                paragraph += trimmed
                index++
            }
        }
    }
    flushParagraph()
    flushBullets()
    return blocks
}

/** Mise à jour 098 — texte lisible pour « Lire à voix haute » (sans syntaxe). */
fun markdownToPlainText(text: String): String =
    parseMarkdown(text).joinToString("\n") { block ->
        when (block) {
            is MdBlock.MdHeading -> block.runs.joinToString("") { it.text }
            is MdBlock.MdParagraph -> block.runs.joinToString("") { it.text }
            is MdBlock.MdBulletList -> block.items.joinToString("\n") { item ->
                "– " + item.joinToString("") { it.text }
            }
            is MdBlock.MdCodeBlock -> block.code
        }
    }

private const val URL_TAG = "md_url"

private fun List<MdRun>.toAnnotatedString(
    baseColor: Color,
    primaryColor: Color
): AnnotatedString = buildAnnotatedString {
    for (run in this@toAnnotatedString) {
        val style = SpanStyle(color = baseColor).let { base ->
            var s = base
            if (run.bold) s = s.copy(fontWeight = FontWeight.Bold)
            if (run.italic) s = s.copy(fontStyle = FontStyle.Italic)
            if (run.code) {
                s = s.copy(fontFamily = FontFamily.Monospace, background = primaryColor.copy(alpha = 0.12f))
            }
            if (run.link != null) {
                s = s.copy(color = primaryColor, textDecoration = TextDecoration.Underline)
            }
            s
        }
        if (run.link != null) {
            pushStringAnnotation(tag = URL_TAG, annotation = run.link)
            pushStyle(style)
            append(run.text)
            pop()
            pop()
        } else {
            pushStyle(style)
            append(run.text)
            pop()
        }
    }
}

/** Annotation URL + détection de tap, sans API expérimentale. */
internal fun AnnotatedString.linkAt(offset: Int): String? =
    getStringAnnotations(URL_TAG, offset, offset).firstOrNull()?.item

@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit = {}
) {
    val blocks = remember(markdown) { parseMarkdown(markdown) }
    val bodyColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.MdHeading -> Text(
                    text = block.runs.toAnnotatedString(bodyColor, primaryColor),
                    style = if (block.level <= 1) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.SemiBold
                )
                is MdBlock.MdParagraph -> MdParagraphText(
                    runs = block.runs,
                    bodyColor = bodyColor,
                    primaryColor = primaryColor,
                    onLinkClick = onLinkClick
                )
                is MdBlock.MdBulletList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.items.forEach { item ->
                        Row {
                            Text(
                                text = "•",
                                color = primaryColor,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            MdParagraphText(
                                runs = item,
                                bodyColor = bodyColor,
                                primaryColor = primaryColor,
                                onLinkClick = onLinkClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is MdBlock.MdCodeBlock -> MdCodeBlockView(block)
            }
        }
    }
}

@Composable
private fun MdParagraphText(
    runs: List<MdRun>,
    bodyColor: Color,
    primaryColor: Color,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val annotated = remember(runs, bodyColor, primaryColor) {
        runs.toAnnotatedString(bodyColor, primaryColor)
    }
    var layoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
            // Interligne 1,5 (mise à jour 091) — 16 sp × 1,5 = 24 sp.
            lineHeight = 24.sp
        ),
        onTextLayout = { layoutResult = it },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(annotated) {
                detectTapGestures { position ->
                    layoutResult?.getOffsetForPosition(position)?.let { offset ->
                        annotated.linkAt(offset)?.let(onLinkClick)
                    }
                }
            }
    )
}

@Composable
private fun MdCodeBlockView(block: MdBlock.MdCodeBlock) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500) // confirmation visuelle de 1,5 s (mise à jour 095)
            copied = false
        }
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = block.language.ifBlank { "code" },
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(block.code))
                    copied = true
                }) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = stringResource(
                            if (copied) R.string.chat_code_copied else R.string.chat_code_copy
                        ),
                        modifier = Modifier.padding(end = 4.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(if (copied) R.string.chat_code_copied else R.string.chat_code_copy),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Text(
                text = block.code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
            )
        }
    }
}
