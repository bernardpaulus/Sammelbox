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

package org.sammelbox.controller.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.EventObserver;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.view.ApplicationUI;

public final class MenuManager implements EventObserver {
	private static Menu menu;
	
	private MenuManager() {
		EventObservable.registerObserver(this);
	}
	
	/** This method creates and initializes the menu for the main user interface
	 * @param shell the shell used to create the user interface */
	public static Menu createAndInitializeMenuBar(Shell parentShell) {
		new MenuManager();
		
		// Create the bar menu itself
		menu = new Menu(ApplicationUI.getShell(), SWT.BAR);

		// Create all the menu items for the bar menu
		MenuItem sammelboxItem = new MenuItem(menu, SWT.CASCADE);
		MenuItem albumItem = new MenuItem(menu, SWT.CASCADE);
		MenuItem importExport = new MenuItem(menu, SWT.CASCADE);
		MenuItem settingsItem = new MenuItem(menu, SWT.CASCADE);
		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);

		// Set the text labels for each of the main menu items
		sammelboxItem.setText(Translator.get(DictKeys.MENU_COLLECTOR));
		albumItem.setText(Translator.get(DictKeys.MENU_ALBUM));
		importExport.setText(Translator.toBeTranslated("Import/Export"));
		settingsItem.setText(Translator.get(DictKeys.MENU_SETTINGS));
		helpItem.setText(Translator.get(DictKeys.MENU_HELP));

		// Setup dropdown menus for the main menu items
		createDropdownForSammelboxMenuItem(menu, sammelboxItem);
		createDropdownForAlbumMenuItem(menu, albumItem);
		createDropdownForImportExportMenuItem(menu, importExport);
		createDropdownSettingsMenuItem(menu, settingsItem);
		createDropdownHelpItem(menu, helpItem);

		// Attach the menu bar to the given shell
		ApplicationUI.getShell().setMenuBar(menu);
		
