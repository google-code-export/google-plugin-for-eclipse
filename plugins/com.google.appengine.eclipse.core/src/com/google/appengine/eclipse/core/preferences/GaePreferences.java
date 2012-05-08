/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.google.appengine.eclipse.core.preferences;

import com.google.appengine.eclipse.core.AppEngineCorePlugin;
import com.google.appengine.eclipse.core.AppEngineCorePluginLog;
import com.google.appengine.eclipse.core.datatools.SqlConnectionExtensionPopulator;
import com.google.appengine.eclipse.core.nature.GaeNature;
<<<<<<< .mine
import com.google.appengine.eclipse.core.properties.GaeProjectProperties;
=======
import com.google.appengine.eclipse.core.properties.GaeProjectProperties;
import com.google.appengine.eclipse.core.properties.GoogleCloudSqlProperties;
import com.google.appengine.eclipse.core.properties.ui.GaeProjectPropertyPage;
>>>>>>> .r4
import com.google.appengine.eclipse.core.resources.GaeProject;
<<<<<<< .mine
import com.google.appengine.eclipse.core.sdk.AppEngineBridge;
import com.google.appengine.eclipse.core.sdk.AppEngineUpdateProjectSdkCommand;
=======
import com.google.appengine.eclipse.core.sdk.AppEngineUpdateProjectSdkCommand;
>>>>>>> .r4
import com.google.appengine.eclipse.core.sdk.AppEngineUpdateWebInfFolderCommand;
import com.google.appengine.eclipse.core.sdk.GaeSdk;
import com.google.appengine.eclipse.core.sdk.GaeSdkCapability;
import com.google.appengine.eclipse.core.sdk.GaeSdkContainer;
import com.google.gdt.eclipse.core.WebAppUtilities;
import com.google.gdt.eclipse.core.sdk.ClasspathContainerUpdateJob;
import com.google.gdt.eclipse.core.sdk.SdkManager;
import com.google.gdt.eclipse.core.sdk.SdkManager.SdkUpdate;
import com.google.gdt.eclipse.core.sdk.SdkManager.SdkUpdateEvent;
import com.google.gdt.eclipse.core.sdk.SdkSet;
import com.google.gdt.eclipse.core.sdk.UpdateProjectSdkCommand.UpdateType;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.service.prefs.BackingStoreException;

import java.io.FileNotFoundException;
<<<<<<< .mine
import java.util.List;
=======
import java.io.IOException;
import java.util.List;
>>>>>>> .r4

/**
 * Contains static methods for retrieving and setting GAE plug-in preferences.
 */
public final class GaePreferences {

  private static SdkManager<GaeSdk> sdkManager;

