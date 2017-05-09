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
    JCheckBox saveNonDICOM;
    JButton start;
    ColorPane cp;
	Color bgColor = new Color(0xc6d8f9);
	JFileChooser inputChooser = null;
	JFileChooser outputChooser = null;

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
		saveNonDICOM = new JCheckBox("Save Non-DICOM Files");
		saveNonDICOM.setSelected(false);
		saveNonDICOM.setBackground(bgColor);
		header.add(start);
		header.add(Box.createHorizontalGlue());
		header.add(showDICOM);
		header.add(Box.createHorizontalStrut(30));
		header.add(showNonDICOM);
		header.add(Box.createHorizontalStrut(30));
		header.add(saveNonDICOM);
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
	}

	public void actionPerformed(ActionEvent e) {
		new FileScanner().start();
	}

    private void centerFrame() {
        Toolkit t = getToolkit();
        Dimension scr = t.getScreenSize ();
        setSize(scr.width/2, scr.height/2);
        setLocation (new Point ((scr.width-getSize().width)/2,
                                (scr.height-getSize().height)/2));
    }
    
	private File getStartingFile() {
		if (inputChooser == null) {
			File here = new File(System.getProperty("user.dir"));
			inputChooser = new JFileChooser(here);
			inputChooser.setDialogTitle("Select a DICOM image file or a directory");
			inputChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			inputChooser.setSelectedFile(here);
		}
		if (inputChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return inputChooser.getSelectedFile();
		}
		else return null;
	}
	
	private File getOutputFile() {
		if (outputChooser == null) {
			File here = new File(System.getProperty("user.dir"));
			outputChooser = new JFileChooser(here);
			outputChooser.setDialogTitle("Select a file for saving non-DICOM file paths");
			outputChooser.setSelectedFile(new File(here, "Non-DICOM.txt"));
		}
		if (outputChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			return outputChooser.getSelectedFile();
		}
		else return null;
	}
	
	class FileScanner extends Thread {
		File file;
		int dicomCount;
		int nonDicomCount;
		DicomObject dob = null;
		PrintWriter pw = null;
		public FileScanner() {
			super();
			cp.clear();
			dicomCount = 0;
			nonDicomCount = 0;
			file = getStartingFile();
			if (file == null) System.exit(0);
			if (saveNonDICOM.isSelected()) {
				File outputFile = getOutputFile();
				if (outputFile != null) {
					try { pw = new PrintWriter(outputFile); }
					catch (Exception unable) { 
						cp.println(Color.red, "Unable to create the output file");
					}
				}
			}
		}
		public void run() {
			try { process(file); }
			catch (Exception ex) {
				StringWriter sw = new StringWriter();
				ex.printStackTrace(new PrintWriter(sw));
				cp.println(sw.toString());
			}
			cp.println(Color.black, " ");
			cp.println(Color.black, dicomCount+" DICOM file"+((dicomCount!=1)?"s":"")+" found");
			cp.println(Color.black, nonDicomCount+" non-DICOM file"+((nonDicomCount!=1)?"s":"")+" found");
			cp.println(Color.black, "Done.");
			showPath(" ");
			if (pw != null) {
				pw.flush();
				pw.close();
			}
		}
		private void process(File file) {
			String path = file.getAbsolutePath();
			showPath(path);
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null) for (File f : files) process(f);
				else cp.println(Color.red, "\nUnable to process "+path+"\n");
			}
			else {
				try {
					DicomObject dob = new DicomObject(file);
					dicomCount++;
					if (showDICOM.isSelected()) cp.println(Color.black, path);
				}
				catch (Exception ex) {
					nonDicomCount++;
					if (showNonDICOM.isSelected()) cp.println(Color.red, path + " - not DICOM");
					if (pw != null) pw.println(path);
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
