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
package org.hpccsystems.eclide.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

//  TODO:  There must be an easier way to get an IFile to the HoverText callback. 
public class ECLDocument extends Document {

	IFileEditorInput editorInput;

	public void setEditorInput(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput) {
			this.editorInput = (IFileEditorInput)editorInput;
		}
	}

	IFile getFile() {
		if (editorInput != null) {
			return editorInput.getFile();
		}
		return null;
	}

}
