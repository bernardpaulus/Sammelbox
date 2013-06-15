package collector.desktop.gui.browser;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.album.AlbumItem;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.album.OptionType;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.DictKeys;
import collector.desktop.internationalization.Translator;
import collector.desktop.settings.ApplicationSettingsManager;

public class ItemCreator {
	private final static Logger LOGGER = LoggerFactory.getLogger(ItemCreator.class);
	
	static String getAlbumItemTableRowHtml(AlbumItem albumItem) {
		return getAlbumItemTableRowHtml(albumItem, true);
	}
	
	static String getAlbumItemTableRowHtml(AlbumItem albumItem, boolean showUpdateAndRemoveButtons) {
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();
		StringBuilder albumItemTableRowHtml = new StringBuilder();
		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, showUpdateAndRemoveButtons);

		return albumItemTableRowHtml.toString();
	}

	static String getAlbumItemDivContainerHtml(AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		addAlbumItemDivContainer(albumItem, htmlBuilder);

		return htmlBuilder.toString();
	}

	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml) {
		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, true);
	}
	
	static void addAlbumItemDivContainer(AlbumItem albumItem, StringBuilder htmlBuilder) {
		htmlBuilder.append(
				"<div id=\"imageId" + albumItem.getItemID() + "\" " +
				"    class=\"pictureContainer\" " +
				"    onMouseOver=\"parent.location.href=&quot;show:///details=" + albumItem.getItemID() + "&quot;\" " +
				"    onClick=\"parent.location.href=&quot;show:///detailsComposite=" + albumItem.getItemID() + "&quot;\">" +
                "  <div class=\"innerPictureContainer\">" +
		        "    <img src=\"" + albumItem.getPrimaryThumbnailPicturePath() + "\">" +
                "  </div>" +
                "</div>");
	}
	
	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, 
			StringBuilder albumItemTableRowHtml, boolean showUpdateAndRemoveButtons) {
		
		// the id of the current album item
		long id = -1;
		
		for (ItemField fieldItem : albumItem.getFields()) {			
			if (fieldItem.getType().equals(FieldType.UUID)) {
				// schema or content version UUID --> ignore 
			} else if (fieldItem.getType().equals(FieldType.ID)) {
				if (!fieldItem.getName().equals("typeinfo")) {
					// do not show, but store id
					id = fieldItem.getValue();
				} else {
					LOGGER.warn("Found a field type that wasn't expected: " + fieldItem.getName());
				}
			} else if (fieldItem.getType().equals(FieldType.Picture)) {
				List<URI> uris = fieldItem.getValue();
				htmlPictureColumnContent.append(
						"<table border=0>" +
						"  <tr>" +
						"    <td align=center width=200 height=200>" +
				               getMainPictureHtml(id, uris) +
						"    </td>" +
						"  </tr>" +
						"  <tr>" +
						"    <td>" +
				        "      <div style=\"max-width:200px;\">" +
						         getAlternativePicturesHtml(id, uris) +
						"      </div>" + 
						"    </td>" +
						"  </tr>" + 
						"</table>");
			} else if (fieldItem.getType().equals(FieldType.Option)) {
				if (fieldItem.getValue() == OptionType.YES) {
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_YES)));
				} else if (fieldItem.getValue() == OptionType.NO) {
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_NO)));
				} else if (fieldItem.getValue() == OptionType.UNKNOWN) {
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_UNKNOWN)));
				}
			} else if (fieldItem.getType().equals(FieldType.Date)) {
				java.sql.Date sqlDate = fieldItem.getValue();
				java.util.Date utilDate = new java.util.Date(sqlDate.getTime());

				SimpleDateFormat dateFormater = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, ApplicationSettingsManager.getUserDefinedLocale());
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), dateFormater.format(utilDate)));
			} else {
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Utilities.escapeHtmlString(fieldItem.getValue().toString())));
			}
		}	

		if (showUpdateAndRemoveButtons) {
			htmlDataColumnContent.append(getUpdateRemoveButtonsHtml(id));
		}	

		albumItemTableRowHtml.append("<tr id=\"albumId" + id + "\">" +
				                     "  <td>" + htmlPictureColumnContent + "</td>" +
				                     "    <td width=90% bgcolor=" + Utilities.getBackgroundColorOfWidgetInHex() + ">" + 
				                     	    htmlDataColumnContent + 
				                     "    </td>" +
				                     "  </tr>" +
				                     "  <tr>" +
				                     "    <td height=\"20\" colspan=\"2\">" +
				                     "  </td>" +
				                     "</tr>");		
	}
	
	private static String getUpdateRemoveButtonsHtml(long id) {
		return "<form>" +
		       "  <input type=\"button\" " +
			   "         onclick=parent.location.href=\"show:///updateComposite=" + id + "\" " +
			   "         value=\"" + Translator.get(DictKeys.BROWSER_UPDATE) + "\">" +
		       "  <input type=\"button\" " +
			   "    onclick=parent.location.href=\"show:///deleteComposite=" + id + "\" " +
		       "    value=\"" + Translator.get(DictKeys.BROWSER_DELETE) + "\">" +
		       "</form>";
	}
	
	private static String getFieldNameAndValueLine(String fieldName, String value) {
		return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span> : " + value + "<br>"; 
	}
	
	private static String getAlternativePicturesHtml(long id, List<URI> uris) {
		StringBuilder htmlBuilder = new StringBuilder();
		
		if (uris.size() > 1) {	
			for(URI uri : uris) {
				if (!uri.toString().contains("original")) {						
					htmlBuilder.append(
						"<div align=center style=\"display:inline; min-width:40px; width:auto; width:40px\">" +
						"  <a onClick=showBigPicture(\"imageId" + id + "\") " +
						"     onMouseOver=\"change(&quot;imageId" + id + "&quot;, &quot;" + uri.toString() + "&quot;)\">" +
					    "    <img onMouseOver=this.style.cursor=\"pointer\" style=\"max-width:40px; max-height:40px;\" src=\"" + uri.toString() + "\">" +
					    "  </a>" +
					    "</div>");
				}
			}
		}
		
		return htmlBuilder.toString();
	}

	private static String getMainPictureHtml(long id, List<URI> uris) {
		// Initialize with placeholder
		String mainPictureHtml = "<img id=\"imageId" + id + "\" " +
				 "     width=195 " +
				 "     height=195 " +
				 "     src=\"" + FileSystemAccessWrapper.PLACEHOLDERIMAGE + "\">";
		
		// Use primary image if available
		if (!uris.isEmpty()) {
			mainPictureHtml = "<img id=\"imageId" + id + "\" " +
							  "     style=\"max-width:195px; " +
							  "     max-height:195px;\" " +
							  "     src=\"" + uris.get(0).toString() + "\" " +
							  "     onMouseOver=changeCursorToHand(\"imageId" + id + "\") " +
							  "     onClick=showBigPicture(\"imageId" + id + "\")>";
		}
		
		return mainPictureHtml;
	}
}
