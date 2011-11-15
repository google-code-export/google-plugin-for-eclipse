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

import org.eclipse.wst.sse.ui.internal.format.StructuredFormattingStrategy;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;

/**
 * Strategy to format XML documents.
 * <p>
 * Eclipse 3.3 uses the {@link StructuredFormattingStrategy} with the
 * {@link FormatProcessorXML} processor.
 */
@SuppressWarnings("restriction")
public class XmlFormattingStrategy extends StructuredFormattingStrategy {
  public XmlFormattingStrategy() {
    super(new FormatProcessorXML());
  }
}
