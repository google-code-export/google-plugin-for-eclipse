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
package com.google.gdt.eclipse.platform.jdt.text.correction.proposals;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.internal.ui.text.correction.NewCUCompletionUsingWizardProposal;

/**
 * Adapter around the eclipse version specific
 * {@link NewCUCompletionUsingWizardProposal}.
 */
@SuppressWarnings("restriction")
public class NewCompilationUnitUsingWizardProposal extends
    NewCUCompletionUsingWizardProposal {

  public static final int K_CLASS = NewCUCompletionUsingWizardProposal.K_CLASS;
  public static final int K_INTERFACE = NewCUCompletionUsingWizardProposal.K_INTERFACE;
  public static final int K_ENUM = NewCUCompletionUsingWizardProposal.K_ENUM;
  public static final int K_ANNOTATION = NewCUCompletionUsingWizardProposal.K_ANNOTATION;

  public NewCompilationUnitUsingWizardProposal(ICompilationUnit cu, Name node,
      int typeKind, IJavaElement typeContainer, int severity) {
    super(cu, node, typeKind, typeContainer, severity);
  }

  @Override
  public String getAdditionalProposalInfo() {
    return getAdditionalProposalInfo(new NullProgressMonitor());
  }

  public String getAdditionalProposalInfo(IProgressMonitor monitor) {
    return "";
  }

  protected String asLabel(String label) {
    // Nothing happens on eclipse 3.3
    return label;
  }
}
