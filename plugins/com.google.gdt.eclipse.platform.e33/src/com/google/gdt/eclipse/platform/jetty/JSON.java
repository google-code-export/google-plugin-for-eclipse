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
package com.google.gdt.eclipse.platform.jetty;

import java.util.Map;

/**
 * Simplified implementation of Jetty 6.x's JSON class for our purposes:
 * 
 * - toString(Map) that converts Maps to a JSON object, and Strings to JSON
 * strings
 */
public class JSON {
  @SuppressWarnings("unchecked")
  public static String toString(Map<String, ?> map) {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (Map.Entry<String, ?> entry : map.entrySet()) {
      sb.append('"').append(escapeJsonCharacters(entry.getKey())).append("\":");
      if (entry.getValue() instanceof String) {
        sb.append('"').append(escapeJsonCharacters((String) entry.getValue())).append(
            '"');
      } else if (entry.getValue() instanceof Map<?, ?>) {
        sb.append(toString((Map<String, ?>) entry.getValue()));
      } else {
        throw new RuntimeException("Could not convert to JSON: "
            + entry.getValue());
      }

      sb.append(',');
    }

    // Take off last ','
    sb.setLength(sb.length() - 1);

    sb.append('}');

    return sb.toString();
  }

  private static String escapeJsonCharacters(String string) {
    string = string.replaceAll("'", "\\'");
    return string.replaceAll("\"", "\\\\\"");
  }
}
