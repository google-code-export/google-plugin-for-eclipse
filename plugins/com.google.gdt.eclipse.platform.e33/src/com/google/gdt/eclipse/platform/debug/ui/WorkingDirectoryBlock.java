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
package com.google.gdt.eclipse.platform.debug.ui;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * WorkingDirectoryBlock wrapper for E33.
 * 
 * The methods below are overridden to prevent requiring a
 * @@SuppressWarnings("restriction") in subclasses.
 */
@SuppressWarnings("restriction")
public class WorkingDirectoryBlock extends
    org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock {
  
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    return super.isValid(config);
  }

  @Override
  protected ILaunchConfiguration getLaunchConfiguration() {
    return super.getLaunchConfiguration();
  }

  @Override
  protected String getWorkingDirectoryText() {
    return super.getWorkingDirectoryText();
  }

  @Override
  protected void setDefaultWorkingDir() {
    super.setDefaultWorkingDir();
  }

  @Override
  protected void setDefaultWorkingDirectoryText(String dir) {
    super.setDefaultWorkingDirectoryText(dir);
  }

}
