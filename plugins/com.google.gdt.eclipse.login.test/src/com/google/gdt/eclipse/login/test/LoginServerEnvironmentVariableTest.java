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
package com.google.gdt.eclipse.login.test;

import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetTemporaryToken;
import com.google.gdt.eclipse.login.GoogleLoginUtils;

import junit.framework.TestCase;

public class LoginServerEnvironmentVariableTest extends TestCase {

  /**
   * Ensure that the hacks used to switch out the login server using an
   * environment variable work.
   */
  public void testLoginServerEnvironmentVariable() throws Exception {

    String serverUrl = "http://login.google.com"; // this is bogus.

    GoogleOAuthGetTemporaryToken temporaryToken = new GoogleOAuthGetTemporaryToken();
    GoogleLoginUtils.setGenericUrl(temporaryToken, serverUrl
        + "/accounts/OAuthGetRequestToken");

    assertEquals("login.google.com", temporaryToken.host);
    assertEquals("http", temporaryToken.scheme);
    assertEquals(-1, temporaryToken.port);

    String[] pathParts = new String[]{"", "accounts", "OAuthGetRequestToken"};
    for (int i = 0; i < pathParts.length; i++) {
      assertEquals(pathParts[i], temporaryToken.pathParts.get(i));
    }
  }

}
