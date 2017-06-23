package com.aemtools.index

import com.aemtools.completion.util.toPsiFile
import com.aemtools.index.model.TemplateDefinition
import com.aemtools.util.allScope
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PathUtil
import com.intellij.util.indexing.FileBasedIndex

/**
 * @author Dmytro Troynikov
 */
object HtlIndexFacade {

    private val SLY_USE_EXTENSIONS = listOf("js", "html")

    /**
     * Find file for sly use
     * @param name the name of file
     * @param psiFile the "relative" file
     * @return resolved file
     */
    fun resolveFile(name: String, psiFile: PsiFile): PsiFile? {

        val extension = PathUtil.getFileExtension(name)
                ?: return null

        if (extension !in SLY_USE_EXTENSIONS) {
            return null
        }

        val normalizedName = if (isAbsolutePath(name)) {
            name
        } else {
            with(psiFile.virtualFile.path) {
                substring(0, lastIndexOf('/')) + "/$name"
            }
        }

        val files = FilenameIndex
                .getAllFilesByExt(psiFile.project, extension, GlobalSearchScope.projectScope(psiFile.project))
        val file = files.find { it.path.endsWith(normalizedName) }
                ?: return null
        return file.toPsiFile(psiFile.project)
    }

    fun importableFiles(relativeToFile: PsiFile): List<PsiFile> {
        val htmlFiles = FilenameIndex
                .getAllFilesByExt(relativeToFile.project, "html", GlobalSearchScope.projectScope(relativeToFile.project))

        val relativeToDir = relativeToFile.containingDirectory.virtualFile.path

        return htmlFiles.filter { it.path.startsWith(relativeToDir) }
                .map { it.toPsiFile(relativeToFile.project) }
                .filterNotNull()
                .filterNot { it.name == relativeToFile.name }
    }

    /**
     * Collects all Htl files containing templates.
     * @return list of [TemplateDefinition] objects
     */
    fun getTemplates(project: Project): List<TemplateDefinition> {
        val fbi = FileBasedIndex.getInstance()
        val keys = fbi.getAllKeys(HtlTemplateIndex.HTL_TEMPLATE_ID, project)
        val result = keys.flatMap {
            fbi.getValues(HtlTemplateIndex.HTL_TEMPLATE_ID, it, project.allScope())
        }
        return result
    }

    private fun isAbsolutePath(path: String): Boolean = path.startsWith("/")

}