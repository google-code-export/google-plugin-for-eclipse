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
package com.google.gdt.eclipse.login;

import com.google.gdt.eclipse.core.extensions.ExtensionQuery.Data;
import com.google.gdt.eclipse.core.extensions.ExtensionQueryStringAttr;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utils for GoogleLogin.
 */
public class GoogleLoginUtils {
  
  /**
   * Takes a string that looks like "param1=val1&param2=val2&param3=val3" and
   * puts the key-value pairs into a map. The string is assumed to be UTF-8
   * encoded. If the string has a '?' character, then only the characters after
   * the question mark are considered.
   * 
   * @param params The parameter string.
   * @return A map with the key value pairs
   * @throws UnsupportedEncodingException if UTF-8 encoding is not supported
   */
  public static Map<String, String> parseUrlParameters(String params)
      throws UnsupportedEncodingException {
    Map<String, String> paramMap = new HashMap<String, String>();
    
    int qMark = params.indexOf('?');
    if (qMark > -1) {
      params = params.substring(qMark + 1);
    }
    
    String[] paramArr = params.split("&");
    for (String s : paramArr) {
      String[] keyVal = s.split("=");
      if (keyVal.length == 2) {
        paramMap.put(URLDecoder.decode(keyVal[0], "UTF-8"), URLDecoder.decode(
            keyVal[1], "UTF-8"));
      }
    }
    return paramMap;
  }

  /**
   * Returns a space delimited string of the OAuth scope contributions.
   */
  protected static String queryOAuthScopeExtensions() {
    ExtensionQueryStringAttr q = new ExtensionQueryStringAttr(
        GoogleLoginPlugin.PLUGIN_ID, "oauthScope", "scope");
    List<Data<String>> data = q.getData();
    List<String> scopes = new ArrayList<String>(data.size());
    for (Data<String> scopeData : data) {
      scopes.add(scopeData.getExtensionPointData().trim());
    }

    // ensure that the scopes are always in the same order so that the
    // check in loadLogin against the stored scopes is always in the same order
    Collections.sort(scopes);

    StringBuilder scopeString = new StringBuilder();
    for (String scope : scopes) {
      scopeString.append(scope);
      scopeString.append(" ");
    }
    return scopeString.toString().trim();
  }
}
