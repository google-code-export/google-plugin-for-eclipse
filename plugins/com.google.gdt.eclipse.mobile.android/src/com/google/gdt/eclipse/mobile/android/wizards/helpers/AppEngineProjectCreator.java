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

import com.google.appengine.eclipse.core.preferences.GaePreferences;
import com.google.appengine.eclipse.core.sdk.AppEngineUpdateWebInfFolderCommand;
import com.google.appengine.eclipse.core.sdk.GaeSdk;
import com.google.appengine.eclipse.core.sdk.GaeSdkContainer;
import com.google.gdt.eclipse.core.ResourceUtils;
import com.google.gdt.eclipse.core.WebAppUtilities;
import com.google.gdt.eclipse.core.natures.NatureUtils;
import com.google.gdt.eclipse.core.projects.IWebAppProjectCreator;
import com.google.gdt.eclipse.core.sdk.Sdk;
import com.google.gdt.eclipse.core.sdk.Sdk.SdkException;
import com.google.gdt.eclipse.mobile.android.GdtAndroidPlugin;
import com.google.gdt.eclipse.suite.wizards.WebAppProjectCreator;
import com.google.gwt.eclipse.core.sdk.GWTUpdateWebInfFolderCommand;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.osgi.service.prefs.BackingStoreException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the App Engine project as part of the Cloud connected android
 * application
 * 
 */
public class AppEngineProjectCreator extends WebAppProjectCreator {

  public static final IWebAppProjectCreator.Factory FACTORY = new IWebAppProjectCreator.Factory() {
    public IWebAppProjectCreator create() {
      return new AppEngineProjectCreator();
    }
  };

  private static String[] SERVER_SOURCE_FILES = new String[] {
      "DeviceInfo.java", "HelloWorldService.java", "Message.java", //$NON-NLS-N$
      "MessageLocator.java", "RegistrationInfo.java", "SendMessage.java"}; //$NON-NLS-N$

  private static String[] CLIENT_SOURCE_FILES = new String[] {"Main.java", //$NON-NLS-N$
      "MainWidget.java", "MainWidget.ui.xml"}; //$NON-NLS-N$

  private static String[] SHARED_SOURCE_FILES = new String[] {
      "MessageProxy.java", "RegistrationInfoProxy.java"}; //$NON-NLS-N$

  private static String[] SHARED_CLIENT_FILES = new String[] {"MyRequestFactory.java"}; //$NON-NLS-N$

  private static final String TEMPLATES_DIRECTORY = "templates/gae/"; //$NON-NLS-1$

  private AppEngineProjectCreator() {
    super();
  }

  /**
   * Creates the project per the current configuration. Note that the caller
   * must have a workspace lock in order to successfully execute this method.
   * 
   * @throws BackingStoreException
   */
  @Override
  public void create(IProgressMonitor monitor) throws CoreException,
      MalformedURLException, SdkException, ClassNotFoundException,
      UnsupportedEncodingException, FileNotFoundException,
      BackingStoreException {

    // always use GWT
    createGaeProject(true);

    IProject project = createProject(monitor);

    /*
     * Refresh contents; if this project was generated via GWT's WebAppCreator,
     * then these files would have been created directly on the file system
     * Although, this refresh should have been done via the project.open() call,
     * which is part of the createProject call above.
     */
    project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

    // Create files
    createFiles(project, monitor);

    // Set all of the natures on the project
    NatureUtils.addNatures(project, getNatureIds());

    // Create the java project
    IJavaProject javaProject = JavaCore.create(project);

    // Create a source folder and add it to the raw classpath
    IResource warFolder = project.findMember(WebAppUtilities.DEFAULT_WAR_DIR_NAME);
    boolean createWarFolders = (warFolder != null);
    IFolder srcFolder = createFolders(project, createWarFolders, monitor);
    IFolder sharedFolder = project.getFolder(ProjectCreationConstants.SHARED_FOLDER_NAME);
    ResourceUtils.createFolderIfNonExistent(sharedFolder, monitor);
    if (createWarFolders) {
      // Set the WAR source/output directory to "/war"
      WebAppUtilities.setDefaultWarSettings(project);

      // Set the default output directory
      WebAppUtilities.setOutputLocationToWebInfClasses(javaProject, monitor);

      /*
       * Copy files into the web-inf lib folder. This code assumes that it is
       * running in a context that has a workspace lock.
       */
      Sdk gwtSdk = getGWTSdk();
      if (gwtSdk != null) {
        new GWTUpdateWebInfFolderCommand(javaProject, gwtSdk).execute();
      }

      Sdk gaeSdk = getGaeSdk();
      if (gaeSdk != null) {
        new AppEngineUpdateWebInfFolderCommand(javaProject, gaeSdk).execute();
      }
    }

    IFolder aptFolder = project.getFolder(ProjectCreationConstants.APT_FOLDER);
    ResourceUtils.createFolderIfNonExistent(aptFolder, monitor);
    setProjectClasspath(javaProject, srcFolder, monitor);
    createLaunchConfig(project);
  }

