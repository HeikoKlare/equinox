/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.osgi.tests.configuration;

import static org.eclipse.osgi.tests.OSGiTestsActivator.PI_OSGI_TESTS;
import static org.eclipse.osgi.tests.OSGiTestsActivator.addRequiredOSGiTestsBundles;
import static org.eclipse.osgi.tests.OSGiTestsActivator.getContext;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.harness.FileSystemComparator;
import org.eclipse.core.tests.harness.session.CustomSessionConfiguration;
import org.eclipse.core.tests.harness.session.ExecuteInHost;
import org.eclipse.core.tests.harness.session.SessionTestExtension;
import org.eclipse.osgi.tests.OSGiTestsActivator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadOnlyConfigurationAreaTest {

	private static CustomSessionConfiguration sessionConfiguration = createSessionConfiguration();

	@RegisterExtension
	static SessionTestExtension extension = SessionTestExtension.forPlugin(PI_OSGI_TESTS)
			.withCustomization(sessionConfiguration).create();

	private static CustomSessionConfiguration createSessionConfiguration() {
		CustomSessionConfiguration configuration = SessionTestExtension.createCustomConfiguration().setReadOnly();
		addRequiredOSGiTestsBundles(configuration);
		return configuration;
	}

	@Test
	@Order(1)
	public void test0thSession() throws Exception {
		// initialization session
		Bundle installed = BundleTestingHelper.installBundle("1.0", getContext(),
				OSGiTestsActivator.TEST_FILES_ROOT + "configuration/bundle01");
		// not read-only yet, should work fine
		assertTrue(BundleTestingHelper.resolveBundles(getContext(), new Bundle[] { installed }),
				"installed bundle could not be resolved: " + installed);
	}

	/**
	 * Takes a snapshot of the file system.
	 */
	@Test
	@Order(2)
	@ExecuteInHost
	public void test1stSession() throws IOException {
		// compute and save tree image
		Path configurationDir = sessionConfiguration.getConfigurationDirectory();
		FileSystemComparator comparator = new FileSystemComparator();
		Object snapshot = comparator.takeSnapshot(configurationDir.toFile(), true);
		comparator.saveSnapshot(snapshot, configurationDir.toFile());
	}

	@Test
	@Order(3)
	@ExecuteInHost
	public void test1stSessionFollowUp() throws IOException {
		FileSystemComparator comparator = new FileSystemComparator();
		Path configurationDir = sessionConfiguration.getConfigurationDirectory();
		Object oldSnaphot = comparator.loadSnapshot(configurationDir.toFile());
		Object newSnapshot = comparator.takeSnapshot(configurationDir.toFile(), true);
		comparator.compareSnapshots("1.0", oldSnaphot, newSnapshot);
	}

	/**
	 * Tries to install a plug-in that has no manifest. Should fail because by
	 * default the manifest generation area is under the configuration area (which
	 * is read-only here)
	 */
	@Test
	@Order(4)
	@SuppressWarnings("deprecation") // installBundle
	public void test2ndSession() throws BundleException, IOException {
		// try to install plug-in
		// ensure it is not installed
		Bundle installed = null;
		try {
			installed = BundleTestingHelper.installBundle(getContext(),
					OSGiTestsActivator.TEST_FILES_ROOT + "configuration/bundle02");
			// should have failed with BundleException, does not have a bundle manifest
			fail("1.0");
		} catch (BundleException be) {
			// success
		} finally {
			if (installed != null)
				// clean-up - only runs if we end-up accepting an invalid manifest
				installed.uninstall();
		}
	}

	@Test
	@Order(5)
	@ExecuteInHost
	public void test2ndSessionFollowUp() throws IOException {
		FileSystemComparator comparator = new FileSystemComparator();
		Path configurationDir = sessionConfiguration.getConfigurationDirectory();
		Object oldSnaphot = comparator.loadSnapshot(configurationDir.toFile());
		Object newSnapshot = comparator.takeSnapshot(configurationDir.toFile(), true);
		comparator.compareSnapshots("1.0", oldSnaphot, newSnapshot);
	}

	/**
	 * Tries to install a plug-in that has manifest. Should fail because by default
	 * the manifest generation area is under the configuration area (which is
	 * read-only here)
	 */
	@Test
	@Order(6)
	@SuppressWarnings("deprecation") // installBundle
	public void test3rdSession() throws BundleException, IOException {
		// install plug-in
		// ensure it is not installed
		Bundle installed = null;
		try {
			installed = BundleTestingHelper.installBundle(getContext(),
					OSGiTestsActivator.TEST_FILES_ROOT + "configuration/bundle03");
			// should have failed - cannot install a bundle in read-only mode
			fail("1.0");
		} catch (BundleException be) {
			// success
		} finally {
			if (installed != null)
				// clean-up - only runs if we end-up accepting an invalid manifest
				installed.uninstall();
		}
	}

	@Test
	@Order(7)
	@ExecuteInHost
	public void test3rdSessionFollowUp() throws IOException {
		FileSystemComparator comparator = new FileSystemComparator();
		Path configurationDir = sessionConfiguration.getConfigurationDirectory();
		Object oldSnaphot = comparator.loadSnapshot(configurationDir.toFile());
		Object newSnapshot = comparator.takeSnapshot(configurationDir.toFile(), true);
		comparator.compareSnapshots("1.0", oldSnaphot, newSnapshot);
	}

}