  static {
    sdkManager = new SdkManager<GaeSdk>(
        GaeSdkContainer.CONTAINER_ID, getEclipsePreferences(), GaeSdk.getFactory());
    sdkManager.addSdkUpdateListener(new SdkManager.SdkUpdateListener<GaeSdk>() {
      public void onSdkUpdate(SdkUpdateEvent<GaeSdk> sdkUpdateEvent) throws CoreException {
        IJavaProject[] projects =
            JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
        GaeSdk newDefaultSdk = null;
        List<SdkUpdate<GaeSdk>> sdkUpdates = sdkUpdateEvent.getUpdates();
        for (SdkUpdate<GaeSdk> sdkUpdate : sdkUpdates) {
          if (sdkUpdate.getType() == SdkUpdate.Type.NEW_DEFAULT) {
            newDefaultSdk = sdkUpdate.getSdk();
            break;
          }
        }
        GaeSdk newDefaultSdk = null;
        List<SdkUpdate<GaeSdk>> sdkUpdates = sdkUpdateEvent.getUpdates();
        for (SdkUpdate<GaeSdk> sdkUpdate : sdkUpdates) {
          if (sdkUpdate.getType() == SdkUpdate.Type.NEW_DEFAULT) {
            newDefaultSdk = sdkUpdate.getSdk();
            break;
          }
        }
        for (IJavaProject project : projects) {
          if (!GaeNature.isGaeProject(project.getProject())) {
            continue;
          }
          GaeSdk sdk = null;
          try {
<<<<<<< .mine
            if (GaeProjectProperties.getIsUseSdkFromDefault(project.getProject())) {
              sdk = newDefaultSdk;
            } else {
              GaeProject p = GaeProject.create(project.getProject());
              sdk = p.getSdk();
=======
            if (GaeProjectProperties.getIsUseSdkFromDefault(project.getProject())) {
              sdk = newDefaultSdk;
              // If a project has Google Cloud SQL enabled and the selected sdk
              // is incompatible with Google Cloud SQL, we do nothing.
              if (sdk != null
                  && GoogleCloudSqlProperties.getGoogleCloudSqlEnabled(project.getProject())
                  && GoogleCloudSqlProperties.getLocalDevMySqlEnabled(project.getProject())
                  && sdk.getCapabilities().contains(GaeSdkCapability.GOOGLE_CLOUD_SQL)) {
                GaeProjectPropertyPage.copyJdbcDriverJar(project, sdk);
              }
            } else {
              GaeProject p = GaeProject.create(project.getProject());
              sdk = p.getSdk();
>>>>>>> .r4
            }
<<<<<<< .mine
            if (sdk != null && WebAppUtilities.hasManagedWarOut(project.getProject())) {
              UpdateType updateType = AppEngineUpdateProjectSdkCommand.computeUpdateType(
                  GaeSdk.findSdkFor(project), sdk,
                  GaeProjectProperties.getIsUseSdkFromDefault(project.getProject()));
              new AppEngineUpdateProjectSdkCommand(
                  project, GaeSdk.findSdkFor(project), sdk, updateType, null).execute();
              new AppEngineUpdateWebInfFolderCommand(project, sdk).execute();

              // Finally make sure that DTP google_sql.jar driver is reset in dtp connections
              SqlConnectionExtensionPopulator.populateCloudSQLBridgeExtender(project,
                  sdk.getInstallationPath().toOSString()
                  + AppEngineBridge.APPENGINE_CLOUD_SQL_JAR_PATH_IN_SDK
                  + AppEngineBridge.APPENGINE_CLOUD_SQL_JAR);
            }
=======
            if (sdk != null && WebAppUtilities.hasManagedWarOut(project.getProject())) {
              UpdateType updateType = AppEngineUpdateProjectSdkCommand.computeUpdateType(
                  GaeSdk.findSdkFor(project), sdk, 
                  GaeProjectProperties.getIsUseSdkFromDefault(project.getProject()));
              new AppEngineUpdateProjectSdkCommand(project, GaeSdk.findSdkFor(project), sdk, 
                  updateType, null).execute();
              new AppEngineUpdateWebInfFolderCommand(project, sdk).execute();
            }
>>>>>>> .r4
          } catch (FileNotFoundException e) {
            // Log the error and continue
            AppEngineCorePluginLog.logError(e);
          } catch (BackingStoreException e) {
            // Log the error and continue
            AppEngineCorePluginLog.logError(e);
          } catch (IOException e) {
            // Log the error and continue
            AppEngineCorePluginLog.logError(e);
          }
        }

        ClasspathContainerUpdateJob classpathContainerUpdateJob = new ClasspathContainerUpdateJob(
            "ClasspathContainerUpdateJob", GaeSdkContainer.CONTAINER_ID);
        classpathContainerUpdateJob.schedule();
      }
    });
  }

  public static GaeSdk getDefaultSdk() {
    return getSdks().getDefault();
  }

  public static String getDeployEmailAddress() {
    return AppEngineCorePlugin.getDefault().getPluginPreferences().getString(
        GaePreferenceConstants.DEPLOY_EMAIL_ADDRESS);
  }

  public static SdkManager<GaeSdk> getSdkManager() {
    return sdkManager;
  }

  /**
   * Returns the current {@link com.google.gdt.eclipse.core.sdk.SdkSet} state.
   */
  public static SdkSet<GaeSdk> getSdks() {
    return getSdkManager().getSdks();
  }

  public static void setDefaultSdk(GaeSdk sdk) {
    getSdks().setDefault(sdk);
  }

  public static void setDeployEmailAddress(String address) {
    assert (address != null);
    AppEngineCorePlugin.getDefault().getPluginPreferences().setValue(
        GaePreferenceConstants.DEPLOY_EMAIL_ADDRESS, address);
  }

  /**
   * Sets the current {@link com.google.gdt.eclipse.core.sdk.SdkSet} 
   * state without concern for merges, etc.
   */
  public static void setSdks(SdkSet<GaeSdk> sdkSet) {
    try {
      getSdkManager().setSdks(sdkSet);
    } catch (CoreException e) {
      AppEngineCorePluginLog.logError(e);
    }
  }

  private static IEclipsePreferences getEclipsePreferences() {
    InstanceScope scope = new InstanceScope();
    IEclipsePreferences workspacePrefs = scope.getNode(AppEngineCorePlugin.PLUGIN_ID);
    return workspacePrefs;
  }

  private GaePreferences() {
    // Not instantiable
  }

}

