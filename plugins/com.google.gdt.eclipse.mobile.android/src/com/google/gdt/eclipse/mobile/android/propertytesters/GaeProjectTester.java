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

import com.google.appengine.eclipse.core.nature.GaeNature;
import com.google.gdt.eclipse.core.AdapterUtilities;
import com.google.gdt.eclipse.core.ResourceUtils;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Tests if the project has GaeNature
 */

@SuppressWarnings("restriction")
public class GaeProjectTester extends PropertyTester {

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
   * java.lang.String, java.lang.Object[], java.lang.Object)
   */
            
  public boolean test(Object receiver, String property, Object[] args,
      Object expectedValue) {

    IResource resource = AdapterUtilities.getAdapter(receiver, IResource.class);

    if (resource == null) {
      return false;
    }
    // Resolve to the actual resource (if it is linked)
    resource = ResourceUtils.resolveTargetResource(resource);
    if (resource == null) {
      return false;
    }
    IProject project = resource.getProject();
    return (project == resource)
        && (project.isAccessible() && GaeNature.isGaeProject(project));
  }

}
