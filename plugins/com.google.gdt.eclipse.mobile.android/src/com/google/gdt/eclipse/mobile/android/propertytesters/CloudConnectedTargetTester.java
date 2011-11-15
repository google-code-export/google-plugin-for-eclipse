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
package com.google.gdt.eclipse.mobile.android.propertytesters;

import com.google.gdt.eclipse.appengine.rpc.nature.AppEngineConnectedNature;
import com.google.gdt.eclipse.core.AdapterUtilities;
import com.google.gdt.eclipse.core.ResourceUtils;
import com.google.gdt.eclipse.mobile.android.GdtAndroidPlugin;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A PropertyTester applied to resources to determine if they should have a App
 * Engine Connected Application launch shortcut applied to them.
 * 
 * 
 **/
public class CloudConnectedTargetTester extends PropertyTester {

  public boolean test(Object receiver, String property, Object[] args,
      Object expectedValue) {

    assert (receiver != null);
    IResource resource = AdapterUtilities.getAdapter(receiver, IResource.class);

    if (resource == null) {
      // Unexpected case; we were asked to test against something that's
      // not a resource.
      return false;
    }

    // Resolve to the actual resource (if it is linked)
    resource = ResourceUtils.resolveTargetResource(resource);

    try {
      return (resourceIsProject(resource) && (isAndroidCloudConnectedProject(resource)));
    } catch (CoreException ce) {
      GdtAndroidPlugin.getLogger().logError(ce);
      return false;
    }
  }

  private boolean isAndroidCloudConnectedProject(IResource resource)
      throws CoreException {
    return AppEngineConnectedNature.isCloudConnectedProject(resource.getProject())
        && resource.getProject().isAccessible();
  }

  /**
   * Is this resource a project?
   * 
   * @param resource
   * @return true if the resource is a project.
   */
  private boolean resourceIsProject(IResource resource) {
    if (resource == null) {
      return false;
    }

    IProject proj = resource.getProject();
    boolean out = (proj == resource);
    return out;
  }

}
