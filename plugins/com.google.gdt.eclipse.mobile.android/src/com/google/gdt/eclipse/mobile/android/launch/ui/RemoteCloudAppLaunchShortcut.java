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
package com.google.gdt.eclipse.mobile.android.launch.ui;

import com.google.gdt.eclipse.mobile.android.GdtAndroidPlugin;

import com.android.ide.eclipse.adt.internal.launch.AndroidLaunchController;
import com.android.ide.eclipse.adt.internal.sdk.ProjectState;
import com.android.ide.eclipse.adt.internal.sdk.Sdk;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * Launches the Android app in debug mode, against deployed app engine
 * application
 */
@SuppressWarnings("restriction")
public class RemoteCloudAppLaunchShortcut implements ILaunchShortcut {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart,
   * java.lang.String)
   */
  public void launch(IEditorPart editor, String mode) {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.
   * ISelection, java.lang.String)
   */

  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {

      // get the object and the project from it
      IStructuredSelection structSelect = (IStructuredSelection) selection;
      Object o = structSelect.getFirstElement();

      // get the first (and normally only) element
      if (o instanceof IAdaptable) {
        IResource r = (IResource) ((IAdaptable) o).getAdapter(IResource.class);

        // get the project from the resource
        if (r != null) {
          IProject project = r.getProject();
          if (project != null) {
            // delete the debug file

            // TODO: ask whether to deploy app engine project
            deleteDebugSettingsFile(project);

            ProjectState state = Sdk.getProjectState(project);
            if (state != null && state.isLibrary()) {

              MessageDialog.openError(
                  PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                  "Android Launch",
                  "Android library projects cannot be launched.");
            } else {
              // and launch
              launchAndroidApp(project, mode);
            }
          }
        }
      }
    }
  }

  private void deleteDebugSettingsFile(IProject project) {
    String filePath = "assets";
    String fileName = "/debugging_prefs.properties";

    IFile file = project.getFile(new Path(filePath + fileName));
    try {
      file.delete(true, new NullProgressMonitor());
      project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

    } catch (CoreException e) {
      GdtAndroidPlugin.getLogger().logError(
          "Could not delete debugging_prefs.properties file");
    }
  }

  /**
   * Launch a config for the specified project.
   * 
   * @param project The project to launch
   * @param mode The launch mode ("debug", "run")
   */
  private void launchAndroidApp(IProject project, String mode) {
    // get an existing or new launch configuration
    ILaunchConfiguration config = AndroidLaunchController.getLaunchConfig(project);

    if (config != null) {
      // and launch!
      DebugUITools.launch(config, mode);
    }
  }

}
