package com.aemtools.lang.html.inspection

import com.aemtools.completion.util.hasChild
import com.aemtools.completion.util.isInsideOF
import com.aemtools.constant.const.htl.DATA_SLY_INCLUDE
import com.aemtools.constant.const.htl.DATA_SLY_USE
import com.aemtools.constant.messages.annotator.SIMPLIFY_EXPRESSION
import com.aemtools.inspection.fix.ELSimplifyIntention
import com.aemtools.lang.htl.psi.HtlHtlEl
import com.aemtools.lang.htl.psi.HtlStringLiteral
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager

/**
 * @author Dmytro_Troynikov
 */
class RedundantELAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is HtlHtlEl && accept(element)) {

      val elementPointer = SmartPointerManager.getInstance(element.project)
          .createSmartPsiElementPointer(element)

      holder.createWarningAnnotation(element, SIMPLIFY_EXPRESSION)
          .registerFix(ELSimplifyIntention(elementPointer))
    }
  }

  private fun accept(element: PsiElement): Boolean = (element is HtlHtlEl
      && element.isDumbStringLiteralEl()
      && (element.isInsideOF(DATA_SLY_USE) || element.isInsideOF(DATA_SLY_INCLUDE)))

  /**
   * Check if current current [HtlHtlEl] is a "Dumb String Literal", which mean
   * that the expression doesn't contain anything except the string literal, e.g.:
   * ```
   *   ${'static string'}
   * ```
   */
  private fun HtlHtlEl.isDumbStringLiteralEl(): Boolean =
      this.hasChild(HtlStringLiteral::class.java)
          && !this.hasChild(com.aemtools.lang.htl.psi.HtlPropertyAccess::class.java)
          && !this.hasChild(com.aemtools.lang.htl.psi.HtlContextExpression::class.java)

}
