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
package com.google.appengine.eclipse.core.datatools;

import com.google.appengine.eclipse.core.AppEngineCorePlugin;
import com.google.appengine.eclipse.core.AppEngineCorePluginLog;
import com.google.appengine.eclipse.core.properties.GoogleCloudSqlProperties;
import com.google.appengine.eclipse.core.sdk.AppEngineBridge;
import com.google.gdt.eclipse.core.WebAppUtilities;
import com.google.gdt.eclipse.core.extensions.ExtensionQuery;
import com.google.gdt.eclipse.login.GoogleLogin;

import org.eclipse.core.resources.IProject;
import org.eclipse.equinox.security.storage.StorageException;

import java.util.List;

/**
 * Populates the objects in the extension point
 * com.google.appengine.eclipse.core.gaeSqlToolsExtension and calls
 * createProfileDefinition().
 */
public final class SqlConnectionExtensionPopulator {
  
  /**
   * There types of connections cloud sql production, cloud sql testing and
   * local mysql can be specified in the preferences ui. 
   */
  public enum ConnectionType {
    CONNECTION_TYPE_TEST ("GoogleSQL.TestingInstance"),
    CONNECTION_TYPE_PROD ("GoogleSQL.ProductionInstance"),
    CONNECTION_TYPE_LOCAL_MYSQL ("MySQL");
    
    private String displayableStringForType;
    
    private ConnectionType(String displayableString) {
      displayableStringForType = displayableString;  
    }
    
    public String getDisplayableStringForType() {
      return displayableStringForType;
    }
  }
  private static final String GAE_MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
  private static final String GAE_CLOUD_SQL_DRIVER_CLASS = "com.google.cloud.sql.Driver";
  private static final String GAE_CLOUD_SQL_JAR_IN_WAR = "/WEB-INF/lib/" 
      + AppEngineBridge.APPENGINE_CLOUD_SQL_JAR;

  private static final String GAE_SQL_CONNECTION_PROPERTIES_EXTENSION_POINT = 
      "gaeSqlToolsExtension";
  
  public static void populateCloudSQLBridgeExtender(IProject project, 
      ConnectionType connectionType) {
    
    ExtensionQuery<GaeSqlToolsExtension> extensionQuery = 
        new ExtensionQuery<GaeSqlToolsExtension>(
            AppEngineCorePlugin.PLUGIN_ID, GAE_SQL_CONNECTION_PROPERTIES_EXTENSION_POINT,
            "class");

    List<ExtensionQuery.Data<GaeSqlToolsExtension>> 
        sqlPropertylUpdateListenerList = extensionQuery.getData();
    
    for (ExtensionQuery.Data<GaeSqlToolsExtension> bridge : 
        sqlPropertylUpdateListenerList) {
      try {
        SqlConnectionProperties connectionProperties;
        connectionProperties = getSqlConnectionPropertiesByType(project, connectionType);
        bridge.getExtensionPointData().updateConnectionProperties(connectionProperties);
      } catch (StorageException e) {
        AppEngineCorePluginLog.logError(e, "Error while populating connection");
      } catch (Exception e) {
        AppEngineCorePluginLog.logError(e, "Error while populating connection");
      }
    }
    return;
  }
  
  private static String getCloudSqlJarPath(IProject project) {
    String jarPath = WebAppUtilities.getWarOutLocation(project).append(
        GAE_CLOUD_SQL_JAR_IN_WAR).toString();
    return jarPath;
  }
  
  private static String getCloudSqlJdbcUrl(String instanceName, String databaseName) {
    String refreshToken = GoogleLogin.getInstance().fetchOAuth2RefreshToken();
    String clientId = GoogleLogin.getInstance().fetchOAuth2ClientId();
    String clientSecret = GoogleLogin.getInstance().fetchOAuth2ClientSecret();
    String url = "jdbc:google:rdbms://" + instanceName + "/" + databaseName + "?oauth2RefreshToken=" 
        + refreshToken;
    if (clientId != null && clientId.trim().isEmpty() == false) {
      url += "&oauth2ClientId=" + clientId;
    }
    if (clientSecret != null && clientSecret.trim().isEmpty() == false) {
      url += "&oauth2ClientSecret=" + clientSecret;
    }
    return url;
  }
  
  private static String getDisplaybleConnectionId(String projectName, ConnectionType type) {
    return projectName + "." + type.getDisplayableStringForType();
  }

  private static String getLocalMySqlJdbcUrl(String hostname, String databaseName,
      String portNumber) {
    return "jdbc:mysql://" + hostname + ":" + portNumber + "/" + databaseName;
  }

  private static SqlConnectionProperties getSqlConnectionPropertiesByType(IProject project, 
      ConnectionType connectionType) throws StorageException {
      if (connectionType == ConnectionType.CONNECTION_TYPE_LOCAL_MYSQL) {
        return setLocalMySqlConnectionProperties(project); 
      } else if (connectionType == ConnectionType.CONNECTION_TYPE_PROD) {
        return setProdCloudSqlConnectorsProperties(project);
      } else if (connectionType == ConnectionType.CONNECTION_TYPE_TEST) {
        return setTestCloudSqlConnectorsProperties(project);
      }
      return null;
  }

