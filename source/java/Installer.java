/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.dc;

import org.rsna.installer.SimpleInstaller;

/**
 * The MultiframeSplitter program installer, consisting of just a
 * main method that instantiates a SimpleInstaller.
 */
public class Installer {

	static String windowTitle = "DicomChecker Installer";
	static String programName = "DicomChecker";
	static String introString = "<p><b>DicomChecker</b> is a stand-alone tool for checking whether "
								+ "files parse as DICOM objects."
								+ "</p>";

	public static void main(String args[]) {
		new SimpleInstaller(windowTitle, programName, introString);
	}
}
