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

package de.jalin.gitshare.action;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;

import de.jalin.gitshare.AbstractGitAction;
import de.jalin.gitshare.GitAction;
import de.jalin.gitshare.GitShare;
import de.jalin.gitshare.GitShareException;
import de.jalin.gitshare.RepositoryConfig;
import de.jalin.gitshare.TrayIntegration.Status;


public class CloneAction extends AbstractGitAction implements GitAction {

	final private GitShare git;
	final private RepositoryConfig conf;

	public CloneAction(GitShare gitShare, RepositoryConfig config) {
		git = gitShare;
		conf = config;
	}
	
	@Override
	public void performAction() throws GitShareException {
		try {
			CloneCommand clone = Git.cloneRepository();
			clone.setURI(conf.getRemoteURL());
			clone.setDirectory(new File(conf.getLocalPath()));
			clone.setCredentialsProvider(git.getCredentialsProvider());
			git.setGit(clone.call());
			git.setReady(true);
			success();
		} catch (Exception e) {
			failure(e);
			throw new GitShareException(e.getLocalizedMessage());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

	@Override
	public Status getActionStatus() {
		return Status.PULLING;
	}
	
}
