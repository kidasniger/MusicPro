package com.example.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 095 — analyseur Markdown léger : blocs, segments en ligne,
 * liens et texte lisible pour la lecture à voix haute (098).
 */
class ChatMarkdownTest {

    @Test
    fun `paragraph simple devient un bloc paragraphe`() {
        val blocks = parseMarkdown("Bonjour, voici une réponse.")
        assertEquals(1, blocks.size)
        val paragraph = blocks.single() as MdBlock.MdParagraph
        assertEquals("Bonjour, voici une réponse.", paragraph.runs.joinToString("") { it.text })
    }

    @Test
    fun `titre et listes sont reconnus`() {
        val blocks = parseMarkdown("# Titre\n- premier point\n- second point\n1. numéroté")
        assertEquals(2, blocks.size)
        val heading = blocks[0] as MdBlock.MdHeading
        assertEquals(1, heading.level)
        assertEquals("Titre", heading.runs.joinToString("") { it.text })
        val bullets = blocks[1] as MdBlock.MdBulletList
        assertEquals(3, bullets.items.size)
    }

    @Test
    fun `gras italique et code en ligne sont stylés`() {
        val runs = parseInline("un **gras**, un *italique* et du `code`")
        assertEquals(6, runs.size)
        assertTrue(runs.any { it.bold && it.text == "gras" })
        assertTrue(runs.any { it.italic && it.text == "italique" })
        assertTrue(runs.any { it.code && it.text == "code" })
    }

    @Test
    fun `lien markdown et URL nue sont annotés`() {
        val runs = parseInline("voir [MusicPro](https://musicpro.app) et https://groq.com directement")
        val markdownLink = runs.first { it.link != null }
        assertEquals("MusicPro", markdownLink.text)
        assertEquals("https://musicpro.app", markdownLink.link)
        val bareUrl = runs.last { it.link != null }
        assertEquals("https://groq.com", bareUrl.link)
    }

    @Test
    fun `bloc de code capture le langage et le contenu`() {
        val blocks = parseMarkdown("Avant.\n```kotlin\nval x = 1\nval y = 2\n```\nAprès.")
        assertEquals(3, blocks.size)
        val code = blocks[1] as MdBlock.MdCodeBlock
        assertEquals("kotlin", code.language)
        assertEquals("val x = 1\nval y = 2", code.code)
    }

    @Test
    fun `bloc de code non refermé reste affichable`() {
        val blocks = parseMarkdown("```python\nprint('salut')")
        val code = blocks.single() as MdBlock.MdCodeBlock
        assertEquals("python", code.language)
        assertEquals("print('salut')", code.code)
    }

    @Test
    fun `texte lisible sans syntaxe pour la voix`() {
        val plain = markdownToPlainText("# Salon\n- Piano **doux**\n- [Site](https://x.y)")
        assertEquals("Salon\n– Piano doux\n– Site", plain)
    }
}