  /**
   * Create the gae project
   */
  @Override
  protected void createGaeProject(boolean useGwt) throws CoreException,
      FileNotFoundException, UnsupportedEncodingException {

    String justProjectName = getProjectName().replace("-AppEngine", "").replace( //$NON-NLS-N$
        " ", "");
    justProjectName = justProjectName.substring(0, 1).toUpperCase()
        + justProjectName.substring(1);

    Map<String, String> replacements = new HashMap<String, String>();
    replacements.put("@ClientPackageName@", getPackageName() + ".client"); //$NON-NLS-N$
    replacements.put("@ServerPackageName@", getPackageName() + ".server"); //$NON-NLS-N$
    replacements.put("@ClassName@", justProjectName); //$NON-NLS-N$
    replacements.put("@ClassNameLowercase@", justProjectName.toLowerCase());//$NON-NLS-N$
    replacements.put("@PackageName@", getPackageName()); //$NON-NLS-N$

    addFile(
        new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
            + "/WEB-INF/appengine-web.xml"), //$NON-NLS-N$
        ProjectResourceUtils.getResourceAsString(TEMPLATES_DIRECTORY
            + "appengine-web.xml", replacements)); //$NON-NLS-N$

    addFile(
        new Path("src/META-INF/jdoconfig.xml"), //$NON-NLS-N$
        ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
            + "jdoconfig.xml")); //$NON-NLS-N$

    IPath gaeSdkContainerPath = findContainerPath(GaeSdkContainer.CONTAINER_ID);
    if (gaeSdkContainerPath == null) {
      throw new CoreException(new Status(IStatus.ERROR,
          GdtAndroidPlugin.PLUGIN_ID, "Missing GAE SDK container path"));
    }

    GaeSdk gaeSdk = GaePreferences.getSdkManager().findSdkForPath(
        gaeSdkContainerPath);
    if (gaeSdk != null) {
      IPath installationPath = gaeSdk.getInstallationPath();
      File log4jPropertiesFile = installationPath.append(
          "config/user/log4j.properties").toFile(); //$NON-NLS-N$
      if (log4jPropertiesFile.exists()) {
        // Add the log4j.properties file
        addFile(new Path("src/log4j.properties"), new FileInputStream( //$NON-NLS-N$
            log4jPropertiesFile));
      }
      File loggingPropertiesFile = installationPath.append(
          "config/user/logging.properties").toFile(); //$NON-NLS-N$
      if (loggingPropertiesFile.exists()) {
        // Add the logging.properties file
        addFile(new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
            + "/WEB-INF/logging.properties"), new FileInputStream( //$NON-NLS-N$
            loggingPropertiesFile));
      }
    }

    IPath classSourcePath;
    String newName = "";

    if (useGwt) {

      // Add .html
      addFile(
          new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
              + "/" + justProjectName + ".html"), //$NON-NLS-N$
          ProjectResourceUtils.getResourceAsString(TEMPLATES_DIRECTORY
              + "Main.html", replacements)); //$NON-NLS-N$

      addFile(
          new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
              + "/" + justProjectName + ".css"), //$NON-NLS-N$
          ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
              + "Main.css")); //$NON-NLS-N$

      addFile(new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
          + "/dataMessagingToken.txt"), C2dmAuthentication.authToken); //$NON-NLS-N$

      // add favicon.ico (really a .png)
      addFile(
          new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/favicon.ico"), //$NON-NLS-N$
          ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
              + "favicon.ico"));//$NON-NLS-N$

      // Add web.xml
      addFile(
          new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/web.xml"), //$NON-NLS-N$
          ProjectResourceUtils.getResourceAsString(TEMPLATES_DIRECTORY
              + "web.xml", replacements)); //$NON-NLS-N$

      // replacements));
      addFile(
          new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/queue.xml"), //$NON-NLS-N$
          ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
              + "queue.xml")); //$NON-NLS-N$

      String pathString = "src/" //$NON-NLS-N$
          + getPackageName().replace('.', '/').replaceAll(" ", "");

      // add gwt.xml
      String fileNamePrefix = justProjectName.substring(0, 1).toUpperCase()
          + justProjectName.substring(1);
      addFile(
          new Path(pathString + "/" + fileNamePrefix + ".gwt.xml"), //$NON-NLS-N$
          ProjectResourceUtils.getResourceAsString(TEMPLATES_DIRECTORY
              + "gwt.xml", replacements)); //$NON-NLS-N$

      for (String name : SERVER_SOURCE_FILES) {
        classSourcePath = new Path(pathString + "/server/" + name); //$NON-NLS-N$
        addFile(classSourcePath, ProjectResourceUtils.getResourceAsString(
            TEMPLATES_DIRECTORY + name, replacements));
      }

      for (String name : CLIENT_SOURCE_FILES) {
        newName = name.replace("Main", justProjectName); //$NON-NLS-N$
        classSourcePath = new Path(pathString + "/client/" + newName); //$NON-NLS-N$
        addFile(classSourcePath, ProjectResourceUtils.getResourceAsString(
            TEMPLATES_DIRECTORY + name, replacements));
      }
    }

    String sharedClientPathString = ProjectCreationConstants.SHARED_FOLDER_NAME
        + "/" + getPackageName().replace('.', '/').replaceAll(" ", "") //$NON-NLS-N$
        + "/client"; //$NON-NLS-N$
    for (String name : SHARED_CLIENT_FILES) {
      classSourcePath = new Path(sharedClientPathString + "/" + name);
      addFile(classSourcePath, ProjectResourceUtils.getResourceAsString(
          TEMPLATES_DIRECTORY + name, replacements));
    }

    String sharedPathString = ProjectCreationConstants.SHARED_FOLDER_NAME + "/"
        + getPackageName().replace('.', '/').replaceAll(" ", "") + "/shared"; //$NON-NLS-N$
    for (String name : SHARED_SOURCE_FILES) {
      classSourcePath = new Path(sharedPathString + "/" + name);
      addFile(classSourcePath, ProjectResourceUtils.getResourceAsString(
          TEMPLATES_DIRECTORY + name, replacements));
    }

    addFile(
        new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/lib" //$NON-NLS-N$
            + "/json-1.5.jar"), //$NON-NLS-N$
        ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
            + "json-1.5.jar"));//$NON-NLS-N$

    addFile(
        new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/lib" //$NON-NLS-N$
            + "/validation-api-1.0.0.GA.jar"), //$NON-NLS-N$
        ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
            + "validation-api-1.0.0.GA.jar"));//$NON-NLS-N$

    addFile(
        new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/lib" //$NON-NLS-N$
            + "/validation-api-1.0.0.GA-sources.jar"), //$NON-NLS-N$
        ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
            + "validation-api-1.0.0.GA-sources.jar"));//$NON-NLS-N$

    addFile(
        new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/lib" //$NON-NLS-N$
            + "/c2dm-server-src.jar"),//$NON-NLS-N$
        ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
            + "c2dm-server-src.jar"));//$NON-NLS-N$

    addFile(
        new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/lib"//$NON-NLS-N$
            + "/c2dm-server.jar"),//$NON-NLS-N$
        ProjectResourceUtils.getResourceAsStream(TEMPLATES_DIRECTORY
            + "c2dm-server.jar"));//$NON-NLS-N$

    // add prefs for request factory apt generation
    addFile(
        new Path(ProjectCreationConstants.PREFS_DIRECTORY
            + ProjectCreationConstants.JDT_APT_PREFS),
        ProjectResourceUtils.getResourceAsString(TEMPLATES_DIRECTORY
            + ProjectCreationConstants.JDT_APT_PREFS));
    addFile(
        new Path(ProjectCreationConstants.PREFS_DIRECTORY
            + ProjectCreationConstants.JDT_PREFS),
        ProjectResourceUtils.getResourceAsString(TEMPLATES_DIRECTORY
            + ProjectCreationConstants.JDT_PREFS));
    // addfile .factorypath

    String factorypathInfo = "<factorypath>\n "
        + "<factorypathentry kind=\"EXTJAR\" id=\""
        + getGWTSdk().getInstallationPath().toOSString()
        + "/requestfactory-apt.jar\" enabled=\"true\" "
        + "runInBatchMode=\"false\"/>\n</factorypath>";
    addFile(new Path(ProjectCreationConstants.FACTORYPATH_FILE),
        factorypathInfo);
  }

  @Override
  protected void setProjectClasspath(IJavaProject javaProject,
      IFolder srcFolder, IProgressMonitor monitor) throws JavaModelException {
    List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
    classpathEntries.add(JavaCore.newSourceEntry(srcFolder.getFullPath()));

    IProject project = javaProject.getProject();
    IFolder sharedFolder = project.getFolder(ProjectCreationConstants.SHARED_FOLDER_NAME);
    if (sharedFolder.exists()) {
      classpathEntries.add(JavaCore.newSourceEntry(sharedFolder.getFullPath()));
    }
    // Add our container entries to the path
    for (IPath containerPath : getContainerPaths()) {
      classpathEntries.add(JavaCore.newContainerEntry(containerPath));
    }

    classpathEntries.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));

    IFile jarFile = project.getFile(new Path(
        WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/lib" + "/json-1.5.jar")); //$NON-NLS-N$
    classpathEntries.add(JavaCore.newLibraryEntry(jarFile.getFullPath(), null,
        null));
    jarFile = project.getFile(new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
        + "/WEB-INF/lib" + "/validation-api-1.0.0.GA.jar"));//$NON-NLS-N$
    classpathEntries.add(JavaCore.newLibraryEntry(jarFile.getFullPath(), null,
        null));
    jarFile = project.getFile(new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
        + "/WEB-INF/lib" + "/validation-api-1.0.0.GA-sources.jar"));//$NON-NLS-N$
    classpathEntries.add(JavaCore.newLibraryEntry(jarFile.getFullPath(), null,
        null));
    jarFile = project.getFile(new Path(WebAppUtilities.DEFAULT_WAR_DIR_NAME
        + "/WEB-INF/lib" + "/c2dm-server.jar"));//$NON-NLS-N$
    IFile jarSrcFile = project.getFile(new Path(
        WebAppUtilities.DEFAULT_WAR_DIR_NAME + "/WEB-INF/lib"//$NON-NLS-N$
            + "/c2dm-server-src.jar"));//$NON-NLS-N$
    classpathEntries.add(JavaCore.newLibraryEntry(jarFile.getFullPath(),
        jarSrcFile.getFullPath(), null));
    // add .apt_generated to classpath
    IClasspathAttribute[] attributes = new IClasspathAttribute[] {JavaCore.newClasspathAttribute(
        "optional", "true")}; //$NON-NLS-N$
    IFolder aptFolder = project.getFolder(ProjectCreationConstants.APT_FOLDER);
    IClasspathEntry entry = JavaCore.newSourceEntry(aptFolder.getFullPath(),
        ClasspathEntry.INCLUDE_ALL, ClasspathEntry.EXCLUDE_NONE, null,
        attributes); //$NON-NLS-N$
    classpathEntries.add(entry);
    javaProject.setRawClasspath(
        classpathEntries.toArray(new IClasspathEntry[0]), monitor);
  }

}
