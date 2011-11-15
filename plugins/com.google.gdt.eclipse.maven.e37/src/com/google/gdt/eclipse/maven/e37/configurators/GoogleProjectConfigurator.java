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
package com.google.gdt.eclipse.maven.e37.configurators;

import com.google.appengine.eclipse.core.nature.GaeNature;
import com.google.gdt.eclipse.core.StringUtilities;
import com.google.gdt.eclipse.core.properties.WebAppProjectProperties;
import com.google.gdt.eclipse.maven.Activator;
import com.google.gdt.eclipse.maven.sdk.GWTMavenRuntime;
import com.google.gwt.eclipse.core.nature.GWTNature;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * M2Eclipse project configuration extension that configures a project to get
 * the Google GWT/GAE project nature.
 * 
 * NOTE: Do not access this class from outside of the configurators package. All
 * classes in the configurators package have dependencies on plugins that may or
 * may not be present in the user's installation. As long as these classes are
 * only invoked through m2Eclipe's extension points, other parts of this plugin
 * can be used without requiring the m2Eclipse dependencies.
 */
public class GoogleProjectConfigurator extends
    AbstractGoogleProjectConfigurator {

  private static final String AJDT_NATURE = "org.eclipse.ajdt.ui.ajnature";
  private static final List<String> GAE_UNPACK_GOAL = Arrays.asList(new String[] {"net.kindleit:maven-gae-plugin:unpack"});
  private static final Map<String, String> JAVA_BUILDER_ARG_MAP;
  // Assume true; only applies for AJDT check below
  private static boolean isAtLeastSTS251 = true;

  static {
    JAVA_BUILDER_ARG_MAP = new HashMap<String, String>();
    JAVA_BUILDER_ARG_MAP.put(
        "org.eclipse.jdt.core.compiler.generateClassFiles", "false");

    IProduct product = Platform.getProduct();

    // Assume true; only applies for AJDT check below
    isAtLeastSTS251 = true;

    if (product.getId().startsWith("com.springsource.sts.ide")) {
      String versionStr = product.getDefiningBundle().getHeaders().get(
          Constants.BUNDLE_VERSION);
      if (new Version(versionStr).compareTo(new Version("2.5.1")) < 0) {
        isAtLeastSTS251 = false;
      }
    }
  }

  /**
   * Add a non-class-file-generating Java Builder to the top of the build spec.
   * Remove any other Java builders that are part of the project.
   * 
   * Before STS 2.5.1, JavaCompilationParticipants were not called as part of
   * the AspectJ build process. In addition, AspectJ projects do not have the
   * Java Builder as part of their build spec. In this case, we need to add the
   * Java Builder in order to trigger our CompilationParticipants, which are
   * used for validation.
   */
  private static void addNonClassFileJavaBuilder(IProject project,
      IProgressMonitor monitor) throws CoreException {
    IProjectDescription description = project.getDescription();
    List<ICommand> builders = new ArrayList<ICommand>(
        Arrays.asList(description.getBuildSpec()));

    /*
     * Remove any existing Java Builders. There shouldn't be any on an AJDT
     * project, anyway. However, this call prevents us from adding more and more
     * Java builders to the project every time the user requests a configuration
     * update for their Maven project.
     */
    for (int i = 0, size = builders.size(); i < size; i++) {
      ICommand curBuilder = builders.get(i);
      if (curBuilder.getBuilderName().equals(JavaCore.BUILDER_ID)) {
        builders.remove(i);
        i--;
        size--;
      }
    }

    ICommand newBuilder = description.newCommand();
    newBuilder.setBuilderName(JavaCore.BUILDER_ID);

    /*
     * We need to prevent the Java Builder from generating class files,
     * otherwise the AspectJ plugin will remove the Java Builder, even if we add
     * it explicitly here.
     */
    newBuilder.setArguments(JAVA_BUILDER_ARG_MAP);

    /*
     * Add the Java builder to the top of the build spec. We add it to the top
     * because we don't the Java Builder to wipe out IDT-generated class files
     * that AspectJ creates.
     */

    builders.add(0, newBuilder);
    description.setBuildSpec(builders.toArray(new ICommand[builders.size()]));
    project.setDescription(description, monitor);
  }

  /**
   * Remove any non-class-file generating Java Builders from the build spec. The
   * assumption is that such builders on GWT and/or GAE projects were added by
   * GPE. See {@link #addNonClassFileJavaBuilder(IProject, IProgressMonitor) for
   * more information.
   * 
   * In STS 2.5.1+, the AspectJ builder has been modified to call
   * CompilationParticipants. We no longer need the Java Builder that we
   * previously added; in fact, adding such a builder will cause more work to be
   * done than necessary.
   */
  private static void removeNonClassFileJavaBuilders(IProject project,
      IProgressMonitor monitor) throws CoreException {
    IProjectDescription description = project.getDescription();
    List<ICommand> builders = new ArrayList<ICommand>(
        Arrays.asList(description.getBuildSpec()));
    boolean buildersRemoved = false;

    for (int i = 0, size = builders.size(); i < size; i++) {
      ICommand curBuilder = builders.get(i);
      if (curBuilder.getBuilderName().equals(JavaCore.BUILDER_ID)
          && curBuilder.getArguments().equals(JAVA_BUILDER_ARG_MAP)) {
        buildersRemoved = true;
        builders.remove(i);
        i--;
        size--;
      }
    }

    if (buildersRemoved) {
      description.setBuildSpec(builders.toArray(new ICommand[builders.size()]));
      project.setDescription(description, monitor);
    }
  }

  @Override
  protected void doConfigure(final MavenProject mavenProject, IProject project,
      ProjectConfigurationRequest request, final IProgressMonitor monitor)
      throws CoreException {

    final IMaven maven = MavenPlugin.getDefault().getMaven();

    boolean configureGaeNatureSuccess = configureNature(project, mavenProject,
        GaeNature.NATURE_ID, true, new NatureCallbackAdapter() {

          @Override
          public void beforeAddingNature() {
            try {
              DefaultMavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
              executionRequest.setBaseDirectory(mavenProject.getBasedir());
              executionRequest.setLocalRepository(maven.getLocalRepository());
              executionRequest.setRemoteRepositories(mavenProject.getRemoteArtifactRepositories());
              executionRequest.setPluginArtifactRepositories(mavenProject.getPluginArtifactRepositories());
              executionRequest.setPom(mavenProject.getFile());
              executionRequest.setGoals(GAE_UNPACK_GOAL);

              MavenExecutionResult result = maven.execute(executionRequest,
                  monitor);
              if (result.hasExceptions()) {
                Activator.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        "Error configuring project",
                        result.getExceptions().get(0)));
              }
            } catch (CoreException e) {
              Activator.getDefault().getLog().log(
                  new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                      "Error configuring project", e));
            }
          }
        }, monitor);

    boolean configureGWTNatureSuccess = configureNature(project, mavenProject,
        GWTNature.NATURE_ID, true, new NatureCallbackAdapter() {

          @Override
          public void beforeAddingNature() {

            // Get the GWT version from the project pom
            String gwtVersion = null;
            List<Dependency> dependencies = mavenProject.getDependencies();
            for (Dependency dependency : dependencies) {
              if (GWTMavenRuntime.MAVEN_GWT_GROUP_ID.equals(dependency.getGroupId())
                  && (GWTMavenRuntime.MAVEN_GWT_USER_ARTIFACT_ID.equals(dependency.getArtifactId()) || GWTMavenRuntime.MAVEN_GWT_SERVLET_ARTIFACT_ID.equals(dependency.getArtifactId()))) {
                gwtVersion = dependency.getVersion();
                break;
              }
            }

            // Check that the pom.xml has GWT dependencies
            if (!StringUtilities.isEmpty(gwtVersion)) {
              try {
                /*
                 * Download and install the gwt-dev.jar into the local
                 * repository.
                 */
                maven.resolve(GWTMavenRuntime.MAVEN_GWT_GROUP_ID,
                    GWTMavenRuntime.MAVEN_GWT_DEV_JAR_ARTIFACT_ID, gwtVersion,
                    "jar", null, mavenProject.getRemoteArtifactRepositories(),
                    monitor);
              } catch (CoreException e) {
                Activator.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        "Error configuring project", e));
              }
            }
          }
        }, monitor);

    if (configureGWTNatureSuccess || configureGaeNatureSuccess) {
      try {
        // Add GWT Web Application configuration parameters
        WebAppProjectProperties.setWarSrcDir(project, new Path(
            "src/main/webapp"));
        WebAppProjectProperties.setWarSrcDirIsOutput(project, false);

        String artifactId = mavenProject.getArtifactId();
        String version = mavenProject.getVersion();
        IPath location = (project.getRawLocation() != null
            ? project.getRawLocation() : project.getLocation());
        if (location != null && artifactId != null && version != null) {
          WebAppProjectProperties.setLastUsedWarOutLocation(project,
              location.append("target").append(artifactId + "-" + version));
        }
      } catch (BackingStoreException be) {
        Activator.getDefault().getLog().log(
            new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                "Error configuring project", be));
      }

      /*
       * If this project is using AspectJ, then we may either need to add the
       * Java Builder to the project, or remove the Java Builder from the
       * proejct, depending on what version of STS the user is running.
       * 
       * Note that we're assuming that our configurator runs after AJDT's
       * configurator. This is true, because we've set our configurator's
       * priority (see the plugin.xml file) to be lower than that of AJDT's
       * configurator.
       */
      if (hasProjectNature(mavenProject, project, AJDT_NATURE)) {
        if (!isAtLeastSTS251) {
          addNonClassFileJavaBuilder(project, monitor);
        } else {
          removeNonClassFileJavaBuilders(project, monitor);
        }
      }
    }
  }

}
