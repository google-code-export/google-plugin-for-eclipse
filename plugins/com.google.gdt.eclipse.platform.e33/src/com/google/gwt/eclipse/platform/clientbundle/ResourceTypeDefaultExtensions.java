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
package com.google.gwt.eclipse.platform.clientbundle;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility class for mapping between a client bundle resource type and its
 * default file extensions.
 * 
 * This version uses hard-coded mappings instead of GWT's DefaultExtensions
 * annotations, since Eclipse 3.3 does not provide support for accessing
 * annotations via the Java Model.
 */
public final class ResourceTypeDefaultExtensions {

  public static String[] getDefaultExtensions(IType resourceType)
      throws JavaModelException {
    String[] extensions = getDeclaredDefaultExtensions(resourceType);

    // Check the super interface hierarchy
    if (extensions.length == 0) {
      ITypeHierarchy superHierarchy = resourceType.newSupertypeHierarchy(null);
      IType[] superInterfaces = superHierarchy.getAllSuperInterfaces(resourceType);
      for (IType superInterface : superInterfaces) {
        extensions = getDeclaredDefaultExtensions(superInterface);
        if (extensions.length > 0) {
          break;
        }
      }
    }
    return extensions;
  }

  private static String[] getDeclaredDefaultExtensions(IType resourceType) {
    String typeName = resourceType.getFullyQualifiedName('.');

    if (typeName.equals("com.google.gwt.resources.client.ImageResource")) {
      return new String[] {".png", ".jpg", ".gif", ".bmp"};
    }

    if (typeName.equals("com.google.gwt.resources.client.CssResource")) {
      return new String[] {".css"};
    }

    if (typeName.equals("com.google.gwt.resources.client.TextResource")) {
      return new String[] {".txt"};
    }

    if (typeName.equals("com.google.gwt.resources.client.ExternalTextResource")) {
      return new String[] {".txt"};
    }

    return new String[0];
  }

  private ResourceTypeDefaultExtensions() {
    // Not instantiable
  }

}
