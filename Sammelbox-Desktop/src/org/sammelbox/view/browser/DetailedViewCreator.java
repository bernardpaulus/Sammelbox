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

package org.sammelbox.view.browser;

import org.eclipse.swt.browser.Browser;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;

public final class DetailedViewCreator {	
	private DetailedViewCreator() {
	}
	
	static void showDetailedAlbum(Browser browser) {
		// Exit if no album is selected
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
			return;
		}
		
		// Builders for efficient html creation
		StringBuilder albumItemDetailDivContainers = new StringBuilder();
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();
		StringBuilder htmlPreviewPicturesContent = new StringBuilder();


		// Add all available album items
		boolean hasEvenCountingInList = false;
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems()) {
			htmlDataColumnContent.delete(0, htmlDataColumnContent.length());
			htmlPictureColumnContent.delete(0, htmlPictureColumnContent.length());
			htmlPreviewPicturesContent.delete(0, htmlPreviewPicturesContent.length());
			DetailedItemCreator.addImageAndDetailContainer(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemDetailDivContainers, htmlPreviewPicturesContent, true, hasEvenCountingInList);
			hasEvenCountingInList = !hasEvenCountingInList;
		}

		// If no album items have been found
		if (htmlDataColumnContent.length() == 0 && htmlPictureColumnContent.length() == 0) {
			albumItemDetailDivContainers.delete(0, albumItemDetailDivContainers.length());
			albumItemDetailDivContainers.append(
	          "<tr><td><div>" + 
	            "<h3>" + 
	              Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, GuiController.getGuiState().getSelectedAlbum()) + 
	            "</h3>" + 
	            "<p>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + "</p>" +
	          "</div></td></tr>"); 
		}
		
		// Build header using album name. Include view name if appropriated
		String collectionHeader = GuiController.getGuiState().getSelectedAlbum();
		if (GuiController.getGuiState().isViewSelected()) {
			collectionHeader += " - " + GuiController.getGuiState().getSelectedSavedSearch();
		}
		
		// Create final page html
		String finalPageAsHtml = 
				"<!DOCTYPE HTML>" +
				"<html>" +
				  "<head>" +
				    "<title>sammelbox.org</title>" +
				    "<meta " + UIConstants.META_PARAMS + ">" + 
				    "<link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
				    "<script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
				  "</head>" +
				  "<body>" +
				    "<h2>" + collectionHeader + "</h2>" +
				  	"<div id=\"albumItems\">" + albumItemDetailDivContainers + "</div>" +
				  "</body>" +
				"</html>";
		
		browser.setText(finalPageAsHtml);		
		BrowserUtils.setLastPageAsHtml(finalPageAsHtml);		
	}
}
