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
package com.google.gdt.eclipse.gph.extensions;

/**
 * Implementers provide an API key for the Project Hosting API via the
 * <code>apikeyprovider</code> extension point.
 * 
 * See http://code.google.com/apis/console.
 */
public interface IKeyProvider {

  /**
   * Returns a key for the Project Hosting API. <code>null</code> and the empty
   * string are not considered to be keys.
   */
  String getKey();
}
