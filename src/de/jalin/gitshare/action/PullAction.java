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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CheckoutCommand.Stage;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;

import de.jalin.gitshare.AbstractGitAction;
import de.jalin.gitshare.GitAction;
import de.jalin.gitshare.GitShare;
import de.jalin.gitshare.GitShareException;
import de.jalin.gitshare.TrayIntegration.Status;


public class PullAction extends AbstractGitAction implements GitAction {

	final private GitShare git;

	public PullAction(GitShare gitShare) {
		git = gitShare;
	}
	
	@Override
	public void performAction() throws GitShareException {
		try {
			git.getGit().fetch().setCredentialsProvider(git.getCredentialsProvider()).call();
			MergeCommand merge = git.getGit().merge(); 
			merge.include(git.getGit().getRepository().getRef("origin/master"));
			MergeResult mergeResult = merge.call();
			MergeStatus mergeStatus = mergeResult.getMergeStatus();
			if (mergeStatus.equals(MergeStatus.CONFLICTING)) {
				Map<String, int[][]> conflicts = mergeResult.getConflicts();
				CheckoutCommand checkout = git.getGit().checkout();
				for (String s : conflicts.keySet()) {
					checkout.addPath(s);
				}
				checkout.setStage(Stage.OURS);
				checkout.call();
				for (String s : conflicts.keySet()) {
					String path = git.getRepositoryConfig().getLocalPath();
					File myFile = new File(path + "/" + s);
					int lastSlash = s.lastIndexOf('/');
					File newFile = new File(path + "/" + s.substring(0, lastSlash) + "/my_" + s.substring(lastSlash + 1));
					if (!myFile.renameTo(newFile)) {
						FileInputStream inputStream = new FileInputStream(myFile);
						FileOutputStream outputStream = new FileOutputStream(newFile);
						byte[] buff = new byte[16384];
						int blockSize = inputStream.read(buff);
						while (blockSize > 0) {
							outputStream.write(buff);
							blockSize = inputStream.read(buff);
						}
						inputStream.close();
						outputStream.close();
					}
				}
				checkout = git.getGit().checkout();
				for (String s : conflicts.keySet()) {
					checkout.addPath(s);
				}
				checkout.setStage(Stage.THEIRS);
				checkout.call();
			}
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
