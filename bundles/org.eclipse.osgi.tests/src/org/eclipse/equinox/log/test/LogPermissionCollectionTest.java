/*******************************************************************************
 * Copyright (c) 2011 Cognos Incorporated, IBM Corporation and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.equinox.log.test;

import junit.framework.TestCase;
import org.eclipse.equinox.log.LogPermission;
import org.eclipse.equinox.log.LogPermissionCollection;

public class LogPermissionCollectionTest extends TestCase {

	public void testImplies() {

		LogPermission permission = new LogPermission("*", "*"); //$NON-NLS-1$//$NON-NLS-2$
		LogPermissionCollection permissionCollection = new LogPermissionCollection();
		assertFalse(permissionCollection.implies(permission));
		permissionCollection.add(permission);
		assertTrue(permissionCollection.implies(permission));
	}

}
