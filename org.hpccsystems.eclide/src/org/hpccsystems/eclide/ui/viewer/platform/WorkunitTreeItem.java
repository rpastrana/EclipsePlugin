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
package org.hpccsystems.eclide.ui.viewer.platform;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.graphics.Image;
import org.hpccsystems.eclide.Activator;
import org.hpccsystems.internal.data.Workunit;
import org.hpccsystems.internal.ui.tree.MyTreeItem;

public class WorkunitTreeItem extends PlatformBaseTreeItem implements Observer {
	Workunit workunit;

	public WorkunitTreeItem(TreeItemOwner treeViewer, PlatformBaseTreeItem parent, Workunit wu) {
		super(treeViewer, parent, wu.getPlatform());
		this.workunit = wu;
		this.workunit.addObserver(this);
		primeChildren();
	}

	public Workunit getWorkunit() {
		return workunit;
	}
	@Override
	public String getText() {
		if (workunit.isComplete()) 
			return workunit.getWuid();
		return workunit.getWuid() + " (" + workunit.getState() + ")";
	}

	@Override
	public Image getImage() {
		switch(workunit.getStateID()) {
		case SCHEDULED:
			return Activator.getImage("icons/workunit_warning.png"); 
		case SUBMITTED:
			return Activator.getImage("icons/workunit_submitted.png"); 
		case RUNNING:
			return Activator.getImage("icons/workunit_running.png"); 
		case ABORTING:
			return Activator.getImage("icons/workunit_aborting.png"); 
		case BLOCKED:
			return Activator.getImage("icons/workunit_warning.png"); 
		case WAIT:
			return Activator.getImage("icons/workunit_warning.png"); 
		case COMPILING:
			return Activator.getImage("icons/workunit_running.png"); 
		case COMPLETED:
			return Activator.getImage("icons/workunit_completed.png"); 
		case FAILED:
			return Activator.getImage("icons/workunit_failed.png"); 
		case ABORTED:
			return Activator.getImage("icons/workunit_failed.png"); 
		case ARCHIVED:
			return Activator.getImage("icons/workunit_warning.png"); 
		case COMPILED:
			return Activator.getImage("icons/workunit_completed.png"); 
		}
		return Activator.getImage("icons/workunit.png"); 
	}

	public URL getWebPageURL() throws MalformedURLException {
		return platform.getURL("WsWorkunits", "WUInfo", "Wuid=" + workunit.getWuid());
	}

	@Override
	public void primeChildren() {
		ArrayList<MyTreeItem> retVal = new ArrayList<MyTreeItem>();
		MyTreeItem parent = getParent();
		while (parent != null) {
			if (parent instanceof WorkunitTreeItem)
				if (workunit == ((WorkunitTreeItem)parent).workunit) {
					retVal.add(new RecursiveTreeItem(treeViewer, this));				
					break;
				}
			parent = parent.getParent();
		}
		if (retVal.isEmpty()) {
			retVal.add(new ResultFolderTreeItem(treeViewer, this, workunit));
			retVal.add(new GraphFolderTreeItem(treeViewer, this, workunit));
			retVal.add(new WorkunitLogicalFileFolderTreeItem(treeViewer, this, workunit));
			retVal.add(new TextTreeItem(treeViewer, this, workunit));
		}
		children.set(retVal.toArray(new MyTreeItem[0]));
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof Workunit.Notification) {
			switch ((Workunit.Notification)arg1){
			case WORKUNIT:
				update(null);
			}
		}
	}
}

