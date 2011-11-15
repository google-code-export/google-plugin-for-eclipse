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
package com.google.gwt.eclipse.platform.editors.java.contentassist;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * Eclipse 3.3 implementation of base class for
 * {@link com.google.gwt.eclipse.core.editors.java.contentassist.JsniCompletionProposal }.
 * This version does nothing, since styled display text is only supported on
 * 3.4+
 */
public abstract class JsniCompletionProposal extends
    AbstractJsniCompletionProposal {

  public JsniCompletionProposal(IJavaCompletionProposal jdtProposal,
      CompletionProposal wrappedProposal) {
    super(jdtProposal, wrappedProposal);
  }

}