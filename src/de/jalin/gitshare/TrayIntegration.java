// 
// This file is part of GitShare.
// 
// GitShare is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// GitShare is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with GitShare.  If not, see <http:// www.gnu.org/licenses/>.
// 
// Diese Datei ist Teil von GitShare.
// 
// GitShare ist Freie Software: Sie können es unter den Bedingungen
// der GNU General Public License, wie von der Free Software Foundation,
// Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
// veröffentlichten Version, weiterverbreiten und/oder modifizieren.
// 
// GitShare wird in der Hoffnung, dass es nützlich sein wird, aber
// OHNE JEDE GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite
// Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
// Siehe die GNU General Public License für weitere Details.
// 
// Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
// Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

package de.jalin.gitshare;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;


public class TrayIntegration {

	public enum Status {
		STARTING, PULLING, PUSHING, IN_SYNC, SEARCHING;
	}
	
	private final SystemTray systemTray;
	
	private Map<Status, String> iconSet = new HashMap<Status, String>();
	private TrayIcon trayIcon;

	private PopupMenu menu;

	public TrayIntegration() {
		menu = new PopupMenu();
		if (SystemTray.isSupported()) {
			systemTray = SystemTray.getSystemTray();
			iconSet.put(Status.STARTING, "/icons/24_coffee.png");
			iconSet.put(Status.PULLING, "/icons/24_download.png");
			iconSet.put(Status.PUSHING, "/icons/24_upload.png");
			iconSet.put(Status.SEARCHING, "/icons/24_refresh.png");
			iconSet.put(Status.IN_SYNC, "/icons/24_weather.png");
		} else {
			systemTray = null;
		}
	}

	public void addMenuAction(String menuText, ActionListener actListener) {
		MenuItem defaultAction = new MenuItem(menuText);
		defaultAction.addActionListener(actListener);
		menu.add(defaultAction);
	}

	public void init() {
		if (systemTray != null) {
			trayIcon = new TrayIcon(loadIcon(Status.STARTING), "GitShare", menu);
			try {
				systemTray.add(trayIcon);
			} catch (AWTException e) {
			}
		}
	}
	
	public void changeIcon(Status status) {
		if (systemTray != null) {
			trayIcon.setImage(loadIcon(status));
		}
	}
	
	private Image loadIcon(Status status) {
		try {
			String resourceName = iconSet.get(status);
			Class<? extends TrayIntegration> clasz = getClass();
			InputStream resourceAsStream = clasz.getResourceAsStream(resourceName);
			return ImageIO.read(resourceAsStream);
		} catch (IOException e) {
			return null;
		}
	}

}
