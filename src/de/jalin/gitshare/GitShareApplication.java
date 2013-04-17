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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import de.jalin.gitshare.TrayIntegration.Status;
import de.jalin.gitshare.window.GitShareWindow;
import de.jalin.gitshare.window.RepositoryListener;

public class GitShareApplication implements Runnable, RepositoryListener {

	final private static Logger LOG = Logger.getLogger(GitShareApplication.class.getCanonicalName());

	final private GitShareWindow window;
	final private TrayIntegration trayIntegration;
	final private Queue<GitAction> actionQueue;
	final private List<GitShare> sharesList;

	public GitShareApplication(GitShareConfiguration conf) {
		ResourceBundle texts = ResourceBundle.getBundle("texts/menu");
		window = new GitShareWindow(conf, this);
		sharesList = new ArrayList<GitShare>();
		actionQueue = new LinkedBlockingDeque<GitAction>();
		trayIntegration = new TrayIntegration();
		trayIntegration.addMenuAction(texts.getString("config"), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.show();
			}
		});
		trayIntegration.addMenuAction(texts.getString("pull"), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (GitShare share : sharesList) {
					share.doPull();
				}
			}
		});
		trayIntegration.addMenuAction(texts.getString("push"), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (GitShare share : sharesList) {
					share.doPush();
				}
			}
		});
		trayIntegration.addMenuAction(texts.getString("exit"), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (GitShare share : sharesList) {
					share.abort();
				}
				try {
					Thread.sleep(2000L);
				} catch (InterruptedException e1) {
				}
				System.exit(0);
			}
		});
		trayIntegration.init();
	}
	
	public void addShare(GitShare gitShare) {
		sharesList.add(gitShare);
	}

	public void startSync() {
		for (GitShare share : sharesList) {
			Thread thread = new Thread(share);
			thread.start();
		}
	}

	public void enqueue(GitAction action) {
		boolean enqueued = false;
		while (!enqueued) {
			enqueued = actionQueue.offer(action);
			Thread.yield();
		}
	}

	@Override
	public void run() {
		while (true) {
			Thread.yield();
			GitAction gitAction = actionQueue.poll();
			if (gitAction == null) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
				}
			} else {
				LOG.info(gitAction.toString());
				trayIntegration.changeIcon(gitAction.getActionStatus());
				try {
					gitAction.performAction();
				} catch (GitShareException e) {
					LOG.severe(e.getLocalizedMessage());
				}
				trayIntegration.changeIcon(Status.IN_SYNC);
			}
		}
	}

	@Override
	public void addRepository(RepositoryConfig conf, GitActionListener ...actionListeners) throws GitShareException {
		GitShare share = GitShare.create(this, conf, actionListeners);
		addShare(share);
		new Thread(share).start();
		Thread.yield();
	}

	public static void main(String[] args) {
		GitShareConfiguration config = new GitShareConfiguration(); 
		GitShareApplication app = new GitShareApplication(config);
		for (RepositoryConfig conf : config.getRepostoryConfigs()) {
			try {
				app.addShare(GitShare.open(app, conf));
			} catch (GitShareException e) {
				LOG.severe(e.getMessage());
			}
		}
		Thread thread = new Thread(app);
		thread.start();
		Thread.yield();
		app.startSync();
	}

}
