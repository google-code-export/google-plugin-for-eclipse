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
package com.google.appengine.eclipse.core.properties.ui;

import com.google.appengine.eclipse.core.AppEngineCorePluginLog;
import com.google.appengine.eclipse.core.datatools.SqlConnectionExtensionPopulator;
import com.google.appengine.eclipse.core.properties.GoogleCloudSqlProperties;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Dialog box to configure the Google Cloud SQL Service for test or prod.
 */
public class GoogleCloudSqlConfigure extends Dialog {

  private static final int TEXT_WIDTH = 220;

  private Boolean isProd;
  private IProject project;
  private Label error;
  private String errorText;
  private Text instanceName;
  private Text databaseName;
  private Text databaseUser;
  private Text databasePassword;

  public GoogleCloudSqlConfigure(IShellProvider parentShell, IProject project, Boolean isProd) {
    super(parentShell);
    this.project = project;
    this.isProd = isProd;
  }

  public GoogleCloudSqlConfigure(Shell parentShell, IProject project, Boolean isProd) {
    super(parentShell);
    this.project = project;
    this.isProd = isProd;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#create()
   */
  @Override
  public void create() {
    super.create();
    getShell().setText("Configure Google Cloud SQL");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
   * .Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = SWTFactory.createComposite(
        (Composite) super.createDialogArea(parent), 2, 1, SWT.HORIZONTAL);
    addControls(composite);
    addEventHandlers();
    initializeControls();
    return composite;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {
    Text incorrect = validateFields();
    if (incorrect != null) {
      error.setText(errorText);
      error.setVisible(true);
      incorrect.setFocus();
      incorrect.selectAll();
      return;
    }
    try {
      if (isProd) {
        GoogleCloudSqlProperties.setProdDatabaseName(project, databaseName.getText().trim());
        GoogleCloudSqlProperties.setProdDatabasePassword(project, databasePassword.getText().trim());
        GoogleCloudSqlProperties.setProdDatabaseUser(project, databaseUser.getText().trim());
        GoogleCloudSqlProperties.setProdInstanceName(project, instanceName.getText().trim());
        SqlConnectionExtensionPopulator.populateCloudSQLBridgeExtender(project,
            SqlConnectionExtensionPopulator.ConnectionType.CONNECTION_TYPE_PROD);
      } else {
        GoogleCloudSqlProperties.setTestDatabaseName(project, databaseName.getText().trim());
        GoogleCloudSqlProperties.setTestDatabasePassword(project, databasePassword.getText().trim());
        GoogleCloudSqlProperties.setTestDatabaseUser(project, databaseUser.getText().trim());
        GoogleCloudSqlProperties.setTestInstanceName(project, instanceName.getText().trim());
        SqlConnectionExtensionPopulator.populateCloudSQLBridgeExtender(project,
            SqlConnectionExtensionPopulator.ConnectionType.CONNECTION_TYPE_TEST);
      }
    } catch (BackingStoreException e) {
      AppEngineCorePluginLog.logError(e, "Unable to store Google SQL Service configurations");
    }
    super.okPressed();
  }

  private void addControls(Composite composite) {

    Composite errorComposite = SWTFactory.createComposite(composite, 1, 2, SWT.HORIZONTAL);
    error = new Label(errorComposite, SWT.NONE);
    error.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    error.setVisible(false);
    Label instanceNameLabel = new Label(composite, SWT.NONE);
    instanceNameLabel.setText("Instance name");
    instanceNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    instanceName = new Text(composite, SWT.BORDER);
    instanceName.setLayoutData(new GridData(TEXT_WIDTH, SWT.DEFAULT));
    Label databaseNameLabel = new Label(composite, SWT.NONE);
    databaseNameLabel.setText("Database name");
    databaseNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    databaseName = new Text(composite, SWT.BORDER);
    databaseName.setLayoutData(new GridData(TEXT_WIDTH, SWT.DEFAULT));
    Label databaseUserLabel = new Label(composite, SWT.NONE);
    databaseUserLabel.setText("Database username");
    databaseUserLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    databaseUser = new Text(composite, SWT.BORDER);
    databaseUser.setLayoutData(new GridData(TEXT_WIDTH, SWT.DEFAULT));
    Label databasePasswordLabel = new Label(composite, SWT.NONE);
    databasePasswordLabel.setText("Database password");
    databasePasswordLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    databasePassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
    databasePassword.setLayoutData(new GridData(TEXT_WIDTH, SWT.DEFAULT));
  }

  private void addEventHandlers() {
    instanceName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        fieldsChanged();
      }
    });
    databaseName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        fieldsChanged();
      }
    });
    databaseUser.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        fieldsChanged();
      }
    });
    databasePassword.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        fieldsChanged();
      }
    });
  }

  private void fieldsChanged() {
    if (validateFields() != null) {
      error.setText(errorText);
      error.setVisible(true);
    } else {
      error.setVisible(false);
    }
  }

  private void initializeControls() {
    if (isProd) {
      instanceName.setText(GoogleCloudSqlProperties.getProdInstanceName(project));
      databaseName.setText(GoogleCloudSqlProperties.getProdDatabaseName(project));
      databaseUser.setText(GoogleCloudSqlProperties.getProdDatabaseUser(project));
      databasePassword.setText(GoogleCloudSqlProperties.getProdDatabasePassword(project));
    } else {
      instanceName.setText(GoogleCloudSqlProperties.getTestInstanceName(project));
      databaseName.setText(GoogleCloudSqlProperties.getTestDatabaseName(project));
      databaseUser.setText(GoogleCloudSqlProperties.getTestDatabaseUser(project));
      databasePassword.setText(GoogleCloudSqlProperties.getTestDatabasePassword(project));
    }
  }

  private Text validateFields() {
    Text returnText = null;
    errorText = "";
    if (instanceName.getText().trim().equals("")) {
      errorText = "Enter instance name";
      returnText = instanceName;
    } else if (databaseName.getText().trim().equals("")) {
      errorText = "Enter database name";
      returnText = databaseName;
    }
    return returnText;
  }

}
