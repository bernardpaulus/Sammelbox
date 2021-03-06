/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox.view.various;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TextInputDialog extends Dialog {
	private static final int INPUT_DIALOG_HEIGHT_IN_PIXELS = 200;
	/** Stores the value which is eventually entered */
	private String value = null;

	/** Creates a new TextInputDialog as a child of the provided parent shell 
	 * @param parent the parent of the text input dialog*/
	public TextInputDialog(Shell parent) {
		super(parent);
	}

	/** Creates a new TextInputDialog as a child of the provided parent shell, while accepting SWT styles
	 * @param style an SWT style constant (E.g. SWT.BORDER) */
	public TextInputDialog(Shell parent, int style) {
		super(parent, style);
	}

	/** Opens the text input dialog based on the parameters provided
	 * @param title the window caption
	 * @param labelText the text within the dialog. (E.g. "Please enter a new name") 
	 * @param textBoxValue a default value for the edit field of the dialog
	 * @param buttonText the text for the dialog button (E.g. "Rename") 
	 * @return A string holding the value that the user entered or null, if the dialog was canceled/closed */
	public String open(String title, String labelText, String textBoxValue, String buttonText) {
		final Shell shell = new Shell(getParent(), SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText(title);
		shell.setLayout(new GridLayout(1, true));
				
		ComponentFactory.getH3Label(shell, labelText);

		final Text inputText = new Text(shell, SWT.SINGLE | SWT.BORDER);
		inputText.setText(textBoxValue);
		
		GridData gridData = new GridData();
		gridData.widthHint = INPUT_DIALOG_HEIGHT_IN_PIXELS;
		inputText.setLayoutData(gridData);		
	    
		final Button button = new Button(shell, SWT.PUSH);
		button.setText(buttonText);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		inputText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				try {
					value = inputText.getText();
					button.setEnabled(true);
				} catch (Exception e) {
					button.setEnabled(false);
				}
			}
		});

		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE) {
					event.doit = false;
				}
			}
		});

		shell.pack();
		shell.open();
		
		// Center shell to primary screen
		Monitor primaryMonitor = shell.getDisplay().getPrimaryMonitor();
		Rectangle primaryMonitorBounds = primaryMonitor.getBounds();
		Rectangle shellBounds = shell.getBounds();
		int xCoordinateForShell = primaryMonitorBounds.x + (primaryMonitorBounds.width - shellBounds.width) / 2;
		int yCoordinateForShell = primaryMonitorBounds.y + (primaryMonitorBounds.height - shellBounds.height) / 2;
		shell.setLocation(xCoordinateForShell, yCoordinateForShell);
		
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}

		return value;
	}
}