  private static SqlConnectionProperties setLocalMySqlConnectionProperties(
      IProject project) {
    SqlConnectionProperties sqlConnectionProperties = new SqlConnectionProperties();
    
    String username = GoogleCloudSqlProperties.getMySqlDatabaseUser(project);
    String databaseName = GoogleCloudSqlProperties.getMySqlDatabaseName(project);
    String password = GoogleCloudSqlProperties.getMySqlDatabasePassword(project);
    String hostName = GoogleCloudSqlProperties.getMySqlHostName(project);
    String portNumber = new Integer(GoogleCloudSqlProperties.getMySqlPort(project)).toString();
    String driverURL = getLocalMySqlJdbcUrl(hostName, databaseName, portNumber);
    String driverClass = GAE_MYSQL_DRIVER_CLASS;
    String jarPath = GoogleCloudSqlProperties.getMySqlJdbcJar(project);
    String connectionId = getDisplaybleConnectionId(project.getName(), 
        ConnectionType.CONNECTION_TYPE_LOCAL_MYSQL);
    
    sqlConnectionProperties.setUsername(username);
    sqlConnectionProperties.setPassword(password);
    sqlConnectionProperties.setJdbcUrl(driverURL);
    sqlConnectionProperties.setDatabaseName(databaseName);
    sqlConnectionProperties.setJarPath(jarPath);
    sqlConnectionProperties.setDriverClass(driverClass);
    sqlConnectionProperties.setDisplayableConnectionPropertiesId(connectionId);
    sqlConnectionProperties.setInstanceName(null);
    sqlConnectionProperties.setVendor(SqlConnectionProperties.Vendor.MYSQL);

    return sqlConnectionProperties;
  }
  
  private static SqlConnectionProperties setProdCloudSqlConnectorsProperties(
      IProject project) {
    SqlConnectionProperties sqlConnectionProperties = new SqlConnectionProperties();

    String username = GoogleCloudSqlProperties.getProdDatabaseUser(project);
    String instanceName = GoogleCloudSqlProperties.getProdInstanceName(project);
    String databaseName = GoogleCloudSqlProperties.getProdDatabaseName(project);
    String password = GoogleCloudSqlProperties.getProdDatabasePassword(project);
    String driverURL = getCloudSqlJdbcUrl(instanceName, databaseName);
    String driverClass = GAE_CLOUD_SQL_DRIVER_CLASS;
    String jarPath = getCloudSqlJarPath(project);
    String connectionId = getDisplaybleConnectionId(project.getName(), 
        ConnectionType.CONNECTION_TYPE_PROD);

    sqlConnectionProperties.setUsername(username);
    sqlConnectionProperties.setPassword(password);
    sqlConnectionProperties.setJdbcUrl(driverURL);
    sqlConnectionProperties.setDatabaseName(databaseName);
    sqlConnectionProperties.setJarPath(jarPath);
    sqlConnectionProperties.setDriverClass(driverClass);
    sqlConnectionProperties.setDisplayableConnectionPropertiesId(connectionId);
    sqlConnectionProperties.setInstanceName(instanceName);
    sqlConnectionProperties.setVendor(SqlConnectionProperties.Vendor.GOOGLE);
    
    return sqlConnectionProperties;
  }
 
  private static SqlConnectionProperties setTestCloudSqlConnectorsProperties(
      IProject project) {
    SqlConnectionProperties sqlConnectionProperties = new SqlConnectionProperties();
    
    String username = GoogleCloudSqlProperties.getTestDatabaseUser(project);
    String instanceName = GoogleCloudSqlProperties.getTestInstanceName(project);
    String databaseName = GoogleCloudSqlProperties.getTestDatabaseName(project);
    String password = GoogleCloudSqlProperties.getTestDatabasePassword(project);
    String driverURL = getCloudSqlJdbcUrl(instanceName, databaseName);
    String driverClass = GAE_CLOUD_SQL_DRIVER_CLASS;
    String jarPath = getCloudSqlJarPath(project);
    String connectionId = getDisplaybleConnectionId(project.getName(), 
        ConnectionType.CONNECTION_TYPE_TEST);

    sqlConnectionProperties.setUsername(username);
    sqlConnectionProperties.setPassword(password);
    sqlConnectionProperties.setJdbcUrl(driverURL);
    sqlConnectionProperties.setDatabaseName(databaseName);
    sqlConnectionProperties.setJarPath(jarPath);
    sqlConnectionProperties.setDriverClass(driverClass);
    sqlConnectionProperties.setDisplayableConnectionPropertiesId(connectionId);
    sqlConnectionProperties.setInstanceName(instanceName);
    sqlConnectionProperties.setVendor(SqlConnectionProperties.Vendor.GOOGLE);
    
    return sqlConnectionProperties;
  }
}
