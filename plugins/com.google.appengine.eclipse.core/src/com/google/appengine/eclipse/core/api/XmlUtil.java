/*******************************************************************************
 * Copyright 2012 Google Inc. All Rights Reserved.
 * 
 *  All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.google.appengine.eclipse.core.api;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Xml Manipulation Utility Class for modifying web.xml for generated APIs.
 */
public class XmlUtil {

  private static final String WEB_APP = "web-app";
  private static final String SERVLET = "servlet";
  private static final String SERVLET_NAME = "servlet-name";
  private static final String SERVLET_MAPPING = "servlet-mapping";
  private static final String DEV_API_SERVER = "DevApiServer";
  private static final String WEB_XML_RELATIVE_PATH = "/war/WEB-INF/web.xml";
  private static final String SERVLET_CLASS = "servlet-class";
  private static final String URL_PATTERN = "url-pattern";
  private static final String API_URL_PATTERN = "/_ah/api/*";
  private static final
      String DEV_API_SERVER_CLASS = "com.google.api.server.spi.tools.devserver.DevApiServlet";

  private IPath webXmlIPath;

  /**
   * Insert a DevApiServer node in web.xml inside webApp node.
   * 
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   * @throws TransformerException
   * @throws TransformerFactoryConfigurationError
   */
  public void insertDevApiServer(IProject project)
      throws SAXException,
      IOException,
      ParserConfigurationException,
      TransformerFactoryConfigurationError,
      TransformerException {
    webXmlIPath = ResourcesPlugin.getWorkspace()
        .getRoot()
        .getLocation()
        .append(project.getFullPath())
        .append(WEB_XML_RELATIVE_PATH);
    String webXmlPath = webXmlIPath.toString();
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    org.w3c.dom.Document doc = docBuilder.parse(webXmlPath);
    Node devApiServerNode = findNode(doc, SERVLET);
    if (devApiServerNode == null
        || !devApiServerNode.getNodeName().equals(WEB_APP)) {
      return;
    }
    insertDevApiServer(doc, devApiServerNode);
    saveFile(doc);
  }

  /**
   * Remove DevApiServer node (before deploy) from web.xml.
   * 
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws TransformerException
   * @throws TransformerFactoryConfigurationError
   */
  public void removeDevApiServerNode(IProject project)
      throws ParserConfigurationException,
      SAXException,
      IOException,
      TransformerFactoryConfigurationError,
      TransformerException {
    boolean saveRequired = false;
    webXmlIPath = ResourcesPlugin.getWorkspace()
        .getRoot()
        .getLocation()
        .append(project.getFullPath())
        .append(WEB_XML_RELATIVE_PATH);
    String webXmlPath = webXmlIPath.toString();
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    org.w3c.dom.Document doc = docBuilder.parse(webXmlPath);
    Node devApiServerNode = findNode(doc, SERVLET);
    if (devApiServerNode != null
        && !devApiServerNode.getNodeName().equals(WEB_APP)) {
      devApiServerNode.getParentNode().removeChild(devApiServerNode);
      saveRequired = true;
    }
    devApiServerNode = findNode(doc, SERVLET_MAPPING);
    if (devApiServerNode != null
        && !devApiServerNode.getNodeName().equals(WEB_APP)) {
      devApiServerNode.getParentNode().removeChild(devApiServerNode);
      saveRequired = true;
    }
    if (saveRequired) {
      saveFile(doc);
    }
  }

  private Node findNode(org.w3c.dom.Document doc, String parentNodeName) {
    Node webAppNode = null;

    for (webAppNode = doc.getFirstChild(); webAppNode != null;
        webAppNode = webAppNode.getNextSibling()) {
      if (webAppNode.getNodeName().equals(WEB_APP)) {
        break;
      }
    }
    if (webAppNode == null) {
      return null;
    }

    Node devApiServerNode = null;
    for (devApiServerNode = webAppNode.getFirstChild();
        devApiServerNode != null;
        devApiServerNode = devApiServerNode.getNextSibling()) {
      Node tempNode = null;
      if (devApiServerNode.getNodeName().equals(parentNodeName)) {
        for (tempNode = devApiServerNode.getFirstChild(); tempNode != null;
            tempNode = tempNode.getNextSibling()) {
          if (tempNode.getNodeName().equals(SERVLET_NAME)
              && tempNode.getTextContent().equals(DEV_API_SERVER)) {
            break;
          }
        }
        if (tempNode != null) {
          break;
        }
      }
    }
    if (devApiServerNode == null) {
      return webAppNode;
    } else {
      return devApiServerNode;
    }
  }

  /**
   * @param doc
   * @param webAppNode
   */
  private void insertDevApiServer(Document doc, Node webAppNode) {
    Node n2, n3, n4;
    n2 = doc.createElement(SERVLET);
    webAppNode.appendChild(n2);
    n3 = doc.createElement(SERVLET_MAPPING);
    webAppNode.appendChild(n3);

    n4 = doc.createTextNode("\n" + " " + " ");
    n2.appendChild(n4);
    n4 = doc.createElement(SERVLET_NAME);
    n4.setTextContent(DEV_API_SERVER);
    n2.appendChild(n4);
    n4 = doc.createTextNode("\n" + " " + " ");
    n2.appendChild(n4);
    n4 = doc.createElement(SERVLET_CLASS);
    n4.setTextContent(DEV_API_SERVER_CLASS);
    n2.appendChild(n4);
    n4 = doc.createTextNode("\n" + " ");
    n2.appendChild(n4);

    n4 = doc.createTextNode("\n" + " " + " ");
    n3.appendChild(n4);
    n4 = doc.createElement(SERVLET_NAME);
    n4.setTextContent(DEV_API_SERVER);
    n3.appendChild(n4);
    n4 = doc.createTextNode("\n" + " " + " ");
    n3.appendChild(n4);
    n4 = doc.createElement(URL_PATTERN);
    n4.setTextContent(API_URL_PATTERN);
    n3.appendChild(n4);
    n4 = doc.createTextNode("\n" + " ");
    n3.appendChild(n4);
  }

  private void saveFile(org.w3c.dom.Document doc)
      throws TransformerFactoryConfigurationError, TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(
        new DOMSource(doc), new StreamResult(webXmlIPath.toFile()));
  }
}
