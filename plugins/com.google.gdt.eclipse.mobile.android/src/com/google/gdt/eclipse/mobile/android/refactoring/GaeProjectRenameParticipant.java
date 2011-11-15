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
package com.google.gdt.eclipse.mobile.android.refactoring;

import static com.google.gdt.eclipse.appengine.rpc.nature.AppEngineConnectedNature.getAndroidProjects;

import com.google.gdt.eclipse.mobile.android.GdtAndroidPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Watches for renames of the Gae Project and updates the reference in the
 * preferences
 */
public class GaeProjectRenameParticipant extends RenameParticipant {

  String oldProjectName;

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
   * checkConditions(org.eclipse.core.runtime.IProgressMonitor,
   * org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
   */
  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm,
      CheckConditionsContext context) throws OperationCanceledException {
    return new RefactoringStatus();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
   * createChange(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {

    final String newName = getArguments().getNewName();
    final HashMap<IFile, TextFileChange> changes = new HashMap<IFile, TextFileChange>();
    String[] fileNamePatterns = {GdtAndroidPlugin.PLUGIN_ID + ".prefs"}; //$NON-NLS-N$
    Pattern pattern = Pattern.compile(oldProjectName);

    TextSearchRequestor collector = new TextSearchRequestor() {
      public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess)
          throws CoreException {
        IFile file = matchAccess.getFile();
        TextFileChange change = (TextFileChange) changes.get(file);
        if (change == null) {
          TextChange textChange = getTextChange(file); // an other participant
                                                       // already modified that
                                                       // file?
          if (textChange != null) {
            return false; // don't try to merge changes
          }
          change = new TextFileChange(file.getName(), file);
          change.setEdit(new MultiTextEdit());
          changes.put(file, change);
        }
        ReplaceEdit edit = new ReplaceEdit(matchAccess.getMatchOffset(),
            matchAccess.getMatchLength(), newName);
        change.addEdit(edit);
        change.addTextEditGroup(new TextEditGroup("Update type reference", edit)); //$NON-NLS-1$
        return true;
      }
    };

    for (IProject aproject : getAndroidProjects(oldProjectName)) {
      IResource[] roots = {aproject}; // limit to the current project
      FileTextSearchScope scope = FileTextSearchScope.newSearchScope(roots,
          fileNamePatterns, false);
      TextSearchEngine.create().search(scope, collector, pattern, pm);
    }

    if (changes.isEmpty())
      return null;

    CompositeChange result = new CompositeChange(
        "Project rename preference updates"); //$NON-NLS-1$
    for (Iterator<TextFileChange> iter = changes.values().iterator(); iter.hasNext();) {
      result.add(iter.next());
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName
   * ()
   */
  @Override
  public String getName() {
    return "Renames references in .settings file";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize
   * (java.lang.Object)
   */
  @Override
  protected boolean initialize(Object element) {
    IProject project = (IProject) element;
    oldProjectName = project.getName();
    return true;
  }



}
