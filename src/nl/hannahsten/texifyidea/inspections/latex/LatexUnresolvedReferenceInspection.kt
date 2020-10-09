package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.findLatexAndBibtexLabelStringsInFileSet
import nl.hannahsten.texifyidea.util.firstParentOfType
import java.util.EnumSet

/**
 * @author Hannah Schellekens
 */
open class LatexUnresolvedReferenceInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "UnresolvedReference"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND, MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Unresolved reference"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val labels = file.findLatexAndBibtexLabelStringsInFileSet()
        val commands = file.commandsInFile()

        for (command in commands) {
            if (!Magic.Command.reference.contains(command.name)) {
                continue
            }

            // Don't resolve references in command definitions, as in \cite{#1} the #1 is not a reference
            if (command.parent.firstParentOfType(LatexCommands::class)?.name in Magic.Command.commandDefinitions) {
                continue
            }

            val required = command.requiredParameters
            if (required.isEmpty()) {
                continue
            }

            val parts = required[0].split(",")
            for (i in parts.indices) {
                val part = parts[i]
                if (part == "*") continue

                // The cleveref package allows empty items to customize enumerations
                if (part.isEmpty() && (command.commandToken.text == "\\cref" || command.commandToken.text == "\\Cref")) continue

                // If there is no label with this required label parameter value
                if (!labels.contains(part.trim())) {
                    // We have to subtract from the total length, because we do not know whether optional
                    // parameters were included with [a][b][c] or [a,b,c] in which case the
                    // indices of the parts are different with respect to the start of the command
                    var offset = command.textLength - parts.sumBy { it.length + 1 }
                    for (j in 0 until i) {
                        offset += parts[j].length + 1
                    }

                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            TextRange.from(offset, part.length),
                            "Unresolved reference '$part'",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                        )
                    )
                }
            }
        }

        return descriptors
    }
}