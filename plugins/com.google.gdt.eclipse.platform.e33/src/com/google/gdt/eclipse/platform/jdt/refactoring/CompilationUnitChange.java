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
package com.google.gdt.eclipse.platform.jdt.refactoring;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * A proxy version of
 * {@link org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange}.
 */
@SuppressWarnings("restriction")
public class CompilationUnitChange extends
    org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange {

  public CompilationUnitChange(String name, ICompilationUnit cunit) {
    super(name, cunit);
  }

  /*
   * This method has become public in Eclipse 3.5 but is internal in older
   * versions. If we suppress the internal restriction, we get warnings on E3.5
   * (unnecessarily suppressed). Instead, for these older versions, we override
   * this method which allows it to become public API.
   */
  @Override
  public ICompilationUnit getCompilationUnit() {
    return super.getCompilationUnit();
  }
}
