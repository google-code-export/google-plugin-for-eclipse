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
package com.google.gdt.eclipse.platform.launch;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.PublishUtil;

/**
 * Eclipse 3.3-specific publisher for WTP modules.
 * 
 * Note that this implementation will not ignore the
 * <code>appengine-generated/</code> directory, which means that it will be
 * deleted every time a publish occurs. This is not a large problem, because
 * only a few people use Eclipse 3.3, and it is being deprecated soon.
 */
public class WtpPublisher {

  /**
   * Publish the WST {@link IModule}s to the specified WAR directory. Note that
   * the war directory will be erased and then recreated based on the latest
   * module contents.
   */
  public static void publishModulesToWarDirectory(IProject project,
      IModule[] modules, IPath warDirectoryPath, boolean forceFullPublish,
      IProgressMonitor monitor) throws CoreException {

    File warDirectory = warDirectoryPath.toFile();

    warDirectory.mkdirs();

    for (IModule module : modules) {
      if ("jst.web".equals(module.getModuleType().getId())) {
        ModuleDelegate delegate = (ModuleDelegate) module.loadAdapter(
            ModuleDelegate.class, null);
        if (delegate != null) {
          IModuleResource[] members = delegate.members();
          PublishUtil.publishSmart(members, warDirectoryPath, monitor);
        }
      }
    }
  }

}
