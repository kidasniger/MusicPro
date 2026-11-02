package com.example.ui.theme

import androidx.compose.ui.unit.Dp
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Mise à jour 015 : le build échoue si un fichier migré contient une valeur dp hors grille, ou si
 * un jeton de [MusicProTokens] sort de la grille. Grille : multiples de 4, plus 0, 1, 2 et 3 dp
 * (contours et élévations).
 */
class DesignTokensGridTest {

    /** Fichiers déjà migrés vers les jetons ; ajouter ici chaque écran migré. */
    private val migratedFiles = listOf(
        "com/example/ui/components/MusicProCard.kt",
        "com/example/ui/settings/AppearanceSettingsCard.kt",
        "com/example/ui/whatsnew/NewScreen.kt",
        "com/example/ui/artist/ArtistScreen.kt",
        "com/example/ui/settings/GlassSettingsScreen.kt"
    )

    private fun isOnGrid(value: Float): Boolean =
        value == 0f || value == 1f || value == 2f || value == 3f || value % 4f == 0f

    private fun sourceRoot(): File =
        listOf(File("src/main/java"), File("app/src/main/java")).first { it.isDirectory }

    @Test
    fun tokens_are_on_grid() {
        val dimensions: List<Dp> = MusicProTokens.Spacing.all +
            MusicProTokens.Radius.all +
            MusicProTokens.Elevation.all
        val offGrid = dimensions.filterNot { isOnGrid(it.value) }
        assertTrue("Jetons hors grille : $offGrid", offGrid.isEmpty())
        assertTrue(MusicProTokens.Duration.all == listOf(120, 200, 320))
    }

    @Test
    fun migrated_files_have_no_off_grid_dp() {
        val dpLiteral = Regex("""(\d+(?:\.\d+)?)\.dp\b""")
        val root = sourceRoot()
        val offenders = migratedFiles.flatMap { path ->
            val file = File(root, path)
            assertTrue("Fichier migré introuvable : $path", file.isFile)
            file.readLines().withIndex().flatMap { (index, line) ->
                dpLiteral.findAll(line)
                    .map { it.groupValues[1].toFloat() }
                    .filterNot { isOnGrid(it) }
                    .map { "$path:${index + 1} -> $it dp" }
                    .toList()
            }
        }
        assertTrue("Valeurs dp hors grille :\n${offenders.joinToString("\n")}", offenders.isEmpty())
    }
}
