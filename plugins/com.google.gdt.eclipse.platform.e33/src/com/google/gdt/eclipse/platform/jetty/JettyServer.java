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

import org.mortbay.http.HttpContext;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.InetAddrPort;

import javax.servlet.Servlet;

/**
 * Eclipse 3.4-specific JettyServer
 */
public class JettyServer implements IJettyServer {

  private int port;

  private Server server;

  private final ServletHandler servletHandler = new ServletHandler();

  private final ClassLoader classLoader;

  public JettyServer(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public void addServlet(String path, Servlet servlet) {
    servletHandler.addServlet(path, servlet.getClass().getName());
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void start() throws Exception {
    if (server != null) {
      return;
    }

    server = new Server();
    server.addListener(new InetAddrPort(port));

    HttpContext root = new HttpContext(server, "/");
    root.addHandler(servletHandler);

    /*
     * Set the class loader to the given class loader. Surprisingly, the current
     * thread's context class loader won't include the subclass and its peers
     * (assuming the servlet is a peer of the subclass).
     */
    root.setClassLoader(classLoader);

    server.start();
  }

  public void stop() throws Exception {
    if (server == null) {
      return;
    }

    server.stop();
    server = null;
  }

}
