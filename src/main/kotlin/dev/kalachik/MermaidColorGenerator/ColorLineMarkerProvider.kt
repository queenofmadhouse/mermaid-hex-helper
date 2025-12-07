package dev.kalachik.MermaidColorGenerator

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.ColorPicker
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.ColorIcon
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFence
import java.awt.Color
import java.awt.event.MouseEvent

class ColorLineMarkerProvider : LineMarkerProvider {

    private val hexPattern = Regex("#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\\b")

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        for (element in elements) {
            if (element !is LeafPsiElement) continue

            if (isInjectedFragment(element)) continue

            val file = element.containingFile?.virtualFile ?: continue
            val ext = file.extension?.lowercase()

            if (ext != "md" && ext != "mmd") continue

            if (ext == "md" && !isInsideMermaidBlock(element)) {
                continue
            }

            val text = element.text
            val matches = hexPattern.findAll(text)

            for (match in matches) {
                createLineMarker(element, match, result)
            }
        }
    }

    private fun isInsideMermaidBlock(element: PsiElement): Boolean {
        val codeFence = PsiTreeUtil.getParentOfType(element, MarkdownCodeFence::class.java) ?: return false

        return codeFence.fenceLanguage?.contains("mermaid", ignoreCase = true) == true
    }

    private fun isInjectedFragment(element: PsiElement): Boolean {
        val manager = InjectedLanguageManager.getInstance(element.project)
        return manager.getTopLevelFile(element) != element.containingFile
    }

    private fun createLineMarker(
        element: PsiElement,
        matchResult: MatchResult,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        try {
            val colorStr = matchResult.value
            val color = Color.decode(colorStr)
            val icon = ColorIcon(12, color)

            val startOffset = element.textRange.startOffset + matchResult.range.first
            val endOffset = element.textRange.startOffset + matchResult.range.last + 1
            val range = TextRange(startOffset, endOffset)

            val navHandler = GutterIconNavigationHandler<PsiElement> { e: MouseEvent, elt: PsiElement ->
                val newColor = ColorPicker.showDialog(e.component, "Choose Color", color, true, null, false)
                if (newColor != null) {
                    val newHex = "#" + ColorUtil.toHex(newColor)
                    WriteCommandAction.runWriteCommandAction(elt.project) {
                        val document = com.intellij.psi.PsiDocumentManager.getInstance(elt.project)
                            .getDocument(elt.containingFile)
                        document?.replaceString(range.startOffset, range.endOffset, newHex)
                    }
                }
            }

            result.add(LineMarkerInfo(
                element,
                range,
                icon,
                { "Change color" },
                navHandler,
                GutterIconRenderer.Alignment.LEFT,
                { "Color preview" }
            ))
        } catch (e: Exception) {
        }
    }
}
