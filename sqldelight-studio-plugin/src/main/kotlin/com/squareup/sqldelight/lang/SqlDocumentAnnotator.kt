/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sqldelight.lang

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.squareup.sqldelight.Status

internal class SqlDocumentAnnotator : ExternalAnnotator<Status, Status>() {
  override fun collectInformation(file: PsiFile) = (file as SqliteFile).status
  override fun doAnnotate(status: Status) = status
  override fun apply(file: PsiFile, status: Status, holder: AnnotationHolder) {
    when (status) {
      is Status.Failure -> {
        holder.createErrorAnnotation(TextRange(status.originatingElement.start.startIndex,
            status.originatingElement.stop.stopIndex + 1), status.errorMessage)
      }
      is Status.Invalid -> {
        for (exception in status.exceptions) {
          holder.createErrorAnnotation(TextRange(exception.originatingElement.start.startIndex,
              exception.originatingElement.stop.stopIndex + 1), exception.message)
        }
      }
      is Status.Success -> {
        val generatedFile = (file as SqliteFile).generatedFile ?: return
        val document = FileDocumentManager.getInstance().getDocument(generatedFile.virtualFile)
        document?.createGuardedBlock(0, document.textLength)
      }
    }
  }
}
