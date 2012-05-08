// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.gdt.eclipse.managedapis.ui;

import com.google.appengine.eclipse.core.nature.GaeNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Utility for error dialog popup in case the project is not an App Engine
 * project.
 */
public class AppEngineCheckDialog {
  public static boolean isAppEngineProject(IProject project) {
    if (!GaeNature.isGaeProject(project)) {
      MessageDialog.openInformation(Display.getDefault().getActiveShell(),
          "Google Plugin for Eclipse",
          "Project " + project.getName() + " is not an App Engine Project.");
      return false;
    }
    return true;
  }

}
