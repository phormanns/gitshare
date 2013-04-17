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

package de.jalin.gitshare.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jalin.gitshare.GitActionListener;
import de.jalin.gitshare.GitShareConfiguration;
import de.jalin.gitshare.GitShareException;
import de.jalin.gitshare.RepositoryConfig;

public class GitShareWindow extends AbstractListModel<RepositoryConfig> {

	private static final long serialVersionUID = 1L;
	
	final private JFrame window;
	final private GitShareConfiguration config;
	final private RepositoryListener listener;
	
	private JTextField tfAuthorName;
	private JTextField tfAuthorEMail;
	private JTextField tfRepositoryRemoteURL;
	private JTextField tfRepositoryLocalPath;
	private JButton btnSelectLocalDirectory;
	private JTextField tfRepositoryUsername;
	private JTextField tfRepositoryPassword;
	private JButton btnNewRepository;
	private JButton btnAddRepository;
	private JList<RepositoryConfig> lstRepositories;

	
	public GitShareWindow(GitShareConfiguration conf, RepositoryListener app) {
		window = new JFrame("GitShare");
		config = conf;
		listener = app;
		initWindow();
	}

	@Override
	public RepositoryConfig getElementAt(int idx) {
		int i = 0;
		for (RepositoryConfig c : config.getRepostoryConfigs()) {
			if (i >= idx) {
				return c;
			}
			i++;
		}
		return null;
	}

	@Override
	public int getSize() {
		return config.getSize();
	}
	
	public void show() {
		window.setVisible(true);		
	}

