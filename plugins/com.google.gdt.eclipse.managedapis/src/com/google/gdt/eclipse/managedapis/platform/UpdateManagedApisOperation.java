/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.google.gdt.eclipse.managedapis.platform;

import com.google.gdt.eclipse.managedapis.ManagedApi;
import com.google.gdt.eclipse.managedapis.ManagedApiPlugin;
import com.google.gdt.eclipse.managedapis.ManagedApiProject;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The UpdateManagedApisOperation isolate changes to the classpath in an
 * undoable operation. The caller provides context and runs within an undo
 * history.
 * 
 * This type is abstract and is intended to be extended by an anonymous inner
 * class so that we can access internals of the ManagedApiProject without
 * exposing them publicly.
 * 
 * The heavy-lifting for this class is supplied by an apply method which takes a
 * current state and a target state and calls the methods necessary to get from
 * the original state to the target state.
 */
public class UpdateManagedApisOperation extends AbstractOperation {

  private List<ManagedApi> before, after;

  private ManagedApiProject managedApiProject;

  public UpdateManagedApisOperation(String label) {
    super(label);
  }

  @Override
  public IStatus execute(IProgressMonitor monitor, IAdaptable info)
      throws ExecutionException {
    // snapshot the "before" state.
    if (before == null) {
      before = Arrays.asList(managedApiProject.getManagedApis());
    }
    return redo(monitor, info);
  }

  @Override
  public IStatus redo(IProgressMonitor monitor, IAdaptable info)
      throws ExecutionException {
    try {
      return apply(before, after);
    } catch (CoreException e) {
      throw new ExecutionException("Error redoing Managed API change", e);
    }
  }

  public void setAfterManagedApis(ManagedApi[] managedApis) {
    after = Arrays.asList(managedApis);
  }

  /**
   * automatically set for you. Only set if your before state is not the current
   * state.
   */
  public void setBeforeManagedApis(ManagedApi[] managedApis) {
    before = Arrays.asList(managedApis);
  }

  public void setManagedApiProject(ManagedApiProject managedApiProject) {
    this.managedApiProject = managedApiProject;
  }

  @Override
  public IStatus undo(IProgressMonitor monitor, IAdaptable info)
      throws ExecutionException {
    try {
      return apply(after, before);
    } catch (CoreException e) {
      throw new ExecutionException("Error redoing Managed API change", e);
    }
  }

  private IStatus apply(List<ManagedApi> stateBeforeApply,
      List<ManagedApi> stateAfterApply) throws CoreException {
    try {
      ArrayList<ManagedApi> toRemove = new ArrayList<ManagedApi>();
      toRemove.addAll(stateBeforeApply);
      toRemove.removeAll(stateAfterApply);

      ArrayList<ManagedApi> toAdd = new ArrayList<ManagedApi>();
      toAdd.addAll(stateAfterApply);
      toAdd.removeAll(stateBeforeApply);

      IClasspathEntry[] initialRawClasspath = managedApiProject.getJavaProject().getRawClasspath();
      List<IClasspathEntry> rawClasspathList = new ArrayList<IClasspathEntry>();
      rawClasspathList.addAll(Arrays.asList(initialRawClasspath));

      if (stateAfterApply.size() > 0) {
        managedApiProject.addManagedApiProjectState();
      }

      for (ManagedApi apiToRemove : toRemove) {
        String extraPathInfo = apiToRemove.getRootDirectory().getName();
        IClasspathEntry entry = JavaCore.newContainerEntry(ManagedApiPlugin.API_CONTAINER_PATH.append(extraPathInfo));
        rawClasspathList.remove(entry);
      }

      for (ManagedApi apiToAdd : toAdd) {
        String extraPathInfo = apiToAdd.getRootDirectory().getName();
        IClasspathEntry entry = JavaCore.newContainerEntry(ManagedApiPlugin.API_CONTAINER_PATH.append(extraPathInfo));
        if (!rawClasspathList.contains(entry)) {
          rawClasspathList.add(entry);
        }
      }

      IClasspathEntry[] newRawClasspath = rawClasspathList.toArray(new IClasspathEntry[rawClasspathList.size()]);
      managedApiProject.getJavaProject().setRawClasspath(newRawClasspath, null);

      if (stateAfterApply.size() == 0) {
        managedApiProject.removeManagedApiProjectState();
      } else {
        managedApiProject.notifyManagedApisRemoved(
            toRemove.toArray(new ManagedApi[toRemove.size()]));
        managedApiProject.notifyManagedApisAdded(
            toAdd.toArray(new ManagedApi[toAdd.size()]));
      }
    } catch (JavaModelException e) {
      return new Status(
          IStatus.ERROR,
          ManagedApiPlugin.PLUGIN_ID,
          "Error updating the ManagedAPIs within the UpdateManagedApisOperation",
          e);
    }

    return Status.OK_STATUS;
  }
}
