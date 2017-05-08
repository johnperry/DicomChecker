/*---------------------------------------------------------------
*  Copyright 2016 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.dc;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.rsna.ui.*;
import org.rsna.util.*;
import org.rsna.ctp.objects.*;
import org.apache.log4j.*;

public class DicomChecker extends JFrame implements ActionListener {

    String windowTitle = "Dicom File Checker";
    JLabel filepath;
    JCheckBox showDICOM;
    JCheckBox showNonDICOM;
    JButton start;
    ColorPane cp;
	Color bgColor = new Color(0xc6d8f9);
	JFileChooser chooser = null;
	DicomObject dob = null;
	File startingFile;

    public static void main(String args[]) {
		Logger.getRootLogger().addAppender(
				new ConsoleAppender(
					new PatternLayout("%d{HH:mm:ss} %-5p [%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.INFO);
        new DicomChecker();
    }

    public DicomChecker() {
		super();
		setTitle(windowTitle);
		JPanel mainPanel = new JPanel(new BorderLayout());
		getContentPane().add(mainPanel,BorderLayout.CENTER);
		
		//Make a header panel
		JPanel header = new JPanel();
		header.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.setBackground(bgColor);
		start = new JButton("Start");
		start.addActionListener(this);
		showDICOM = new JCheckBox("Show DICOM Files");
		showDICOM.setSelected(false);
		showDICOM.setBackground(bgColor);
		showNonDICOM = new JCheckBox("Show Non-DICOM Files");
		showNonDICOM.setSelected(true);
		showNonDICOM.setBackground(bgColor);
		header.add(start);
		header.add(Box.createHorizontalGlue());
		header.add(showDICOM);
		header.add(Box.createHorizontalStrut(30));
		header.add(showNonDICOM);
		mainPanel.add(header, BorderLayout.NORTH);

		//Make a footer panel
		JPanel footer = new JPanel();
		footer.setLayout(new FlowLayout(FlowLayout.LEADING));
		footer.setBackground(bgColor);
		filepath = new JLabel(" ");
		footer.add(filepath);
		mainPanel.add(footer, BorderLayout.SOUTH);

		//Make the center pane
		JPanel cpPanel = new JPanel(new BorderLayout());
		JScrollPane jsp = new JScrollPane();
		cp = new ColorPane();
		jsp.setViewportView(cp);
		cpPanel.add(jsp, BorderLayout.CENTER);
		mainPanel.add(cpPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        pack();
        centerFrame();
        setVisible(true);
        startingFile = getStartingFile();
	}

	public void actionPerformed(ActionEvent e) {
        if (startingFile != null) {
			FileScanner fileScanner = new FileScanner(startingFile);
			fileScanner.start();
		}
		else System.exit(0);
	}

    private void centerFrame() {
        Toolkit t = getToolkit();
        Dimension scr = t.getScreenSize ();
        setSize(scr.width/2, scr.height/2);
        setLocation (new Point ((scr.width-getSize().width)/2,
                                (scr.height-getSize().height)/2));
    }
    
	private File getStartingFile() {
		File here = new File(System.getProperty("user.dir"));
		chooser = new JFileChooser(here);
		chooser.setDialogTitle("Select a DICOM image file or a directory");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setSelectedFile(here);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		else return null;
	}
	
	class FileScanner extends Thread {
		File file;
		public FileScanner(File file) {
			super();
			this.file = file;
		}
		public void run() {
			cp.clear();
			try { process(file); }
			catch (Exception ex) {
				StringWriter sw = new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				cp.println(sw.toString());
			}
			cp.println(Color.black, "\nDone.");
			showPath(" ");
		}
		private void process(File file) {
			String path = file.getAbsolutePath();
			showPath(path);
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) process(f);
			}
			else {
				try {
					DicomObject dob = new DicomObject(file);
					if (showDICOM.isSelected()) cp.println(Color.black, path);
				}
				catch (Exception ex) {
					if (showNonDICOM.isSelected()) cp.println(Color.red, path + " - not DICOM");
				}				
			}
		}
		private void showPath(String path) {
			final String fpath = path;
			Runnable update = new Runnable() {
				public void run() {
					filepath.setText(fpath);
				}
			};
			SwingUtilities.invokeLater(update);
		}
	}

}
