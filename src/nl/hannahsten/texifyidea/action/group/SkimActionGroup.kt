package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.run.pdfviewer.InternalPdfViewer

class SkimActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = InternalPdfViewer.SKIM.isAvailable()

    override fun hideIfNoVisibleChildren(): Boolean = true
}