		return menu;
	}
	
	private static void createDropdownForImportExportMenuItem(Menu menu, MenuItem albumItem) {
		Menu sammelboxMenu = new Menu(menu);
		albumItem.setMenu(sammelboxMenu);
		
		MenuItem importAlbumItemsMenuItem = new MenuItem(sammelboxMenu, SWT.NONE);
		importAlbumItemsMenuItem.setText(Translator.get(DictKeys.MENU_CSV_IMPORT));
		importAlbumItemsMenuItem.addSelectionListener(ImportExportMenuItemListener.getImportAlbumItemsListener());
		
		MenuItem appendAlbumItemsMenuItem = new MenuItem(sammelboxMenu, SWT.NONE);
		appendAlbumItemsMenuItem.setText(Translator.toBeTranslated("Append data from CSV file"));
		appendAlbumItemsMenuItem.addSelectionListener(ImportExportMenuItemListener.getAppendAlbumItemsListener());
		
		new MenuItem(sammelboxMenu, SWT.SEPARATOR);
		
		MenuItem exportAlbumMenuToHTMLItem = new MenuItem(sammelboxMenu, SWT.NONE);
		exportAlbumMenuToHTMLItem.setText(Translator.toBeTranslated("Export album to HTML file"));
		exportAlbumMenuToHTMLItem.addSelectionListener(ImportExportMenuItemListener.getExportAlbumItemsToHTMLListener());	
		
		MenuItem exportAlbumMenuToCSVItem = new MenuItem(sammelboxMenu, SWT.NONE);
		exportAlbumMenuToCSVItem.setText(Translator.toBeTranslated("Export album to CSV file"));
		exportAlbumMenuToCSVItem.addSelectionListener(ImportExportMenuItemListener.getExportAlbumItemsToCSVListener());
	}

	private static void createDropdownForSammelboxMenuItem(Menu menu, MenuItem sammelboxMenuItem) {
		Menu sammelboxMenu = new Menu(menu);
		sammelboxMenuItem.setMenu(sammelboxMenu);
		
		MenuItem backupMenuItem = new MenuItem(sammelboxMenu, SWT.NONE);
		backupMenuItem.setText(Translator.get(DictKeys.MENU_BACKUP_ALBUMS_TO_FILE));
		backupMenuItem.addSelectionListener(SammelboxMenuItemListener.getBackupListener());
		
		MenuItem restoreMenuItem = new MenuItem(sammelboxMenu, SWT.NONE);
		restoreMenuItem.setText(Translator.get(DictKeys.MENU_RESTORE_ALBUM_FROM_FILE));
		restoreMenuItem.addSelectionListener(SammelboxMenuItemListener.getRestoreListener());
		
		new MenuItem(sammelboxMenu, SWT.SEPARATOR);
		
		MenuItem synchronize = new MenuItem(sammelboxMenu, SWT.NONE);
		synchronize.setText(Translator.get(DictKeys.MENU_SYNCHRONIZE));
		synchronize.addSelectionListener(SammelboxMenuItemListener.getSynchronizeListener());
		
		new MenuItem(sammelboxMenu, SWT.SEPARATOR);
		
		MenuItem exitMenuItem = new MenuItem(sammelboxMenu, SWT.NONE);
		exitMenuItem.setText(Translator.get(DictKeys.MENU_EXIT));
		exitMenuItem.addSelectionListener(SammelboxMenuItemListener.getExitListener());
	}

	private static void createDropdownForAlbumMenuItem(Menu menu, MenuItem albumMenuItem) {
		Menu albumMenu = new Menu(menu);
		albumMenuItem.setMenu(albumMenu);

		MenuItem advancedSearchMenuItem = new MenuItem(albumMenu, SWT.NONE);
		advancedSearchMenuItem.setText(Translator.get(DictKeys.MENU_ADVANCED_SEARCH));
		advancedSearchMenuItem.addSelectionListener(AlbumMenuItemListener.getAdvancedSearchListener());
		
		new MenuItem(albumMenu, SWT.SEPARATOR);
		
		MenuItem createNewAlbumMenuItem = new MenuItem(albumMenu, SWT.NONE);
		createNewAlbumMenuItem.setText(Translator.get(DictKeys.MENU_CREATE_NEW_ALBUM));
		createNewAlbumMenuItem.addSelectionListener(AlbumMenuItemListener.getCreateNewAlbumListener());
		
		MenuItem alterAlbumMenuItem = new MenuItem(albumMenu, SWT.NONE);
		alterAlbumMenuItem.setText(Translator.get(DictKeys.MENU_ALTER_SELECTED_ALBUM));
		alterAlbumMenuItem.addSelectionListener(AlbumMenuItemListener.getAlterAlbumListener());
		
		new MenuItem(albumMenu, SWT.SEPARATOR);
		
		MenuItem deleteAlbumMenuItem = new MenuItem(albumMenu, SWT.NONE);
		deleteAlbumMenuItem.setText(Translator.get(DictKeys.MENU_DELETE_SELECTED_ALBUM));
		deleteAlbumMenuItem.addSelectionListener(AlbumMenuItemListener.getDeleteAlbumListener());	

	}

	private static void createDropdownSettingsMenuItem(Menu menu, MenuItem settingsMenuItem) {
		Menu settingsMenu = new Menu(menu);
		settingsMenuItem.setMenu(settingsMenu);

		MenuItem settingsItem = new MenuItem(settingsMenu, SWT.NONE);
		settingsItem.setText(Translator.get(DictKeys.MENU_SETTINGS));
		settingsItem.addSelectionListener(SettingsMenuItemListener.getSettingsListener());
	}

	private static void createDropdownHelpItem(Menu menu, MenuItem helpItem) {
		Menu helpMenu = new Menu(menu);
		helpItem.setMenu(helpMenu);

		if (SettingsManager.showDebugMenu()) {
			MenuItem debugMenuItem = new MenuItem(helpMenu, SWT.CASCADE);
			debugMenuItem.setText("Debugging");
			Menu debugSubMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
			debugMenuItem.setMenu(debugSubMenu);
			
			MenuItem dumpHTML = new MenuItem(debugSubMenu, SWT.NONE);
			dumpHTML.setText("Dump HTML");
			dumpHTML.addSelectionListener(HelpMenuItemListener.getDumpHTMLListener());
	
			MenuItem showBrowserInfo = new MenuItem(debugSubMenu, SWT.NONE);
			showBrowserInfo.setText("Show Browser Info");
			showBrowserInfo.addSelectionListener(HelpMenuItemListener.getShowBrowserInfoListener());
						
			MenuItem disableSammelbox = new MenuItem(debugSubMenu, SWT.NONE);
			disableSammelbox.setText("Disable Sammelbox");
			disableSammelbox.addSelectionListener(HelpMenuItemListener.getDisableSammelboxListener());
			
			new MenuItem(helpMenu, SWT.SEPARATOR);
		}
		
		MenuItem reportingIssues = new MenuItem(helpMenu, SWT.NONE);
		reportingIssues.setText(Translator.get(DictKeys.MENU_REPORTING_ISSUES));
		reportingIssues.addSelectionListener(HelpMenuItemListener.getReportingIssuesListener());
		
		new MenuItem(helpMenu, SWT.SEPARATOR);
		
		MenuItem updatesAvailable = new MenuItem(helpMenu, SWT.NONE);
		updatesAvailable.setText(Translator.get(DictKeys.MENU_CHECK_FOR_UPDATES));
		updatesAvailable.addSelectionListener(HelpMenuItemListener.getCheckForUpdatesListener());
		
		new MenuItem(helpMenu, SWT.SEPARATOR);
		
		MenuItem helpContentsMenu = new MenuItem(helpMenu, SWT.NONE);
		helpContentsMenu.setText(Translator.get(DictKeys.MENU_HELP_CONTENTS));
		helpContentsMenu.addSelectionListener(HelpMenuItemListener.getHelpContentsListener());

		MenuItem aboutMenu = new MenuItem(helpMenu, SWT.NONE);
		aboutMenu.setText(Translator.get(DictKeys.MENU_ABOUT));
		aboutMenu.addSelectionListener(HelpMenuItemListener.getAboutListener());
	}

	@Override
	public void reactToEvent(SammelboxEvent event) {
		if (event.equals(SammelboxEvent.DISABLE_SAMMELBOX)) {
			menu.setEnabled(false);
		} else if (event.equals(SammelboxEvent.ENABLE_SAMMELBOX)) {
			menu.setEnabled(true);
		}
	}
}
