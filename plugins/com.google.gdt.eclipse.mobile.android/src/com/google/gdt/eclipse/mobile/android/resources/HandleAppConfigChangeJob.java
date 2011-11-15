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
package com.google.gdt.eclipse.mobile.android.resources;

import static com.google.gdt.eclipse.appengine.rpc.nature.AppEngineConnectedNature.getAndroidProjects;

import com.google.appengine.eclipse.core.resources.GaeProject;
import com.google.gdt.eclipse.core.StatusUtilities;
import com.google.gdt.eclipse.mobile.android.GdtAndroidPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.util.Resources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import java.util.List;

/**
 * Handle modifications to an appengine-web.xml file, propagating changes, as
 * needed to any associated Setup.java files.
 */
@SuppressWarnings("restriction")
public class HandleAppConfigChangeJob extends Job {

  private static final String APP_NAME_FIELD = "APP_NAME";//$NON-NLS-1$
  private static final String JOB_NAME = "Processing AppEngine Web XML change"; //$NON-NLS-1$
  private static final String SETUP_CLASS_NAME = "Setup.java"; //$NON-NLS-1$

  private final IResource resource;

  /**
   * Create an instance that processes changes to the given resource.
   * 
   * @param resource the resource to process
   */
  public HandleAppConfigChangeJob(IResource resource) {
    super(JOB_NAME);
    this.resource = resource;
    setRule(resource);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      processChange(monitor);
      return Status.OK_STATUS;
    } catch (Throwable th) {
      return Status.CANCEL_STATUS;
    }
  }

  /**
   * Find Setup.java in the associated Android project.
   */
  private IResource findSetupFile() {
    List<IProject> projects = getAndroidProjects(getHostProjectName());
    final IResource[] found = new IResource[1];
    for (IProject project : projects) {
      try {
        if (found[0] == null) {
          project.accept(new IResourceProxyVisitor() {
            public boolean visit(IResourceProxy proxy) throws CoreException {
              if (SETUP_CLASS_NAME.equals(proxy.getName())) {
                found[0] = proxy.requestResource();
              }
              return found[0] == null;
            }
          }, IResource.NONE);
        }
      } catch (CoreException e) {
        GdtAndroidPlugin.log(e);
      }
    }
    return found[0];
  }

  /**
   * Get the associated app id.
   */
  private String getAppId() {
    GaeProject appengineProject = GaeProject.create(getHostedProject());
    return appengineProject.getAppId();
  }

  /**
   * Get the host project.
   */
  private IProject getHostedProject() {
    return resource.getProject();
  }

  /**
   * Get the host project's name.
   */
  private String getHostProjectName() {
    return getHostedProject().getName();
  }

  /**
   * Process a change to the config file, propagating changes to the associated
   * android resource (Setup.java) if necessary.
   */
  private void processChange(IProgressMonitor monitor)
      throws MalformedTreeException, BadLocationException, CoreException {
    String appId = getAppId();
    IResource setupFile = findSetupFile();
    updateIfNeeded(appId, setupFile, monitor);
  }

  /**
   * Ensure that the given Setup.java file points to the provided
   * <code>appId</code>.
   */
  private void updateIfNeeded(String appId, IResource setupFile,
      IProgressMonitor monitor) throws MalformedTreeException,
      BadLocationException, CoreException {
    ICompilationUnit cu = (ICompilationUnit) JavaCore.create(setupFile);
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(cu);
    CompilationUnit astRoot = (CompilationUnit) parser.createAST(monitor);
    for (Object td : astRoot.types()) {
      FieldDeclaration[] fields = ((TypeDeclaration) td).getFields();
      for (FieldDeclaration field : fields) {
        for (Object fragment : field.fragments()) {
          VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
          if (APP_NAME_FIELD.equals(vdf.getName().getFullyQualifiedName())) {
            Expression initializer = vdf.getInitializer();
            if (initializer.getNodeType() == ASTNode.STRING_LITERAL) {
              StringLiteral stringLit = (StringLiteral) initializer;
              if (!appId.equals(stringLit.getLiteralValue())) {
                astRoot.recordModifications();
                stringLit.setLiteralValue(appId);

                String source = cu.getSource();
                Document document = new Document(source);
                TextEdit edit = astRoot.rewrite(document,
                    cu.getJavaProject().getOptions(true));
                edit.apply(document);

                String newSource = document.get();
                cu.getBuffer().setContents(newSource);

                IStatus status = Resources.makeCommittable(cu.getResource(),
                    null);
                if (!status.isOK()) {
                  throw new CoreException(StatusUtilities.newErrorStatus(
                      "App Id reconciliation: unable to apply edit to file ",//$NON-NLS-1$
                      GdtAndroidPlugin.PLUGIN_ID));
                }
                // TODO: the updated file is left dirty; we should review
                // whether this is ultimately the desired behavior
                cu.applyTextEdit(edit, monitor);
                cu.reconcile(ICompilationUnit.NO_AST, false, null, monitor);
                cu.commitWorkingCopy(true, null);
                cu.discardWorkingCopy();
              }
            }
          }
        }
      }
    }
  }

}
