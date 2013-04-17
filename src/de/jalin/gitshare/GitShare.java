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

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import de.jalin.gitshare.action.AddAction;
import de.jalin.gitshare.action.CloneAction;
import de.jalin.gitshare.action.CommitAction;
import de.jalin.gitshare.action.PullAction;
import de.jalin.gitshare.action.PushAction;

public class GitShare implements Runnable {

	final private GitShareApplication app;
	final private RepositoryConfig conf;
	final private CredentialsProvider credentialsProvider;

	private Git git;
	private AutoCommiter autoCommiter;
	private boolean ready;

	public static GitShare create(GitShareApplication app, RepositoryConfig conf, GitActionListener ...actionListeners) throws GitShareException {
		GitShare share = new GitShare(app, conf);
		try {
			share.doClone(actionListeners);
			return share;
		} catch (JGitInternalException e) {
			throw new GitShareException(e.getMessage());
		}
	}
	
	public static GitShare open(GitShareApplication app, RepositoryConfig conf) throws GitShareException {
		GitShare share = new GitShare(app, conf);
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			Repository repository = builder.setGitDir(new File(conf.getLocalPath() + "/.git")).readEnvironment().findGitDir().build();
			share.setGit(new Git(repository));
			share.doPull();
			share.setReady(true);
			return share;
		} catch (IOException e) {
			throw new GitShareException(e.getMessage());
		}
	}

	private GitShare(GitShareApplication app, RepositoryConfig conf) throws GitShareException {
		this.setReady(false);
		this.app = app;
		this.conf = conf;
		credentialsProvider = new UsernamePasswordCredentialsProvider(conf.getUsername(), conf.getPassword());
	}
	
	public void setGit(Git git) {
		this.git = git;
	}

	@Override
	public void run() {
		while (!isReady()) {
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
			}
		}
		autoCommiter = new AutoCommiter(git);
		autoCommiter.doCommits(this, getRepositoryConfig().getAuthor(), getRepositoryConfig().getEmail());
	}

	public void doPush() {
		app.enqueue(new AddAction(this, "."));
		app.enqueue(new CommitAction(this, getRepositoryConfig().getAuthor(), getRepositoryConfig().getEmail(), "user commit"));
		app.enqueue(new PushAction(this));
	}

	public void doPull() {
		app.enqueue(new PullAction(this));
	}

	public void doClone(GitActionListener ...actionListeners) {
		CloneAction action = new CloneAction(this, getRepositoryConfig());
		for (GitActionListener l : actionListeners) {
			action.addListener(l);
		}
		app.enqueue(action);
	}

	public Git getGit() {
		return git;
	}

	public CredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}

	public void abort() {
		if (autoCommiter != null) {
			autoCommiter.abort();
		}
	}

	public void enqueue(GitAction action) {
		app.enqueue(action);
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public RepositoryConfig getRepositoryConfig() {
		return conf;
	}
	
}
