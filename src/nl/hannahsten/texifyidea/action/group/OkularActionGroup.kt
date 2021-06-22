package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.run.pdfviewer.InternalPdfViewer

class OkularActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = InternalPdfViewer.OKULAR.isAvailable()

    override fun hideIfNoVisibleChildren(): Boolean = true
}