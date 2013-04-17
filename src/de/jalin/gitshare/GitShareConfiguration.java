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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

public class GitShareConfiguration {

	final private Map<Integer, RepositoryConfig> repositoryConfigs;
	
	public GitShareConfiguration() {
		repositoryConfigs = new TreeMap<Integer, RepositoryConfig>();
		Preferences prefs = Preferences.userRoot();
		String repositoriesString = prefs.get("gitshare.repositories", "");
		String[] repositoriesNames = repositoriesString.split(",");
		for (String repoName : repositoriesNames) {
			if (!repoName.isEmpty()) {
				RepositoryConfig repoConfig = new RepositoryConfig(repoName);
				repoConfig.setLocalPath(prefs.get("gitshare." + repoName + ".localpath", ""));
				repoConfig.setRemoteURL(prefs.get("gitshare." + repoName + ".remoteurl", ""));
				repoConfig.setUsername(prefs.get("gitshare." + repoName + ".username", ""));
				repoConfig.setPassword(prefs.get("gitshare." + repoName + ".password", ""));
				repoConfig.setAuthor(prefs.get("gitshare." + repoName + ".authorname", ""));
				repoConfig.setEmail(prefs.get("gitshare." + repoName + ".authoremail", ""));
				repositoryConfigs.put(Integer.parseInt(repoName), repoConfig);
			}
		}
	}
	
	public Iterable<RepositoryConfig> getRepostoryConfigs() {
		return repositoryConfigs.values();
	}

	public int getSize() {
		return repositoryConfigs.size();
	}

	public void add(RepositoryConfig newConfig) {
		Set<Integer> keySet = repositoryConfigs.keySet();
		int maxKey = 0;
		Iterator<Integer> iterator = keySet.iterator();
		StringBuffer repositoriesStringBuffer = new StringBuffer();
		while (iterator.hasNext()) {
			int key = iterator.next().intValue();
			repositoriesStringBuffer.append(',');
			repositoriesStringBuffer.append(Integer.toString(key));
			if (key > maxKey) {
				maxKey = key;
			}
		}
		Integer newKey = new Integer(maxKey + 1);
		String repoName = newKey.toString();
		repositoriesStringBuffer.append(',');
		repositoriesStringBuffer.append(repoName);
		newConfig.setName(repoName);
		repositoryConfigs.put(newKey, newConfig);
		Preferences prefs = Preferences.userRoot();
		prefs.put("gitshare.repositories", repositoriesStringBuffer.toString().substring(1));
		prefs.put("gitshare." + repoName + ".authorname", newConfig.getAuthor());
		prefs.put("gitshare." + repoName + ".authoremail", newConfig.getEmail());
		prefs.put("gitshare." + repoName + ".localpath", newConfig.getLocalPath());
		prefs.put("gitshare." + repoName + ".remoteurl", newConfig.getRemoteURL());
		prefs.put("gitshare." + repoName + ".username", newConfig.getUsername());
		prefs.put("gitshare." + repoName + ".password", newConfig.getPassword());
	}
	
}
