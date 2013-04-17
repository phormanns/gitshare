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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;

import de.jalin.gitshare.action.AddAction;
import de.jalin.gitshare.action.CommitAction;
import de.jalin.gitshare.action.PullAction;
import de.jalin.gitshare.action.PushAction;
import de.jalin.gitshare.action.RemoveAction;

public class AutoCommiter implements Runnable {

	final private static Logger LOG = Logger.getLogger(AutoCommiter.class.getCanonicalName());

	final private Path path;
	final private Path gitPath;

	private WatchService watch;
	private Map<WatchKey, Path> directoryKeys;
	private boolean isAborted = false;
	
	public AutoCommiter(Git git)  {
		gitPath = Paths.get(git.getRepository().getDirectory().getAbsolutePath());
		path  = gitPath.getParent();
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(this));
			watch = path.getFileSystem().newWatchService();
			directoryKeys = new HashMap<WatchKey, Path>();
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					if (!dir.startsWith(gitPath)) {
						WatchKey watchKey = dir.register(watch, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
						directoryKeys.put(watchKey, dir);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			LOG.severe(e.getMessage());
		}
	}
	
	public void doCommits(GitShare share, final String author, final String email) {
		WatchKey key = null;
		int waitForPull = 0;
		while (!isAborted) {
			try {
				key = watch.poll(500, TimeUnit.MILLISECONDS);
				if (key != null) {
					StringBuffer comment = new StringBuffer("AutoCommit");
					for (WatchEvent<?> evt : key.pollEvents()) {
						Kind<?> kind = evt.kind();
						LOG.info(kind.name());
						Object context = evt.context();
						if (context instanceof Path) {
							Path resolvedPath = directoryKeys.get(key).resolve((Path) context);
							Path relPath = path.relativize(resolvedPath);
							String pathString = relPath.toString();
							LOG.info(pathString);
							if (kind.equals(ENTRY_DELETE)) {
								if (!resolvedPath.toFile().exists()) {
									share.enqueue(new RemoveAction(share, pathString));
									comment.append("\n  rm " + pathString);
								}
							} else {
								if (resolvedPath.toFile().isDirectory()) {
									WatchKey watchKey;
									try {
										watchKey = resolvedPath.register(watch, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
										directoryKeys.put(watchKey, resolvedPath);
									} catch (IOException e) {
									}
									share.enqueue(new AddAction(share, pathString + "/"));
									comment.append("\n  add " + pathString);
								} else {
									share.enqueue(new AddAction(share, pathString));
									comment.append("\n  add " + pathString);
								}
							}
						}
					}
					key.reset();
					share.enqueue(new CommitAction(share, author, email, comment.toString()));
					share.enqueue(new PushAction(share));
				}
				Thread.sleep(15000L);
				// pull every 10 minutes
				if (waitForPull < 20) {
					waitForPull++;
				} else {
					share.enqueue(new PullAction(share));
					waitForPull = 0;
				}
				Thread.yield();
				Thread.sleep(15000L);
			} catch (InterruptedException e) {
				LOG.severe(e.getMessage());
			}
		}
	}
	
	@Override
	public void run() {
		abort();
	}

	public void abort() {
		isAborted = true;
		try {
			Thread.sleep(1000L);
			watch.close();
		} catch (IOException e) {
			LOG.severe(e.getMessage());
		} catch (InterruptedException e) {
			LOG.severe(e.getMessage());
		}
	}

	public void close() {
		abort();
	}
	
}
