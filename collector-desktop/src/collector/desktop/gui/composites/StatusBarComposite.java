package collector.desktop.gui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import collector.desktop.filesystem.BuildInformation;
import collector.desktop.gui.various.ComponentFactory;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;

public class StatusBarComposite {
	private static final int TIME_UNTIL_CLEAR_IN_MILLI_SECONDS = 10000;
	private static StatusBarComposite instance = null;
	private Composite statusbarComposite = null;
	private Label statusLabel = null;
	private Runnable timer = null;
	
	/** Returns a status bar composite which is used to display various information
	 * @param parentComposite the parent composite
	 * @return a new status bar composite */
	private StatusBarComposite(Composite parentComposite) {
		statusbarComposite = new Composite(parentComposite, SWT.NONE);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		statusbarComposite.setLayout(gridLayout);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		statusbarComposite.setLayoutData(gridData);

		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
		seperatorGridData.minimumHeight = 0;

		new Label(statusbarComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(seperatorGridData);

		statusLabel = ComponentFactory.getSmallItalicLabel(statusbarComposite, Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED, 
				BuildInformation.instance().getVersion(), BuildInformation.instance().getBuildTimeStamp(), BuildInformation.instance().getBuildType()));

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		statusLabel.setLayoutData(gridData);
	}

	public static StatusBarComposite getInstance(Shell parentShell) {
		if (instance == null) {
			instance = new StatusBarComposite(parentShell);
		}

		return instance;
	}

	public Composite getStatusbarComposite() {
		return statusbarComposite;
	}

	public void writeStatus(String status) {
		statusLabel.setText(status);

		if (!status.equals(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED))) {
			if (timer != null) {
				Display.getCurrent().timerExec(-1, timer);
			}
			
			timer = new Runnable() {
				public void run() {
					writeStatus(Translator.get(DictKeys.STATUSBAR_PROGRAM_STARTED));
				}
			};
		}
		
		Display.getCurrent().timerExec(TIME_UNTIL_CLEAR_IN_MILLI_SECONDS, timer);
	}
}