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

import org.eclipse.core.runtime.content.IContentDescription;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * A proxy version of
 * {@link org.eclipse.core.internal.content.XMLContentDescriber}.
 */
@SuppressWarnings("restriction")
public class XMLContentDescriber extends
    org.eclipse.core.internal.content.XMLContentDescriber {

  /*
   * Overridden because super's is restricted.
   */
  @Override
  public int describe(InputStream input, IContentDescription description)
      throws IOException {
    return super.describe(input, description);
  }

  /*
   * Overridden because super's is restricted.
   */
  @Override
  public int describe(Reader input, IContentDescription description)
      throws IOException {
    return super.describe(input, description);
  }
}
