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
package com.google.gdt.eclipse.platform.jdt.model;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility methods for the Java model supported by the current platform.
 */
public class PlatformJavaModelUtilities {

  /**
   * Exception thrown when IAnnotation (and related classes) are not available
   * on the current Eclipse version.
   */
  @SuppressWarnings("serial")
  public static class IAnnotationNotAvailableException extends Exception {
  }

  /**
   * @throws IAnnotationNotAvailableException IAnnotation is not available on
   *           Eclipse 3.3
   */
  public static Object getAnnotation(String qualifiedAnnotationName,
      Object annotatable, IType contextType) throws JavaModelException,
      IAnnotationNotAvailableException {
    throw new IAnnotationNotAvailableException();
  }

  /**
   * @throws IAnnotationNotAvailableException IAnnotation is not available on
   *           Eclipse 3.3
   */
  public static <T> T getSingleMemberAnnotationValue(Object annotation, Class<T> type)
      throws JavaModelException, IAnnotationNotAvailableException {
    throw new IAnnotationNotAvailableException();
  }

}
