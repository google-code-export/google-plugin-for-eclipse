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
package com.google.gdt.eclipse.mobile.android.resources;

import com.google.gdt.eclipse.appengine.rpc.nature.AppEngineConnectedNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IStartup;

/**
 * Monitors the workspace for changes to {@link #APPENGINE_WEB_XML} config files,
 * scheduling reconciliations via a {@link HandleAppConfigChangeJob}.
 */
public class WorkspaceMonitor implements IStartup {

  // project relative path of the appengine config xml file
  private static final IPath APPENGINE_WEB_XML = new Path("war").append( //$NON-NLS-1$
      "WEB-INF").append("appengine-web.xml"); //$NON-NLS-1$ //$NON-NLS-2$

  private static final WorkspaceMonitor INSTANCE = new WorkspaceMonitor();

  /**
   * Get the shared {@link WorkspaceMonitor} instance.
   * 
   * @return the monitor
   */
  public static WorkspaceMonitor getInstance() {
    return INSTANCE;
  }

  private IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {

    public void resourceChanged(IResourceChangeEvent event) {

      IResourceDelta delta = event.getDelta();
      if (delta == null) {
        return;
      }
      for (IResourceDelta child : delta.getAffectedChildren(IResourceDelta.ADDED
          | IResourceDelta.CHANGED)) {
        if (child != null) {
          IResource resource = child.getResource();
          if (resource.isAccessible()) {
            if (resource instanceof IProject) {
              IProject project = (IProject) resource;
              if (AppEngineConnectedNature.isAppEngineProject(project)) {
                IResourceDelta resourceDelta = child.findMember(APPENGINE_WEB_XML);
                if (resourceDelta != null) {
                  appengineWebXMLChanged(resourceDelta.getResource());
                }
              }
            }
          }
        }
      }
    }
  };

  /**
   * Add this monitor to the workspace.
   */
  public void addToWorkspace() {
    ResourcesPlugin.getWorkspace().addResourceChangeListener(
        resourceChangeListener, IResourceChangeEvent.POST_BUILD);
  }

  /**
   * Perform early startup.
   */
  public void earlyStartup() {
    getInstance().addToWorkspace();
  }

  /**
   * Remove this monitor from the workspace.
   */
  public void removeFromWorkspace() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(
        resourceChangeListener);
  }

  /**
   * Process this changed appengine-web.xml
   */
  protected void appengineWebXMLChanged(IResource resource) {
    new HandleAppConfigChangeJob(resource).schedule();
  }

}
