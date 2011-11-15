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
package com.google.gdt.eclipse.gph.install;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import java.util.Dictionary;

/**
 * A static utility class for bundle utilities.
 */
public class BundleUtilities {

  /**
   * @return whether the given bundle has all of its dependencies satisfied
   *         (including optional dependencies)
   */
  public static boolean areBundlesDependenciesSatisfied(String bundleId) {
    Bundle bundle = Platform.getBundle(bundleId);

    if (bundle == null) {
      return false;
    }

    // TODO: we need to do this using the bundle APIs, and not in such a brute
    // force way

    @SuppressWarnings("unchecked")
    Dictionary<String, String> headers = bundle.getHeaders();

    String requiredBundles[] = headers.get("Require-Bundle").split(",");

    for (String requiredBundleId : requiredBundles) {
      String id = requiredBundleId.split(";")[0];

      if (!isBundleInstalled(id)) {
        return false;
      }
    }

    return true;
  }

  /**
   * @return whether all the given bundles are installed
   */
  public static boolean areBundlesInstalled(String[] bundleIds) {
    for (String bundleId : bundleIds) {
      if (!isBundleInstalled(bundleId)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return whether the given bundle is installed
   */
  public static boolean isBundleInstalled(String bundleId) {
    Bundle bundle = Platform.getBundle(bundleId);

    return bundle != null;
  }
  
  
  private BundleUtilities() {
  }

}
