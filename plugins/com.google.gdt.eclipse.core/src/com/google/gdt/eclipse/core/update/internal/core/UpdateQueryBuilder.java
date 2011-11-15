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
package com.google.gdt.eclipse.core.update.internal.core;

import com.google.gdt.eclipse.core.CorePlugin;
import com.google.gdt.eclipse.core.extensions.ExtensionQuery;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builds the query string to include with a feature update check request.
 */
public class UpdateQueryBuilder {

  public static final String API_ADD_ACTION = "api_add";

  public static final String GAE_BACKEND_DEPLOY_ACTION = "gae_backend_deploy";
  
  public static final String GAE_DEPLOY_ACTION = "gae_deploy";

  public static final String GPH_PROJECT_IMPORT = "gph_import";

  private static final String ACTION_PARAM = "&action=";

  /**
   * The argument to add to the &action= param. May be null.
   */
  private String action;

  /**
   * If this is for an api add ping, then the name of the api added.
   */
  private String apiName;
  
  /**
   * The current eclipse version that we are using.
   */
  private String eclipseVersion;

  /**
   * The version of the feature that we are looking to update.
   */
  private String featureVersion;

  /**
   * The hash of the GAE app's ID, if applicable.
   */
  private String gaeAppIdHash;

  /**
   * The installation id.
   */
  private String installationId;

  private Map<String, String> maxSdkVersions;

  /**
   * The product information for eclipse, to distinguish between different
   * "brands" of Eclipse, eg "normal" eclipse and STS.
   */
  private String productId;

  /**
   * Sets if this query is a gae deploy ping.
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * @param apiName
   */
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * Sets the eclipse version string to use in the query.
   * 
   * @param eclipseVersion eclipse version string to use in the query
   */
  public void setEclipseVersion(String eclipseVersion) {
    this.eclipseVersion = eclipseVersion;
  }

  /**
   * Sets the feature version component.
   * 
   * @param featureVersion feature version component
   */
  public void setFeatureVersion(String featureVersion) {
    this.featureVersion = featureVersion;
  }

  /**
   * Calculates and stores the hash of the app id of the project if the given
   * project is a GAE project.
   */
  public void setGaeAppIdHash(String hash) {
    this.gaeAppIdHash = hash;
  }

  /**
   * Sets the installation id.
   * 
   * @param installationId installation id
   */
  public void setInstallationId(String installationId) {
    this.installationId = installationId;
  }
  
  public void setMaxSdkVersions(Map<String, String> maxSdkVersions) {
    this.maxSdkVersions = maxSdkVersions;
  }

  /**
   * Sets the product ID to differentiate different branded versions of eclipse,
   * eg, STS is com.springsource.sts.ide while regular eclipse is
   * org.eclipse.sdk.ide.
   */
  public void setProductId(String productId) {
    this.productId = productId;
  }

  /**
   * Converts this query into a string that can be used as a URL query
   * parameter.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("id=");
    sb.append(installationId);
    sb.append("&ev=");
    sb.append(eclipseVersion);
    sb.append("&v=");
    sb.append(featureVersion);
    if (maxSdkVersions != null && !maxSdkVersions.isEmpty()) {
      for (Entry<String, String> entry : maxSdkVersions.entrySet()) {
        sb.append("&");
        String key = entry.getKey();
        assert (key != null);
        sb.append(key);
        sb.append("=");
        String value = entry.getValue();
        assert (value != null);
        sb.append(value);
      }
    }

    sb.append("&p=");
    sb.append(productId);

    if (action != null) {
      sb.append(ACTION_PARAM);
      sb.append(action);
    }

    if (gaeAppIdHash != null) {
      sb.append("&appIdHash=");
      sb.append(gaeAppIdHash);
    }

    if (apiName != null) {
      sb.append("&apiName=");
      sb.append(apiName);
    }
    
    addExtentionContributions(sb);

    return sb.toString();
  }

  private void addExtentionContributions(StringBuilder sb) {
    ExtensionQuery<UpdateQueryArgContributor> extQuery = new ExtensionQuery<UpdateQueryArgContributor>(
        CorePlugin.PLUGIN_ID, "updateQueryArgContributor", "class");
    List<ExtensionQuery.Data<UpdateQueryArgContributor>> contributors = extQuery.getData();
    for (ExtensionQuery.Data<UpdateQueryArgContributor> c : contributors) {
      UpdateQueryArgContributor uqac = c.getExtensionPointData();
      sb.append(uqac.getContribution());
    }
  }
}
