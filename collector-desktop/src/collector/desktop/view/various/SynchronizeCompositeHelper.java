/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
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

package collector.desktop.view.various;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Label;


public class SynchronizeCompositeHelper implements Observer {
	private HashMap<SynchronizeStep, Label> synchronizeStepsToLabelsMap = null;
	
	public void storeSynchronizeCompositeLabels(HashMap<SynchronizeStep, Label> syncronizeStepsToLabelsMap) {
		this.synchronizeStepsToLabelsMap = syncronizeStepsToLabelsMap;
	}
	
	private void enabledSynchronizeStep(SynchronizeStep synchronizeStep) {
		((Label) synchronizeStepsToLabelsMap.get(synchronizeStep).getAccessible().getControl()).setEnabled(true);
		//syncronizeStepsToLabelsMap.get(synchronizeStep).setEnabled(true);
	}
	
	private void disableSynchronizeStep(SynchronizeStep synchronizeStep) {
		((Label) synchronizeStepsToLabelsMap.get(synchronizeStep).getAccessible().getControl()).setEnabled(false);
		//.setEnabled(false);
	}

	@Override
	public void update(Observable observable, Object object) {
		disableSynchronizeStep(SynchronizeStep.ESTABLISH_CONNECTION);
		enabledSynchronizeStep(SynchronizeStep.UPLOAD_DATA);
	}
}
