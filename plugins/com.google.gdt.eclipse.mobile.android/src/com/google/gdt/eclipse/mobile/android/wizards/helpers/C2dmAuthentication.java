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
package com.google.gdt.eclipse.mobile.android.wizards.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * User authentication for C2DM service
 */

public class C2dmAuthentication {

  public static String authToken = "";

  /**
   * Get auth token from server for C2DM service for the account specified by
   * user
   */
  public static void getAuth(String email, String passwd) {
    OutputStreamWriter wr = null;
    BufferedReader rd = null;

    try {
      URL url = new URL("https://www.google.com/accounts/ClientLogin"); //$NON-NLS-N$
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true);
      wr = new OutputStreamWriter(conn.getOutputStream());
      String data = "Email=" + URLEncoder.encode(email, "UTF-8") + "&Passwd=" //$NON-NLS-N$
          + URLEncoder.encode(passwd, "UTF-8") //$NON-NLS-N$
          + "&accountType=GOOGLE&service=ac2dm"; //$NON-NLS-N$
      wr.write(data);
      wr.flush();

      rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null) {
        if (line.startsWith("Auth=")) {
          authToken = line.substring(5);
        }
      }
    } catch (IOException e) {

    } finally {
      if (wr != null) {
        try {
          wr.close();
        } catch (IOException e) {
        }
      }
      if (rd != null) {
        try {
          rd.close();
        } catch (IOException e) {
        }
      }
    }
  }

}
