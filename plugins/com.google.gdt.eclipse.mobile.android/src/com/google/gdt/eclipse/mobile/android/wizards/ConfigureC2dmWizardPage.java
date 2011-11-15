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
package com.google.gdt.eclipse.mobile.android.wizards;

import com.google.gdt.eclipse.core.browser.BrowserUtilities;
import com.google.gdt.eclipse.mobile.android.GdtAndroidPlugin;
import com.google.gdt.eclipse.mobile.android.wizards.helpers.C2dmAuthentication;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wizard Page for specifying C2DM configuration information
 */
public class ConfigureC2dmWizardPage extends WizardPage {
  private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"; //$NON-NLS-1$
  private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
  
  private String emailId = ""; //$NON-NLS-1$
  private NewAndroidCloudProjectWizardPage firstPage;
  
  private Text packageNameText;

  private String passwd  = ""; //$NON-NLS-1$

  private Text passwdText;
  private Text roleAccountEmailText;

  /**
   * Wizard Page to configure C2DM parameters
   */
  public ConfigureC2dmWizardPage(NewAndroidCloudProjectWizardPage page) {
    super("New App Engine Connected Android Project");
    setTitle("Configure C2DM");
    setMessage("Enter C2DM information used by the application");
    firstPage = page;
  }

  /**
   * Create contents of the wizard.
   * 
   * @param parent
   */
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);

    final Composite container = new Composite(parent, SWT.NULL);
    container.setFont(parent.getFont());
    container.setLayout(new GridLayout());
    container.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    createInfoSection(container);
    @SuppressWarnings("unused")
    Label fillerLabel = new Label(container, SWT.NONE);
    createFields(container);
    setControl(container);
    
    readC2dmPropertiesFile();
  }

  public String getApplicationPackageName() {
    return packageNameText.getText().trim();
  }

  public String getEmailId() {
    return roleAccountEmailText.getText().trim();
  }

  public String getPasswd() {
    return passwdText.getText().trim();
  }

  public void performFinish(IProgressMonitor monitor)
      throws InterruptedException {
    monitor.beginTask("Authenticating C2DM service", 1);
    C2dmAuthentication.getAuth(emailId, passwd);
    if (C2dmAuthentication.authToken.length() == 0) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          setErrorMessage("C2DM Authentication failed. Verify role account email address and password");
          setPageComplete(false);
          roleAccountEmailText.setFocus();
        }
      });
      throw new InterruptedException("C2DM authentication failed");
    }
    monitor.done();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible) {
      packageNameText.setText(firstPage.getAndroidPackageName());
      roleAccountEmailText.setFocus();
      validatePageComplete();
      setErrorMessage(null);
    }
  }


  public boolean validCredentials() {
    return validEmail() && passwd != null;
  }

  /**
   * @param container
   */
  private void createFields(final Composite container) {
    Composite fieldGroup = new Composite(container, SWT.NONE);
    fieldGroup.setLayout(new GridLayout(2, false));
    fieldGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    Label nameLabel = new Label(fieldGroup, SWT.NONE);
    nameLabel.setText("Package name:");
    nameLabel.setToolTipText("The package name of your application");
    packageNameText = new Text(fieldGroup, SWT.BORDER);
    packageNameText.setToolTipText("The package name of your application");
    packageNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
        false, 1, 1));
    packageNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validatePageComplete();
      }
    });

    Label emailLabel = new Label(fieldGroup, SWT.NONE);
    emailLabel.setText("Role account email address:");
    emailLabel.setToolTipText("Google Account ID that will be used for sending messages to C2DM");
    roleAccountEmailText = new Text(fieldGroup, SWT.BORDER);
    roleAccountEmailText.setToolTipText("Google Account ID that will be used for sending messages to C2DM");
    roleAccountEmailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
        false, 1, 1));
    roleAccountEmailText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        emailId = roleAccountEmailText.getText().trim();
        validatePageComplete();
      }
    });

    Label pswdLabel = new Label(fieldGroup, SWT.NONE);
    pswdLabel.setText("Role account password:");
    pswdLabel.setToolTipText("Password for role account email address");
    passwdText = new Text(fieldGroup, SWT.BORDER | SWT.PASSWORD);
    passwdText.setToolTipText("Password for role account email address");
    passwdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
        1));
    passwdText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        passwd = passwdText.getText().trim();
        validatePageComplete();
      }
    });

    Link link = new Link(container, SWT.WRAP);
    GridData linkData = new GridData(GridData.FILL_HORIZONTAL);
    linkData.widthHint = 200;
    link.setLayoutData(linkData);
    link.setText("We do not store your email address or password. Check out this <a HREF=\" \">documentation</a> "
        + "for information regarding how these credentials are used.");
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        launchUrl("http://code.google.com/android/c2dm/index.html#lifecycle");
      }
    });
  }

  /**
   * @param container
   */
  private void createInfoSection(final Composite container) {
    Link link = new Link(container, SWT.WRAP);
    link.setText("Android Cloud to Device Messaging (C2DM) is a service that helps"
        + " developers send data from servers to their applications on Android devices. "
        + "If you haven't already done so, you'll need to visit the C2DM "
        + "<a HREF=\"http://code.google.com/android/c2dm/signup.html\">sign up page</a> "
        + "to start using C2DM with your Android applications. "
        + "Once completed enter the following information below.");
    GridData linkData = new GridData(GridData.FILL_HORIZONTAL);
    linkData.widthHint = 200;
    link.setLayoutData(linkData);
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        launchUrl(e.text);
      }
    });
  }

  private void launchUrl(String urlText) {
    try {
      BrowserUtilities.launchBrowser(urlText);
    } catch (PartInitException e) {
      GdtAndroidPlugin.log(e);
    } catch (MalformedURLException e) {
      GdtAndroidPlugin.log(e);
    }
  }

  private void readC2dmPropertiesFile() {
    String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
    File file = new File(homeDir, "c2dm.properties"); //$NON-NLS-1$
    if (!(file.exists() && file.isFile() && file.canRead())) {
      return;
    }
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(file));
    } catch (IOException e) {
      // ignore
    }
    String id = properties.getProperty("user"); //$NON-NLS-1$
    if (id != null) {
      roleAccountEmailText.setText(id);
    }
    String passwdProperty = properties.getProperty("passwd"); //$NON-NLS-1$
    if (passwdProperty != null) {
      passwdText.setText(passwdProperty);
    }    
  }

  private boolean validatePackageField() {
    String packageFieldContents = packageNameText.getText().trim();
    // Validate package field
    if (packageFieldContents.length() == 0) {
      setMessage("Package name must be specified.");
      return false;
    }

    // Check it's a valid package string
    IStatus status = JavaConventions.validatePackageName(packageFieldContents,
        "1.5", "1.5"); //$NON-NLS-1$ $NON-NLS-2$
    if (!status.isOK()) {
      if (status.getSeverity() == IStatus.ERROR) {
        setErrorMessage(status.getMessage());
      } else {
        setErrorMessage(null);
        setMessage(status.getMessage());
      }
      return false;
    }

    if (packageFieldContents.indexOf('.') == -1) {
      setErrorMessage("Package name must have at least two identifiers.");
      return false;
    }
    setErrorMessage(null);
    return true;
  }

  /**
   * Validates the page and updates the Next/Finish buttons
   */
  private void validatePageComplete() {
    boolean complete = validatePackageField() && emailId != null
        && passwd != null;

    if (!validEmail()) {
      complete = false;
      setErrorMessage("\'" + emailId + "\'" + " is not a valid email address.");
    } else {
      setErrorMessage(null);
    }

    if (complete) {
      firstPage.setPackageName(packageNameText.getText().trim());
    }
    setPageComplete(complete);
  }

  private boolean validEmail() {
    if (emailId == null) {
      return false;
    }
    Matcher matcher = pattern.matcher(emailId);
    return matcher.matches();
  }
}
