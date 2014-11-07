/*******************************************************************************
 * Copyright (c) 2011 HPCC Systems.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     HPCC Systems - initial API and implementation
 ******************************************************************************/
package org.hpccsystems.eclide.launchers;

import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.AuthenticationEvent;
import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.hpccsystems.eclide.Activator;
import org.hpccsystems.eclide.builder.ECLCompiler;
import org.hpccsystems.eclide.resources.Messages;
import org.hpccsystems.internal.ECLLaunchConfigurationTab;
import org.hpccsystems.internal.data.ClientTools;
import org.hpccsystems.internal.data.LauncherPlatform;

@SuppressWarnings("restriction")
public class ECLLaunchServerTab extends ECLLaunchConfigurationTab {

	private class WidgetListener extends SelectionAdapter implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			scheduleUpdateJob();
			Object source= e.getSource();
			if (source == fIPText ||
				source == fPortText) {
				refreshAddress();
			} else if (source == fAddressText) {
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source= e.getSource();
			if (source == sslButton) {
				if (sslButton.getSelection() && fPortText.getText().matches(LauncherPlatform.P_PORT_DEFAULT_STR)) {
					fPortText.setText(LauncherPlatform.P_SSLPORT_DEFAULT_STR);
				} else if (!sslButton.getSelection() && fPortText.getText().matches(LauncherPlatform.P_SSLPORT_DEFAULT_STR)) {
					fPortText.setText(LauncherPlatform.P_PORT_DEFAULT_STR);
				}
				refreshAddress();
			}
			if (source == testButton) {
				refreshServerVersion();
				refreshBrowser();
			} else if (source == disableButton) {
				scheduleUpdateJob();
			} else if (source == compileOnlyButton) {
				scheduleUpdateJob();
			}
		}
	}

	private WidgetListener fListener;
	private ILaunchConfiguration testConfig;

	Image image;

	private Button disableButton;
	protected Button sslButton;
	protected Text fIPText;
	protected Text fPortText;
	protected Text fClusterText;
	protected Button compileOnlyButton;

	protected Text fUserText;
	protected Text fPasswordText;

	protected Text fAddressText;
	protected String fCompilerPath;

	protected Text fServerVersionText;
	protected Text fCompilerVersionText;

	private Button testButton;
	private Browser browser;
	
	private static String DisableButtonText = Messages.DisableServer; 
	private static String DisableButtonTempDiabledText = DisableButtonText + Messages.TemporarilyDisabled; 
	
	ECLLaunchServerTab() {
		fListener = new WidgetListener();
		fCompilerPath = new String();
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		testConfig = launchConfig;
		return super.isValid(launchConfig);
	}

	protected void createServerEditor(Composite parent) {
		disableButton = SWTFactory.createCheckButton(parent, DisableButtonText, null, false, 1);
		disableButton.addSelectionListener(fListener);

		Group group = SWTFactory.createGroup(parent, Messages.Server, 2, 1, GridData.FILL_HORIZONTAL);
		sslButton = SWTFactory.createCheckButton(group, Messages.SSL, null, false, 2);
		sslButton.addSelectionListener(fListener);

		SWTFactory.createLabel(group, Messages.IP_Address, 1);
		fIPText = SWTFactory.createSingleText(group, 1);
		fIPText.addModifyListener(fListener);

		SWTFactory.createLabel(group, Messages.Port, 1);
		fPortText = SWTFactory.createSingleText(group, 1);
		fPortText.addModifyListener(fListener);

		SWTFactory.createLabel(group, Messages.Target, 1);
		fClusterText = SWTFactory.createSingleText(group, 1);
		fClusterText.addModifyListener(fListener);
		
		compileOnlyButton = SWTFactory.createCheckButton(group, Messages.CompileOnly, null, false, 1);
		compileOnlyButton.addSelectionListener(fListener);
	}

	protected void createCredentialsEditor(Composite parent) {
		Group group = SWTFactory.createGroup(parent, Messages.Credentials, 2, 1, GridData.FILL_HORIZONTAL);
		SWTFactory.createLabel(group, Messages.User, 1);
		fUserText = SWTFactory.createSingleText(group, 1);
		fUserText.addModifyListener(fListener);

		SWTFactory.createLabel(group, Messages.Password, 1);
		fPasswordText = SWTFactory.createText(group, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD, 1);
		fPasswordText.addModifyListener(fListener);
	}

	protected Browser createBrowser(final Composite parent) {
		final Group group = SWTFactory.createGroup(parent, Messages.EclWatch, 3, 1, GridData.FILL_BOTH);
		SWTFactory.createLabel(group, Messages.Address, 1);
		fAddressText = SWTFactory.createSingleText(group, 1);
		fAddressText.addModifyListener(fListener);
		testButton = SWTFactory.createPushButton(group, Messages.Test, null);
		testButton.addSelectionListener(fListener);
		SWTFactory.createLabel(group, Messages.ServerVersion, 1);
		fServerVersionText = SWTFactory.createText(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY, 2);
		SWTFactory.createLabel(group, Messages.CompilerVersion, 1);
		fCompilerVersionText = SWTFactory.createText(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY, 2);

		try {
			browser = new Browser(group, SWT.BORDER);
		} catch (IllegalArgumentException e) {
			browser = null;
			org.hpccsystems.eclide.Activator.log(Messages.Error_BrowserControl, e);
		} catch (SWTException e) {
			browser = null;
			org.hpccsystems.eclide.Activator.log(Messages.Error_BrowserControl, e);
		} catch (SWTError e) {
			browser = null;
			org.hpccsystems.eclide.Activator.log(Messages.Error_BrowserControl);
		}
		if (browser != null) {
			browser.setUrl("about:blank"); //$NON-NLS-1$
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.horizontalSpan = 3;
			browser.setLayoutData(gd);
			browser.addCloseWindowListener(new CloseWindowListener() {
				@Override
				public void close(WindowEvent event) {
					group.dispose();
					final Browser newBrowser = createBrowser(parent);
					parent.pack();
					refreshAddress();
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							newBrowser.setText(Messages.Error_AuthenticationFailed);
						}
					});
				}
			});
			browser.addAuthenticationListener(new AuthenticationListener() {
				@Override
				public void authenticate(AuthenticationEvent event) {
					// TODO Auto-generated method stub
					event.user = fUserText.getText();
					event.password = fPasswordText.getText();
				}
			});
		} else {
			SWTFactory.createLabel(group, Messages.Warning_WebBrowser_Creation_Failed, 3);
		}
		return browser;
	}

	@Override
	public final void createControl(Composite parent) {
		Composite projComp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH); 
		((GridLayout)projComp.getLayout()).verticalSpacing = 0;

		createVerticalSpacer(projComp, 1);
		createServerEditor(projComp);
		createCredentialsEditor(projComp);		
		createVerticalSpacer(projComp, 1);
		createBrowser(projComp);
		setControl(projComp);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			disableButton.setSelection(configuration.getAttribute(LauncherPlatform.P_DISABLED, false));

			sslButton.setSelection(configuration.getAttribute(LauncherPlatform.P_SSL, LauncherPlatform.P_SSL_DEFAULT));
			fIPText.setText(configuration.getAttribute(LauncherPlatform.P_IP, LauncherPlatform.P_IP_DEFAULT));
			fPortText.setText(Integer.toString(configuration.getAttribute(LauncherPlatform.P_PORT, LauncherPlatform.P_PORT_DEFAULT)));
			fClusterText.setText(configuration.getAttribute(LauncherPlatform.P_CLUSTER, "hthor")); //$NON-NLS-1$
			compileOnlyButton.setSelection(configuration.getAttribute(LauncherPlatform.P_COMPILEONLY, false));

			fUserText.setText(configuration.getAttribute(LauncherPlatform.P_USER, "")); //$NON-NLS-1$
			fPasswordText.setText(configuration.getAttribute(LauncherPlatform.P_PASSWORD, "")); //$NON-NLS-1$

			int port = LauncherPlatform.P_PORT_DEFAULT;
			try {
				port = new Integer(fPortText.getText());
			} catch (NumberFormatException e) {
			}
			LauncherPlatform platform = LauncherPlatform.get(sslButton.getSelection(), fIPText.getText(), port);
			if (platform.isDisabled()) {
				fServerVersionText.setText(Messages.UnableToConnect);
				disableButton.setText(DisableButtonTempDiabledText);
				fCompilerVersionText.setText(""); //$NON-NLS-1$
			} else {
				fServerVersionText.setText(""); //$NON-NLS-1$
				fCompilerVersionText.setText(""); //$NON-NLS-1$
				disableButton.setText(DisableButtonText);
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LauncherPlatform.P_DISABLED, disableButton.getSelection());

		configuration.setAttribute(LauncherPlatform.P_SSL, sslButton.getSelection());
		configuration.setAttribute(LauncherPlatform.P_IP, fIPText.getText());
		try {
			configuration.setAttribute(LauncherPlatform.P_PORT, Integer.parseInt(fPortText.getText()));
		} catch (NumberFormatException e) {
			configuration.setAttribute(LauncherPlatform.P_PORT, LauncherPlatform.P_PORT_DEFAULT);
		}
		configuration.setAttribute(LauncherPlatform.P_CLUSTER, fClusterText.getText());
		configuration.setAttribute(LauncherPlatform.P_COMPILEONLY, compileOnlyButton.getSelection());

		configuration.setAttribute(LauncherPlatform.P_USER, fUserText.getText());
		configuration.setAttribute(LauncherPlatform.P_PASSWORD, fPasswordText.getText());
		
		try {
			if (!fCompilerPath.isEmpty() && configuration.getAttribute(ClientTools.P_TOOLSPATH, "").isEmpty()) { //$NON-NLS-1$
				configuration.setAttribute(ClientTools.P_TOOLSPATH, fCompilerPath);
			}
		} catch (CoreException e) {
		}
	}

	protected void handleProjectButtonSelected() {
		//		IJavaProject project = chooseJavaProject();
		//		if (project == null) {
		//			return;
		//		}
		//		String projectName = project.getElementName();
		//		fProjText.setText(projectName);		
	}

	void refreshAddress() {
		StringBuilder url = new StringBuilder("http"); //$NON-NLS-1$
		if (sslButton.getSelection()) {
			url.append("s"); //$NON-NLS-1$
		}
		url.append("://" + fIPText.getText()); //$NON-NLS-1$
		url.append(":" + fPortText.getText() + "/"); //$NON-NLS-1$ //$NON-NLS-2$
		fAddressText.setText(url.toString());
	}

	void refreshServerVersion() {
		fServerVersionText.setText(""); //$NON-NLS-1$
		fCompilerVersionText.setText(""); //$NON-NLS-1$
		
		int port = LauncherPlatform.P_PORT_DEFAULT;
		try {
			port = new Integer(fPortText.getText());
		} catch (NumberFormatException e) {
		}
		
		LauncherPlatform platform = LauncherPlatform.get(sslButton.getSelection(), fIPText.getText(), port);
		platform.clearTempDisabled();
		try {
		    //rodrigo, not sure if this should be the parsed out version of the hpcc build version or not,
		    // versionString is the raw string fetched from HPCC.
		    //Also, the Version class should have a toString (formatted version string)
			String build = platform.getVersion().versionString;
			fServerVersionText.setText(build);
			
			ClientTools clientTools = ClientTools.get(platform, testConfig);
			if (clientTools != null) {
				fCompilerPath = clientTools.getRootPath();
				ECLCompiler compiler = clientTools.getCompiler();
				if (compiler != null) {
					fCompilerVersionText.setText(compiler.getVersion());
				}
			}
			
			disableButton.setText(DisableButtonText);
		}
		catch (Exception e)
		{
            fServerVersionText.setText(e.getMessage());
            disableButton.setText(DisableButtonTempDiabledText);
		}
	}

	void refreshBrowser() {
		if (browser != null) {
			browser.addProgressListener(new ProgressAdapter() {
				@Override
				public void completed(ProgressEvent event) {
					browser.removeProgressListener(this);
					System.out.println(fAddressText.getText());
					browser.setUrl(fAddressText.getText());
				}
			});
			browser.setText(Messages.Html_Message_Loading + fAddressText.getText() + Messages.Html_Message_End);
		}
	}

	@Override
	public String getName() {
		return Messages.HpccPlatform;
	}

	@Override
	public Image getImage() {
		if (image == null) {
			image = Activator.getImage("icons/releng_gears.gif"); //$NON-NLS-1$
		}
		return image;
	}
}
