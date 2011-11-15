/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.platform.content;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.IFormattingStrategyExtension;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.formatter.DefaultXMLPartitionFormatter;
import org.eclipse.wst.xml.ui.internal.Logger;

import java.util.LinkedList;

/**
 * XML formatting strategy that works with temporary XML models (those that
 * correspond to documents without a file backing.)
 * <p>
 * The {@link org.eclipse.wst.xml.ui.internal.XMLFormattingStrategy} relies on
 * {@link org.eclipse.wst.sse.core.internal.model.ModelManagerImpl#getModelForEdit(org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument)}
 * . This first tries to get an ID for the document, which fails and returns
 * null for our temp documents.
 * <p>
 * This implementation is derived from
 * {@link org.eclipse.wst.xml.ui.internal.XMLFormattingStrategy} with changes to
 * the way we retreive the model. We use
 * {@link org.eclipse.wst.sse.core.internal.model.ModelManagerImpl#getExistingModelForEdit(org.eclipse.jface.text.IDocument)}
 * , which does not require a file-backed document. Instead, it traverses the
 * managed models looking for the one that is mapped to the given document.
 */
@SuppressWarnings({"restriction", "unchecked"})
public class XmlFormattingStrategy extends
    ContextBasedFormattingStrategy implements IFormattingStrategyExtension {

  /** Documents to be formatted by this strategy */
  private final LinkedList fDocuments = new LinkedList();
  /** Partitions to be formatted by this strategy */
  private final LinkedList fPartitions = new LinkedList();
  private IRegion fRegion;
  private DefaultXMLPartitionFormatter formatter = new DefaultXMLPartitionFormatter();

  /*
   * @see
   * org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#format()
   */
  public void format() {
    super.format();

    final IDocument document = (IDocument) fDocuments.removeFirst();
    final TypedPosition partition = (TypedPosition) fPartitions.removeFirst();

    if (document != null && partition != null && fRegion != null) {
      try {
        if (document instanceof IStructuredDocument) {
          IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForEdit(
              document);
          if (model != null) {
            try {
              TextEdit edit = formatter.format(model, fRegion.getOffset(),
                  fRegion.getLength());
              if (edit != null) {
                try {
                  model.aboutToChangeModel();
                  edit.apply(document);
                } finally {
                  model.changedModel();
                }
              }
            } finally {
              model.releaseFromEdit();
            }
          }
        }
      } catch (BadLocationException e) {
        // log for now, unless we find reason not to
        Logger.log(Logger.INFO, e.getMessage());
      }
    }
  }

  /*
   * @see
   * org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStarts
   * (org.eclipse.jface.text.formatter.IFormattingContext)
   */
  public void formatterStarts(final IFormattingContext context) {
    super.formatterStarts(context);

    fPartitions.addLast(context.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
    fDocuments.addLast(context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
    fRegion = (IRegion) context.getProperty(FormattingContextProperties.CONTEXT_REGION);
  }

  /*
   * @see
   * org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops
   * ()
   */
  public void formatterStops() {
    super.formatterStops();

    fPartitions.clear();
    fDocuments.clear();
  }
}
