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
package com.google.gdt.eclipse.core.pde;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;

/**
 * Utility methods for manipulating {@link Bundle}s.
 */
public final class BundleUtilities {

  /**
   * Returns <code>true</code> if this bundle implements the extension point
   * with the given <code>extensionPointId</code>.
   * 
   * @param bundle the bundle to test
   * @param extensionSimpleId the simple id of the extension point
   * @param extensionPointId extension id to search for
   * 
   * @return <code>true</code> if this bundle implements the extension point
   *         with the given <code>extensionPointId</code>
   */
  public static boolean contributesToExtensionPoint(Bundle bundle,
      String extensionSimpleId, String extensionPointId) {
    IExtensionRegistry registry = RegistryFactory.getRegistry();
    IContributor contributor = ContributorFactoryOSGi.createContributor(bundle);
    for (IExtension extension : registry.getExtensions(contributor.getName())) {
      if (extension.getExtensionPointUniqueIdentifier().equals(extensionPointId)) {
        if (extensionSimpleId != null
            && !extensionSimpleId.equals(extension.getSimpleIdentifier())) {
          continue;
        }

        return true;
      }
    }

    return false;
  }

  private BundleUtilities() {
  }
}