	private void initWindow() {
		final ResourceBundle texts = ResourceBundle.getBundle("texts/window");
		Container pane = window.getContentPane();
		pane.removeAll();
		tfAuthorName = makeTextField();
		tfAuthorEMail = makeTextField();
		tfRepositoryRemoteURL = makeTextField();
		tfRepositoryLocalPath = makeTextField();
		tfRepositoryUsername = makeTextField();
		tfRepositoryPassword = makePasswordField();
		JPanel left = new JPanel(new BorderLayout());
		btnAddRepository = new JButton(texts.getString("repository.add"));
		btnAddRepository.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lstRepositories.clearSelection();
				tfAuthorName.setText("");
				tfAuthorName.setEditable(true);
				tfAuthorEMail.setText("");
				tfAuthorEMail.setEditable(true);
				tfRepositoryRemoteURL.setText("");
				tfRepositoryRemoteURL.setEditable(true);
				tfRepositoryLocalPath.setText("");
				tfRepositoryLocalPath.setEditable(true);
				tfRepositoryUsername.setText("");
				tfRepositoryUsername.setEditable(true);
				tfRepositoryPassword.setText("");
				tfRepositoryPassword.setEditable(true);
				btnSelectLocalDirectory.setEnabled(true);
				btnNewRepository.setEnabled(true);
			}
		});
		left.add(btnAddRepository, BorderLayout.NORTH);
		lstRepositories = new JList<RepositoryConfig>(this);
		lstRepositories.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				Object evtSource = evt.getSource();
				int selIdx = ((JList<?>) evtSource).getMinSelectionIndex();
				if (selIdx >=0) {
					Iterator<RepositoryConfig> iterable = config.getRepostoryConfigs().iterator();
					int idx = 0;
					RepositoryConfig selected = iterable.next();
					while (idx < selIdx && iterable.hasNext()) {
						selected = iterable.next();
						idx++;
					}
					tfAuthorName.setText(selected.getAuthor());
					tfAuthorName.setEditable(false);
					tfAuthorEMail.setText(selected.getEmail());
					tfAuthorEMail.setEditable(false);
					tfRepositoryRemoteURL.setText(selected.getRemoteURL());
					tfRepositoryRemoteURL.setEditable(false);
					tfRepositoryLocalPath.setText(selected.getLocalPath());
					tfRepositoryLocalPath.setEditable(false);
					tfRepositoryUsername.setText(selected.getUsername());
					tfRepositoryUsername.setEditable(false);
					tfRepositoryPassword.setText(selected.getPassword());
					tfRepositoryPassword.setEditable(false);
					btnSelectLocalDirectory.setEnabled(false);
					btnNewRepository.setEnabled(false);
				}
			}
		});
		left.add(new JScrollPane(lstRepositories), BorderLayout.CENTER);
		JPanel right = new JPanel(new BorderLayout());
		right.setBackground(Color.WHITE);
		right.setBorder(BorderFactory.createLineBorder(Color.WHITE, 7));
		JPanel form = new JPanel(new SpringLayout());
		form.setBackground(Color.WHITE);
		form.add(makeLabel(texts.getString("author.name")));
		form.add(tfAuthorName);
		form.add(makeFiller());
		form.add(makeLabel(texts.getString("author.email")));
		form.add(tfAuthorEMail);
		form.add(makeFiller());
		form.add(makeLabel(texts.getString("repository.remoteurl")));
		form.add(tfRepositoryRemoteURL);
		form.add(makeFiller());
		form.add(makeLabel(texts.getString("repository.localpath")));
		form.add(tfRepositoryLocalPath);
		btnSelectLocalDirectory = new JButton(texts.getString("path.select"));
		btnSelectLocalDirectory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent acEvent) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.showOpenDialog(window);
				File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile != null) {
					tfRepositoryLocalPath.setText(selectedFile.getAbsolutePath());
				}
			}
		});
		btnSelectLocalDirectory.setEnabled(false);
		form.add(btnSelectLocalDirectory);
		form.add(makeLabel(texts.getString("repository.username")));
		form.add(tfRepositoryUsername);
		form.add(makeFiller());
		form.add(makeLabel(texts.getString("repository.password")));
		form.add(tfRepositoryPassword);
		form.add(makeFiller());
		btnNewRepository = new JButton(texts.getString("btn.addnewrepository"));
		btnNewRepository.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final RepositoryConfig newConfig = new RepositoryConfig();
				newConfig.setLocalPath(tfRepositoryLocalPath.getText());
				newConfig.setRemoteURL(tfRepositoryRemoteURL.getText());
				newConfig.setUsername(tfRepositoryUsername.getText());
				newConfig.setPassword(tfRepositoryPassword.getText());
				newConfig.setAuthor(tfAuthorName.getText());
				newConfig.setEmail(tfAuthorEMail.getText());
				try {
					listener.addRepository(newConfig, new GitActionListener() {
						
						@Override
						public void onSuccess() {
							config.add(newConfig);
						}
						
						@Override
						public void onFailure(Throwable err) {
							JOptionPane.showConfirmDialog(window, err.getLocalizedMessage(), texts.getString("error"), JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
						}
					});
					initWindow();
				} catch (GitShareException err) {
					JOptionPane.showConfirmDialog(window, err.getMessage(), "error", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnNewRepository.setEnabled(false);
		form.add(btnNewRepository);
		form.add(makeFiller());
		form.add(makeFiller());
		SpringUtilities.makeCompactGrid(form, 7, 3, 6, 6, 6, 6);
		right.add(form, BorderLayout.NORTH);
		pane.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right));
		window.pack();
	}

	private JPanel makeFiller() {
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setMaximumSize(new Dimension(72, 28));
		return p;
	}

	private JTextField makeTextField() {
		JTextField tf = new JTextField("");
		tf.setPreferredSize(new Dimension(200, 28));
		tf.setEditable(false);
		return tf;
	}

	private JTextField makePasswordField() {
		JTextField tf = new JPasswordField("");
		tf.setPreferredSize(new Dimension(200, 28));
		tf.setEditable(false);
		return tf;
	}

	private JLabel makeLabel(String text) {
		return new JLabel(text, JLabel.RIGHT);
	}

}
