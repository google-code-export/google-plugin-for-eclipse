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

import org.eclipse.wst.xml.ui.internal.XMLFormattingStrategy;

/**
 * XML formatting strategy that works with temporary XML models (those that
 * correspond to documents without a file backing.)
 * <p>
 * The {@link org.eclipse.wst.xml.ui.internal.XMLFormattingStrategy} relies on
 * {@link org.eclipse.wst.sse.core.internal.model.ModelManagerImpl#getModelForEdit(org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument)}
 * . This first tries to get an ID for the document, which fails and returns
 * null for our temp documents.
 * <p>
 * Since Eclipse 3.5 fixes the above issue, this version-specific class will use
 * the default Eclipse 3.5 behavior.
 */
@SuppressWarnings("restriction")
public class XmlFormattingStrategy extends XMLFormattingStrategy {
}
