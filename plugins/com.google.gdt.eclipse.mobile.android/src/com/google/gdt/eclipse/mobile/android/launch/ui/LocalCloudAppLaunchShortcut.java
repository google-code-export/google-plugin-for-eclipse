/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 *  All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.google.gdt.eclipse.mobile.android.launch.ui;

import com.google.gdt.eclipse.core.ResourceUtils;
import com.google.gdt.eclipse.core.launch.LaunchConfigurationAttributeUtilities;
import com.google.gdt.eclipse.core.launch.WebAppLaunchAttributes;
import com.google.gdt.eclipse.mobile.android.GdtAndroidPlugin;
import com.google.gdt.eclipse.mobile.android.wizards.helpers.ProjectCreationConstants;
import com.google.gdt.eclipse.suite.launch.WebAppLaunchUtil;
import com.google.gdt.eclipse.suite.launch.ui.WebAppLaunchShortcut;

import com.android.ide.eclipse.adt.internal.launch.AndroidLaunchController;
import com.android.ide.eclipse.adt.internal.sdk.ProjectState;
import com.android.ide.eclipse.adt.internal.sdk.Sdk;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The launch shortcut for Android App Engine Connected Application
 */
@SuppressWarnings("restriction")
public class LocalCloudAppLaunchShortcut extends WebAppLaunchShortcut {

  /**
   * Set the Gae project name in the preferences in the Android project.
   * 
   * @throws BackingStoreException
   */
  public static void setGaeProjectName(IProject project, String projectName)
      throws BackingStoreException {
    IScopeContext projectScope = new ProjectScope(project);
    IEclipsePreferences prefs = projectScope.getNode(
        GdtAndroidPlugin.PLUGIN_ID);
    prefs.put(ProjectCreationConstants.APP_ENGINE_PROJECT, projectName);
    prefs.flush();
  }

  private ILaunchConfiguration gaeConfig;

  private IProject project;

  public void launch(IEditorPart editor, String mode) {
  }

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

          project = r.getProject();
          if (project != null) {
            // get the app engine project and launch it
            String gaeProjectName = getGaeProjectName(project);

            if (gaeProjectName == null) {
              // TODO: improve message
              MessageDialog.openError(Display.getDefault().getActiveShell(),
                  "Debug Local App Engine Connected Application",
                  "Could not launch debug, the App Engine project was not found");
              return;
            }
            IProject gaeProject = ResourcesPlugin.getWorkspace()
                .getRoot().getProject(gaeProjectName);
            if (!gaeProject.isAccessible()) {
              // TODO: improve message
              MessageDialog.openError(Display.getDefault().getActiveShell(),
                  "Debug Local App Engine Connected Application",
                  "Could not launch debug, the App Engine project was not found");
              return;
            }
            launchGaeApp(gaeProject, mode);
            launchAndroidApp(project, mode);
          }
        }
      }
    }
  }

  /**
   * Get the Gae project name from the preferences in the Android project
   * 
   */
  private String getGaeProjectName(IProject project) {
    IScopeContext projectScope = new ProjectScope(project);
    IEclipsePreferences prefs = projectScope.getNode(
        GdtAndroidPlugin.PLUGIN_ID);
    return prefs.get(ProjectCreationConstants.APP_ENGINE_PROJECT, null);
  }

  /**
   * Launch a config for the specified project.
   * 
   * @param project The project to launch
   * @param mode The launch mode ("debug", "run")
   */
  private void launchAndroidApp(IProject project, String mode) {

    // write the debug file
    writeDebugSettingsFile(project);

    // verify project state
    ProjectState state = Sdk.getProjectState(project);
    if (state != null && state.isLibrary()) {
      MessageDialog.openError(
          PlatformUI.getWorkbench().getDisplay().getActiveShell(),
          "Android Launch", "Android library projects cannot be launched.");
      return;
    }

    // get an existing or new launch configuration
    ILaunchConfiguration config = AndroidLaunchController.getLaunchConfig(
        project);
    if (config != null) {
      // and launch!
      DebugUITools.launch(config, mode);
    } else {
      MessageDialog.openError(
          PlatformUI.getWorkbench().getDisplay().getActiveShell(),
          "Android Launch", "Android project could not be launched.");
    }
  }

  /**
   * Launch the gae application
   * 
   * @param project to launch
   * @param mode the launch mode - debug, run
   */
  private void launchGaeApp(IResource resource, String mode) {

    // Resolve to the actual resource (if it is linked)
    resource = ResourceUtils.resolveTargetResource(resource);

    try {
      String startupUrl = WebAppLaunchUtil.determineStartupURL(resource, false);
      if (startupUrl != null) {
        gaeConfig = findOrCreateLaunchConfiguration(
            resource, startupUrl, false);

        ILaunchConfigurationWorkingCopy wc = gaeConfig.getWorkingCopy();
        wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
        wc.doSave();

        assert (gaeConfig != null);

        DebugUITools.launch(gaeConfig, mode);
      }
    } catch (CoreException e) {
      GdtAndroidPlugin.log(e);
    } catch (OperationCanceledException e) {
      // Abort launch
    }
  }

  /**
   * Write the the local host address to debugging_prefs.properties file in the
   * assets folder of the Android project
   * 
   */
  private void writeDebugSettingsFile(IProject project) {
    String serverport = "8888"; //$NON-NLS-N$
    boolean autoPort;
    try {
      autoPort = LaunchConfigurationAttributeUtilities.getBoolean(
          gaeConfig, WebAppLaunchAttributes.AUTO_PORT_SELECTION);
      if (!autoPort) {
        serverport = LaunchConfigurationAttributeUtilities.getString(
            gaeConfig, WebAppLaunchAttributes.SERVER_PORT);
      }
      String filePath = ProjectCreationConstants.ASSETS_DIRECTORY;
      String fileName = "debugging_prefs.properties"; //$NON-NLS-N$

      IFile file = project.getFile(new Path(filePath + fileName));
      String address = InetAddress.getLocalHost().getHostAddress();
      StringBuffer buffer = new StringBuffer();
      buffer.append("url=http://"); //$NON-NLS-N$
      buffer.append(address);
      buffer.append(":" + serverport);
      ResourceUtils.createFolderStructure(project, new Path(filePath));
      IPath location = file.getLocation();
      if (location != null) {
        ResourceUtils.writeToFile(location.toFile(), buffer.toString());
      } else {
        ResourceUtils.createFile(file.getFullPath(), buffer.toString());
      }
      project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

    } catch (CoreException e) {
      GdtAndroidPlugin.log(e);
    } catch (UnknownHostException e) {
      GdtAndroidPlugin.log(e);
    } catch (UnsupportedEncodingException e) {
      GdtAndroidPlugin.log(e);
    } catch (IOException e) {
      GdtAndroidPlugin.log(e);
    }
  }
}
