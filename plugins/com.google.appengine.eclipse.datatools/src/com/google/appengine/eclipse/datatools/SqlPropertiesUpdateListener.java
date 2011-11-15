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
package com.google.appengine.eclipse.datatools;

import com.google.appengine.eclipse.core.datatools.GaeSqlToolsExtension;
import com.google.appengine.eclipse.core.datatools.SqlConnectionProperties;

import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCConnectionProfileConstants;

import java.util.Properties;

/**
 * Uses My SQL driver type and definition to connect to Cloud SQL, as well as local MySQL. 
 */
public class SqlPropertiesUpdateListener implements GaeSqlToolsExtension {
  
  static final String DTP_MYSQL_VENDOR_PROP_ID = "MySql";
  static final String DTP_MYSQL_VERSION_PROP_ID = "5.1";
  static final String DTP_BRIDGE_PROP_DEFN_ID =
      "org.eclipse.datatools.connectivity.driverDefinitionID";
  static final String DTP_MYSQL_CONNECTION_PROFILE =
      "org.eclipse.datatools.enablement.mysql.connectionProfile";
  static final String DTP_GOOGLE_CLOUD_SQL_CONNECTION_PROFILE =
      "com.google.appengine.eclipse.datatools.connectionProfile";
  
  static final String DTP_MYSQLBRIDGE_PROP_DEFN_TYPE = 
      "org.eclipse.datatools.enablement.mysql.5_1.driverTemplate";
  static final String DTP_GOOGLEBRIDGE_PROP_DEFN_TYPE = 
      "org.eclipse.datatools.connectivity.googlecloudsql.driverTemplate";
  
  @Override
  public void updateConnectionProperties(SqlConnectionProperties connectionProperties) {
    Properties baseProperties = new Properties();
    
    baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, 
        connectionProperties.getJarPath());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_NAME_PROP_ID, 
        connectionProperties.getDatabaseName());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID, 
        connectionProperties.getDriverClass());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.PASSWORD_PROP_ID, 
        connectionProperties.getPassword());

    baseProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, 
        String.valueOf(true));
    baseProperties.setProperty(IJDBCConnectionProfileConstants.URL_PROP_ID, 
        connectionProperties.getJdbcUrl());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.USERNAME_PROP_ID, 
        connectionProperties.getUsername());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID, 
        DTP_MYSQL_VENDOR_PROP_ID);
    baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID, 
        DTP_MYSQL_VERSION_PROP_ID);
    String profileId;
    if (connectionProperties.getVendor() == SqlConnectionProperties.Vendor.GOOGLE) {
      baseProperties.setProperty(GoogleSqlDriverUIContributer.GOOGLESQL_INSTANCENAME_PROP_ID, 
          connectionProperties.getInstanceName());
      baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, 
          DTP_GOOGLEBRIDGE_PROP_DEFN_TYPE);
      profileId = DTP_GOOGLE_CLOUD_SQL_CONNECTION_PROFILE;
    } else {
      baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, 
          DTP_MYSQLBRIDGE_PROP_DEFN_TYPE);
      profileId = DTP_MYSQL_CONNECTION_PROFILE;
    }

    IPropertySet propertySet = new PropertySetImpl(getDriverName(connectionProperties), 
        getDriverId(connectionProperties));
    propertySet.setBaseProperties(baseProperties);    
    DriverInstance di = new DriverInstance(propertySet);
    DriverManager.getInstance().addDriverInstance(di);
    
    // Create a profile using the driver.
    ProfileManager profileManager = ProfileManager.getInstance();
    IConnectionProfile icp = profileManager.getProfileByName(getProfileName(connectionProperties));
    baseProperties.setProperty(DTP_BRIDGE_PROP_DEFN_ID, getDriverId(connectionProperties));
    try {
      IConnectionProfile oldProfile = profileManager.getProfileByName(
          getProfileName(connectionProperties)); 
      if (profileManager.getProfileByName(getProfileName(connectionProperties)) != null) {
        profileManager.deleteProfile(oldProfile);
      }
      profileManager.createProfile(getProfileName(connectionProperties), 
          getProfileId(connectionProperties), profileId, baseProperties);
    } catch (ConnectionProfileException e) {
      GoogleDatatoolsPluginLog.logError(e, "Could not create DTP connection profile");
    }
  }
  
  private String getDriverId(SqlConnectionProperties connectionProperties) {
    return "Driver ID (" + connectionProperties.getVendor() + ")";
  }
  
  private String getDriverName(SqlConnectionProperties connectionProperties) {
    return "Driver (" + connectionProperties.getVendor() + ")";
  }
  
  private String getProfileId(SqlConnectionProperties connectionProperties) {
    return "Profile ID (" + connectionProperties.getDisplayableConnectionPropertiesId() + ")";
  }
  
  private String getProfileName(SqlConnectionProperties connectionProperties) {
    return "Profile (" + connectionProperties.getDisplayableConnectionPropertiesId() + ")";
  }
}
