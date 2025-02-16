/*******************************************************************************
 *  Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Tasse - Add extra constructor to Path class (bug 454959)
 *******************************************************************************/
package org.eclipse.equinox.common.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

/**
 * Test cases for the Path class.
 */
public class PathTest {

	@Test
	public void testAddTrailingSeparator() {

		IPath with = new Path("/first/second/third/");
		IPath without = new Path("/first/second/third");

		assertSame("1.0", with, with.addTrailingSeparator());
		assertEquals("1.1", with, without.addTrailingSeparator());
		assertEquals("1.2", without, without.addTrailingSeparator());

		assertSame("2.0", IPath.ROOT, IPath.ROOT.addTrailingSeparator());
		assertEquals("2.1", IPath.ROOT, IPath.EMPTY.addTrailingSeparator());

		with = new Path("//first/second/third/");
		without = new Path("//first/second/third");

		assertSame("3.0", with, with.addTrailingSeparator());
		assertEquals("3.1", with, without.addTrailingSeparator());
		assertEquals("3.2", without, without.addTrailingSeparator());

		assertSame("4.0", IPath.ROOT, IPath.ROOT.addTrailingSeparator());
		assertEquals("4.1", IPath.ROOT, IPath.EMPTY.addTrailingSeparator());

		with = new Path("c:/first/second/third/");
		without = new Path("c:/first/second/third");

		assertSame("5.0", with, with.addTrailingSeparator());
		assertEquals("5.1", with, without.addTrailingSeparator());
		assertEquals("5.2", without, without.addTrailingSeparator());

		assertSame("6.0", IPath.ROOT, IPath.ROOT.addTrailingSeparator());
		assertEquals("6.1", IPath.ROOT, IPath.EMPTY.addTrailingSeparator());
	}

	@Test
	public void testAppend() {

		IPath fore = new Path("/first/second/third/");
		IPath win = IPath.forWindows("/first/second/third/");
		IPath posix = IPath.forPosix("/first/second/third/");
		String aftString = "/fourth/fifth";
		IPath aft = new Path(aftString);
		IPath combo = new Path("/first/second/third/fourth/fifth");

		assertEquals("1.0", combo, fore.append(aft));
		assertEquals("1.1", combo, fore.removeTrailingSeparator().append(aft));
		assertEquals("1.2", combo, IPath.ROOT.append(fore).append(aft));
		assertFalse("1.3", fore.append(aft).hasTrailingSeparator());
		assertFalse("1.4", IPath.ROOT.append(fore).append(aft).hasTrailingSeparator());
		assertFalse("1.5", fore.removeTrailingSeparator().append(aft).hasTrailingSeparator());
		// append empty and root path together
		assertEquals("1.6", IPath.EMPTY, IPath.EMPTY.append(IPath.EMPTY));
		assertEquals("1.7", IPath.EMPTY, IPath.EMPTY.append(IPath.ROOT));
		assertEquals("1.8", IPath.ROOT, IPath.ROOT.append(IPath.EMPTY));
		assertEquals("1.9", IPath.ROOT, IPath.ROOT.append(IPath.ROOT));

		assertEquals("2.0", combo, fore.append(aftString));
		assertEquals("2.1", combo, fore.removeTrailingSeparator().append(aftString));
		assertEquals("2.2", combo, IPath.ROOT.append(fore).append(aftString));
		assertTrue("2.3", !fore.append(aftString).hasTrailingSeparator());
		assertTrue("2.4", !IPath.ROOT.append(fore).append(aftString).hasTrailingSeparator());
		assertTrue("2.5", !fore.removeTrailingSeparator().append(aftString).hasTrailingSeparator());

		// ensure append preserves correct trailing separator
		assertTrue("3.0", !fore.append("aft").hasTrailingSeparator());
		assertTrue("3.1", fore.append("aft/").hasTrailingSeparator());
		assertTrue("3.2", !fore.append("/aft").hasTrailingSeparator());
		assertTrue("3.3", fore.append("/aft/").hasTrailingSeparator());
		assertTrue("3.4", !fore.append("\\aft").hasTrailingSeparator());
		// backslash is a trailing separator on windows only
		assertTrue("3.5.win", win.append("aft\\").hasTrailingSeparator());
		assertFalse("3.6.posix", posix.append("aft\\").hasTrailingSeparator());
		assertTrue("3.7", !fore.append("fourth/fifth").hasTrailingSeparator());
		assertTrue("3.8", fore.append("fourth/fifth/").hasTrailingSeparator());
		assertTrue("3.9", !fore.append(new Path("aft")).hasTrailingSeparator());
		assertTrue("3.10", fore.append(new Path("aft/")).hasTrailingSeparator());
		assertTrue("3.11", !fore.append(new Path("fourth/fifth")).hasTrailingSeparator());
		assertTrue("3.12", fore.append(new Path("fourth/fifth/")).hasTrailingSeparator());

		// make sure append converts backslashes appropriately
		aftString = "fourth\\fifth";
		assertEquals("4.0.win", combo, win.append(aftString));
		assertEquals("4.1.win", combo, win.removeTrailingSeparator().append(aftString));
		// append path to root path uses optimized code
		assertEquals("4.2.win", combo, IPath.forWindows("/").append(win).append(aftString));
		assertEquals("4.21.win", combo,
				IPath.forWindows("/").append(win).append("..").append("third").append(aftString));
		assertEquals("4.2X.win", IPath.forWindows("/").append("x"),
				IPath.forWindows("/").append("x").append("..").append("x"));
		assertEquals("4.2XY/.win", IPath.forWindows("\\").append("x/y"),
				IPath.forWindows("\\").append("x").append("y").append("../..").append("x/y"));
		assertEquals("4.2XY\\.win", IPath.forWindows("/").append("x\\y"),
				IPath.forWindows("/").append("x").append("y").append("..\\..").append("x\\y"));
		assertEquals("4.22.win", combo,
				IPath.forWindows("/").append(win).append("..\\..").append("second\\third").append(aftString));
		assertEquals("4.23.win", combo, IPath.forWindows("/").append(win).append("..\\..\\..")
				.append("first\\second\\third").append(aftString));
		assertEquals("4.24.win", combo,
				IPath.forWindows("/").append(win).append("..\\..\\..").append(win).append(aftString));
		assertEquals("4.25.win", combo,
				IPath.forWindows("/").append(win).append("../../..").append(win).append(aftString));
		assertEquals("4.3.win", combo, IPath.forWindows("/").append(posix).append(aftString));
		// append path to empty path uses optimized code
		assertEquals("4.4.win", combo, IPath.forWindows("").append(win).append(aftString).makeAbsolute());
		assertEquals("4.5.win", combo, IPath.forWindows("").append(posix).append(aftString).makeAbsolute());

		assertEquals("5.0", new Path("/foo"), IPath.ROOT.append("../foo"));
		assertEquals("5.1", new Path("/foo"), IPath.ROOT.append("./foo"));
		assertEquals("5.2", new Path("c:/foo/xyz"), new Path("c:/foo/bar").append("../xyz"));
		assertEquals("5.3", new Path("c:/foo/bar/xyz"), new Path("c:/foo/bar").append("./xyz"));

		// append preserves device and leading separator of receiver
		assertEquals("6.1.win", IPath.forWindows("c:foo/bar"), IPath.forWindows("c:").append("/foo/bar"));
		assertEquals("6.2.win", IPath.forWindows("c:foo/bar"), IPath.forWindows("c:").append("foo/bar"));
		assertEquals("6.3.win", IPath.forWindows("c:/foo/bar"), IPath.forWindows("c:/").append("/foo/bar"));
		assertEquals("6.4.win", IPath.forWindows("c:/foo/bar"), IPath.forWindows("c:/").append("foo/bar"));
		assertEquals("6.5.win", IPath.forWindows("c:foo/bar"), IPath.forWindows("c:").append("z:/foo/bar"));
		assertEquals("6.6.win", IPath.forWindows("c:foo/bar"), IPath.forWindows("c:").append("z:foo/bar"));
		assertEquals("6.7.win", IPath.forWindows("c:/foo/bar"), IPath.forWindows("c:/").append("z:/foo/bar"));
		assertEquals("6.8.win", IPath.forWindows("c:/foo/bar"), IPath.forWindows("c:/").append("z:foo/bar"));
		assertEquals("6.9.win", IPath.forWindows("c:/foo"), IPath.forWindows("c:/").append("z:foo"));
		assertEquals("6.10.posix", IPath.forPosix("c:/foo/bar"), IPath.forPosix("c:").append("/foo/bar"));
		assertEquals("6.11.posix", IPath.forPosix("c:/foo/bar/"), IPath.forPosix("c:").append("foo/bar/"));
		assertEquals("6.12.posix", IPath.forPosix("/c:/foo/bar"), IPath.forPosix("/c:").append("/foo/bar"));
		assertEquals("6.13.posix", IPath.forPosix("/c:/foo/bar"), IPath.forPosix("/c:").append("foo/bar"));

		assertEquals("6.14", new Path("foo/bar"), new Path("foo").append(new Path("/bar")));
		assertEquals("6.15", new Path("foo/bar"), new Path("foo").append(new Path("bar")));
		assertEquals("6.16", new Path("/foo/bar"), new Path("/foo/").append(new Path("/bar")));
		assertEquals("6.17", new Path("/foo/bar"), new Path("/foo/").append(new Path("bar")));

		assertEquals("6.18", new Path("foo/bar/"), new Path("foo").append(new Path("/bar/")));
		assertEquals("6.19", new Path("foo/bar/"), new Path("foo").append(new Path("bar/")));
		assertEquals("6.20", new Path("/foo/bar/"), new Path("/foo/").append(new Path("/bar/")));
		assertEquals("6.21", new Path("/foo/bar/"), new Path("/foo/").append(new Path("bar/")));

		// append preserves isUNC of receiver
		assertEquals("7.0", new Path("/foo/bar"), new Path("/foo").append("//bar"));
		assertEquals("7.1", new Path("/foo/bar/test"), new Path("/foo").append("bar//test"));
		assertEquals("7.2", new Path("//foo/bar"), new Path("//foo").append("bar"));
		assertEquals("7.3", new Path("/bar"), IPath.ROOT.append("//bar"));

		// append empty path does nothing
		assertEquals("8.0", fore, fore.append(IPath.ROOT));
		assertEquals("8.1", fore, fore.append(IPath.EMPTY));
		assertEquals("8.2", fore, fore.append(new Path("//")));
		assertEquals("8.3", fore, fore.append(new Path("/")));
		assertEquals("8.4", fore, fore.append(new Path("")));
		assertEquals("8.5", fore, fore.append("//"));
		assertEquals("8.6", fore, fore.append("/"));
		assertEquals("8.7", fore, fore.append(""));
		assertEquals("8.8.win", win, win.append("c://"));
		assertEquals("8.9.win", win, win.append("c:/"));
		assertEquals("8.10.win", win, win.append("c:"));

		// append string respects and preserves the initial path's file system
		IPath win1 = IPath.forWindows("a/b");
		IPath win2 = win1.append("c:d\\e");
		assertEquals("9.1.win", "a/b/d/e", win2.toString());
		assertEquals("9.2.win", null, win2.getDevice());
		assertEquals("9.3.win", 4, win2.segmentCount());
		assertEquals("9.4.win", "d", win2.segment(2));
		assertFalse("9.5.win", win2.isValidSegment(":"));
		IPath posix1 = IPath.forPosix("a/b");
		IPath posix2 = posix1.append("c:d\\e");
		assertEquals("9.6.posix", "a/b/c:d\\e", posix2.toString());
		assertEquals("9.7.posix", null, posix2.getDevice());
		assertEquals("9.8.posix", 3, posix2.segmentCount());
		assertEquals("9.9.posix", "c:d\\e", posix2.segment(2));
		assertTrue("9.10.posix", posix2.isValidSegment(":"));
		assertEquals("9.11", win1, posix1);
		assertNotEquals("9.12", win2, posix2);

		// append path respects and preserves the initial path's file system
		IPath win3 = win1.append(IPath.forPosix("c/d/e"));
		assertEquals("10.1.win", "a/b/c/d/e", win3.toString());
		assertEquals("10.2.win", null, win3.getDevice());
		assertEquals("10.3.win", 5, win3.segmentCount());
		assertEquals("10.4.win", "c", win3.segment(2));
		assertFalse("10.5.win", win3.isValidSegment(":"));
		IPath posix3 = posix1.append(IPath.forWindows("c\\d\\e"));
		assertEquals("10.6.posix", "a/b/c/d/e", posix3.toString());
		assertEquals("10.7.posix", null, posix3.getDevice());
		assertEquals("10.8.posix", 5, posix3.segmentCount());
		assertEquals("10.9.posix", "c", posix3.segment(2));
		assertTrue("10.10.posix", posix3.isValidSegment(":"));
		assertEquals("10.11", win3, posix3);

		// append POSIX path to Windows path may produce invalid segments
		IPath win4 = win1.append(IPath.forPosix("c:d\\e"));
		assertEquals("11.1.win", "a/b/c:d\\e", win4.toString());
		assertEquals("11.2.win", null, win4.getDevice());
		assertEquals("11.3.win", 3, win4.segmentCount());
		assertEquals("11.4.win", "c:d\\e", win4.segment(2));
		// isValidPath() considers it as device 'a/b/c:' with segments {'d','e'}
		assertTrue("11.5.win", win4.isValidPath(win4.toString()));
		assertFalse("11.6.win", win4.isValidSegment(win4.segment(2)));
	}

	@Test
	public void testSegmentCount() {

		assertEquals("1.0", 0, IPath.ROOT.segmentCount());
		assertEquals("1.1", 0, IPath.EMPTY.segmentCount());

		assertEquals("2.0", 1, new Path("/first").segmentCount());
		assertEquals("2.1", 1, new Path("/first/").segmentCount());
		assertEquals("2.2", 3, new Path("/first/second/third/").segmentCount());
		assertEquals("2.3", 3, new Path("/first/second/third").segmentCount());
		assertEquals("2.4", 5, new Path("/first/second/third/fourth/fifth").segmentCount());

		assertEquals("3.0", 0, new Path("//").segmentCount());
		assertEquals("3.1", 1, new Path("//first").segmentCount());
		assertEquals("3.2", 1, new Path("//first/").segmentCount());
		assertEquals("3.3", 2, new Path("//first/second").segmentCount());
		assertEquals("3.4", 2, new Path("//first/second/").segmentCount());
	}

	@Test
	public void testCanonicalize() {
		// Test collapsing multiple separators
		// double slashes at the beginning of a path
		// are left and assumed to be a UNC path
		assertEquals("//", new Path("///////").toString());
		assertEquals("/a/b/c", new Path("/a/b//c").toString());
		assertEquals("//a/b/c", new Path("//a/b//c").toString());
		assertEquals("a/b/c/", new Path("a/b//c//").toString());

		// Test collapsing single dots
		assertEquals("2.0", "/", new Path("/./././.").toString());
		assertEquals("2.1", "/a/b/c", new Path("/a/./././b/c").toString());
		assertEquals("2.2", "/a/b/c", new Path("/a/./b/c/.").toString());
		assertEquals("2.3", "a/b/c", new Path("a/./b/./c").toString());

		// Test collapsing double dots
		assertEquals("3.0", "/a/b", new Path("/a/b/c/..").toString());
		assertEquals("3.1", "/", new Path("/a/./b/../..").toString());
		assertEquals("3.2", "../", new Path("../").toString());
		// test bug 46043 - IPath collapseParentReferences
		assertEquals("3.3", "../", new Path("./../").toString());
		assertEquals("3.4", "../", new Path(".././").toString());
		assertEquals("3.5", "..", new Path("./..").toString());
		assertEquals("3.6", ".", new Path(".").toString());
	}

	@Test
	public void testClone() {

		IPath anyPath = new Path("/a/b/c");
		assertEquals("1.0", anyPath, anyPath.clone());
		anyPath = new Path("//a/b/c");
		assertEquals("1.1", anyPath, anyPath.clone());
		anyPath = new Path("c:/a/b/c");
		assertEquals("1.2", anyPath, anyPath.clone());

		assertEquals("1.3", IPath.ROOT, IPath.ROOT.clone());
	}

	@Test
	public void testConstructors() {

		assertEquals("1.0", "", new Path("").toString());
		assertEquals("1.1", "/", new Path("/").toString());
		assertEquals("1.2", "a", new Path("a").toString());
		assertEquals("1.3", "/a", new Path("/a").toString());
		assertEquals("1.4", "//", new Path("//").toString());
		assertEquals("1.5", "/a/", new Path("/a/").toString());
		assertEquals("1.6", "/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z",
				new Path("/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z").toString());
		assertEquals("1.7", "...", new Path("...").toString());
		assertEquals("1.8", "/a/b/.../c", new Path("/a/b/.../c").toString());

		IPath anyPath = new Path("/first/second/third");

		assertEquals("2.0", IPath.EMPTY, new Path(""));
		assertEquals("2.1", IPath.ROOT, new Path("/"));
		assertEquals("2.2", anyPath, anyPath);

		// should handle slash before the device (see bug 84697)
		// fullPath = new java.io.File("D:\\foo\\abc.txt").toURL().getPath()
		assertEquals("3.0.win", "D:/foo/abc.txt", IPath.forWindows("/D:/foo/abc.txt").toString());
		// fullPath = new java.io.File("D:/").toURL().getPath()
		assertEquals("3.1.win", "D:/", IPath.forWindows("/D:/").toString());
	}

	@Test
	public void testFactoryMethods() {

		IPath win = IPath.forWindows("a:b\\c/d");
		assertEquals("1.1.win", "a:b/c/d", win.toString());
		assertEquals("1.2.win", "a:", win.getDevice());
		assertEquals("1.3.win", 3, win.segmentCount());
		assertEquals("1.4.win", "b", win.segment(0));

		IPath posix = IPath.forPosix("a:b\\c/d");
		assertEquals("2.5.posix", "a:b\\c/d", posix.toString());
		assertEquals("2.6.posix", null, posix.getDevice());
		assertEquals("2.7.posix", 2, posix.segmentCount());
		assertEquals("2.8.posix", "a:b\\c", posix.segment(0));

		assertNotEquals("3.1", win, posix);
	}

	@Test
	public void testFirstSegment() {

		assertNull("1.0", IPath.ROOT.segment(0));
		assertNull("1.1", IPath.EMPTY.segment(0));

		assertEquals("2.0", "a", new Path("/a/b/c").segment(0));
		assertEquals("2.1", "a", new Path("a").segment(0));
		assertEquals("2.2", "a", new Path("/a").segment(0));
		assertEquals("2.3", "a", new Path("a/b").segment(0));
		assertEquals("2.4", "a", new Path("//a/b").segment(0));
		assertEquals("2.5.win", "a", IPath.forWindows("c:a/b").segment(0));
		assertEquals("2.6.win", "a", IPath.forWindows("c:/a/b").segment(0));
		assertEquals("2.7.posix", "c:", IPath.forPosix("c:/a/b").segment(0));
		assertEquals("2.8.posix", "c:", IPath.forPosix("c:/a\\b").segment(0));
		assertEquals("2.9.posix", "a", IPath.forPosix("a/c:/b").segment(0));
		assertEquals("2.10.posix", "a\\b", IPath.forPosix("a\\b/b").segment(0));

	}

	@Test
	public void testFromOSString() {
		List<String> segments = List.of("first", "first/second/third");
		for (String segment : segments) {
			assertEquals(IPath.fromPortableString(segment), IPath.fromOSString(osString(segment)));
			assertEquals(IPath.fromPortableString(segment + "/"), IPath.fromOSString(osString(segment + "/")));
			assertEquals(IPath.fromPortableString("/" + segment), IPath.fromOSString(osString("/" + segment)));
			assertEquals(IPath.fromPortableString("/" + segment + "/"),
					IPath.fromOSString(osString("/" + segment + "/")));
		}
	}

	private static String osString(String pathname) {
		return new java.io.File(pathname).toString();
	}

	@Test
	public void testFromPortableString() {
		assertEquals("1.0", "", IPath.fromPortableString("").toString());
		assertEquals("1.1", "/", IPath.fromPortableString("/").toString());
		assertEquals("1.2", "a", IPath.fromPortableString("a").toString());
		assertEquals("1.3", "/a", IPath.fromPortableString("/a").toString());
		assertEquals("1.4", "//", IPath.fromPortableString("//").toString());
		assertEquals("1.5", "/a/", IPath.fromPortableString("/a/").toString());

		assertEquals("2.1", "a:", IPath.fromPortableString("a:").toString());
		assertEquals("2.2", "a:", IPath.fromPortableString("a::").toString());
		assertEquals("2.3", "a:b:", IPath.fromPortableString("a:b::").toString());
		assertEquals("2.4", "a/b:c", IPath.fromPortableString("a/b::c").toString());
		assertEquals("2.5", "a/b:c", IPath.fromPortableString("a/b:c").toString());
		assertEquals("2.6", "a:b", IPath.fromPortableString("a::b").toString());

		boolean isLocalPosix = java.io.File.separatorChar == '/';
		IPath win1 = IPath.forWindows("a:b\\c/d");
		IPath win2 = IPath.fromPortableString(win1.toPortableString());
		assertEquals("3.1.win", "a:b/c/d", win2.toString());
		assertEquals("3.2.win", "a:", win2.getDevice());
		assertEquals("3.3.win", 3, win2.segmentCount());
		assertEquals("3.4.win", "b", win2.segment(0));
		assertEquals("3.5.win", win1, win2);
		assertEquals("3.6.win", isLocalPosix, win2.isValidSegment(":"));
		IPath posix1 = IPath.forPosix("a:b\\c/d");
		IPath posix2 = IPath.fromPortableString(posix1.toPortableString());
		assertEquals("3.7.posix", "a:b\\c/d", posix2.toString());
		assertEquals("3.8.posix", null, posix2.getDevice());
		assertEquals("3.9.posix", 2, posix2.segmentCount());
		assertEquals("3.10.posix", "a:b\\c", posix2.segment(0));
		assertEquals("3.11.posix", posix1, posix2);
		assertEquals("3.12.posix", isLocalPosix, posix2.isValidSegment(":"));
	}

	@Test
	public void testFromFile() {
		List<String> segments = List.of("first", "first/second/third");
		for (String segment : segments) {
			File file0 = new File(segment);
			assertEquals(file0, IPath.fromFile(file0).toFile());
			File file1 = new File(segment + "/");
			assertEquals(file1, IPath.fromFile(file1).toFile());
			File file2 = new File("/" + segment);
			assertEquals(file2, IPath.fromFile(file2).toFile());
			File file3 = new File("/" + segment + "/");
			assertEquals(file3, IPath.fromFile(file3).toFile());
		}
	}

	@Test
	public void testFromPath() {
		List<String> segments = List.of("first", "first/second/third");
		for (String segment : segments) {
			java.nio.file.Path path0 = java.nio.file.Path.of(segment);
			assertEquals(path0, IPath.fromPath(path0).toPath());
			java.nio.file.Path path1 = java.nio.file.Path.of(segment + "/");
			assertEquals(path1, IPath.fromPath(path1).toPath());
			java.nio.file.Path path2 = java.nio.file.Path.of("/" + segment);
			assertEquals(path2, IPath.fromPath(path2).toPath());
			java.nio.file.Path path3 = java.nio.file.Path.of("/" + segment + "/");
			assertEquals(path3, IPath.fromPath(path3).toPath());
		}
	}

	@Test
	public void testGetFileExtension() {

		IPath anyPath = new Path("index.html");
		assertEquals("1.0", anyPath.getFileExtension(), "html");

		assertNull("2.0", IPath.ROOT.getFileExtension());
		assertNull("2.1", IPath.EMPTY.getFileExtension());
		assertNull("2.2", new Path("index").getFileExtension());
		assertNull("2.3", new Path("/a/b/c.txt/").getFileExtension());

		assertEquals("3.0", "txt", new Path("/a/b/c.txt").getFileExtension());
		assertEquals("3.1", "txt", new Path("/a/b/c.foo.txt").getFileExtension());
		assertEquals("3.2", "txt", new Path("//a/b/c.foo.txt").getFileExtension());
		assertEquals("3.3", "txt", new Path("c:/a/b/c.foo.txt").getFileExtension());
		assertEquals("3.4", "txt", new Path("c:a/b/c.foo.txt").getFileExtension());

	}

	@Test
	public void testHasTrailingSeparator() {

		// positive
		assertTrue("1.0", new Path("/first/second/third/").hasTrailingSeparator());
		assertTrue("1.1", new Path("//first/second/third/").hasTrailingSeparator());
		assertTrue("1.2", new Path("c:/first/second/third/").hasTrailingSeparator());
		assertTrue("1.3", new Path("c:first/second/third/").hasTrailingSeparator());

		// negative
		assertTrue("2.0", !new Path("first/second/third").hasTrailingSeparator());
		assertTrue("2.1", !IPath.ROOT.hasTrailingSeparator());
		assertTrue("2.2", !IPath.EMPTY.hasTrailingSeparator());
		assertTrue("2.3", !new Path("//first/second/third").hasTrailingSeparator());
		assertTrue("2.4", !new Path("c:/first/second/third").hasTrailingSeparator());
		assertTrue("2.5", !new Path("c:first/second/third").hasTrailingSeparator());

		// paths of length 0 never have a trailing separator
		assertTrue("3.0", !new Path("/first/").removeLastSegments(1).hasTrailingSeparator());
		assertTrue("3.1", !new Path("/first/").removeFirstSegments(1).hasTrailingSeparator());
		assertTrue("3.2", !new Path("/").hasTrailingSeparator());
		assertTrue("3.3", !new Path("/first/").append("..").hasTrailingSeparator());
		assertTrue("3.4", !new Path("/first/").append(new Path("..")).hasTrailingSeparator());
		assertTrue("3.5", !new Path("/first/../").hasTrailingSeparator());
		assertTrue("3.6", !IPath.ROOT.addTrailingSeparator().hasTrailingSeparator());
		assertTrue("3.7", !IPath.EMPTY.addTrailingSeparator().hasTrailingSeparator());

	}

	@Test
	public void testIsAbsolute() {

		// positive
		assertTrue("1.0", new Path("/first/second/third").isAbsolute());
		assertTrue("1.1", IPath.ROOT.isAbsolute());
		assertTrue("1.2", new Path("//first/second/third").isAbsolute());
		assertTrue("1.3.win", IPath.forWindows("c:/first/second/third").isAbsolute());
		assertTrue("1.4.posix", IPath.forPosix("/c:first/second/third").isAbsolute());

		// negative
		assertTrue("2.0", !new Path("first/second/third").isAbsolute());
		assertTrue("2.1", !IPath.EMPTY.isAbsolute());
		assertTrue("2.2", !new Path("c:first/second/third").isAbsolute());

		// unc
		assertTrue("3.0.win", IPath.forWindows("c://").isAbsolute());
		assertTrue("3.1.posix", IPath.forPosix("//c:/").isAbsolute());
		assertTrue("3.2", new Path("//").isAbsolute());
		assertTrue("3.3", new Path("//a").isAbsolute());
		assertTrue("3.4", new Path("//a/b/").isAbsolute());

	}

	@Test
	public void testIsEmpty() {

		// positive
		assertTrue("1.0", IPath.EMPTY.isEmpty());
		assertTrue("1.1", new Path("//").isEmpty());
		assertTrue("1.2", new Path("").isEmpty());
		assertTrue("1.3.win", IPath.forWindows("c:").isEmpty());
		assertFalse("1.4.posix", IPath.forPosix("c:").isEmpty());
		assertTrue("1.5", new Path("///").isEmpty());

		// negative
		assertTrue("2.0", !new Path("first/second/third").isEmpty());
		assertTrue("2.1", !IPath.ROOT.isEmpty());
		assertTrue("2.2", !new Path("//a").isEmpty());
		assertTrue("2.3", !new Path("c:/").isEmpty());
	}

	@Test
	public void testIsPrefixOf() {

		IPath prefix = new Path("/first/second");
		IPath path = new Path("/first/second/third/fourth");

		assertTrue("1.0", prefix.isPrefixOf(path));
		// test the case where the arg is longer than the receiver.
		assertTrue("1.1", !path.isPrefixOf(prefix));
		assertTrue("1.2", !new Path("fifth/sixth").isPrefixOf(path));

		assertTrue("2.0", prefix.addTrailingSeparator().isPrefixOf(path));

		assertTrue("3.0", IPath.ROOT.isPrefixOf(path));
		assertTrue("3.1", IPath.EMPTY.isPrefixOf(path));
		assertTrue("3.2", !path.isPrefixOf(IPath.ROOT));
		assertTrue("3.3", !path.isPrefixOf(IPath.EMPTY));
	}

	@Test
	public void testIsRoot() {

		// negative
		assertTrue("1.0", !new Path("/first/second").isRoot());
		assertTrue("1.1", !IPath.EMPTY.isRoot());
		assertTrue("1.2", !new Path("//").isRoot());
		assertTrue("1.3", !new Path("///").isRoot());

		// positive
		assertTrue("2.0", IPath.ROOT.isRoot());
		assertTrue("2.1", new Path("/").isRoot());
		assertTrue("2.2.win", IPath.forWindows("/").isRoot());
		assertTrue("2.3.posix", IPath.forPosix("/").isRoot());
	}

	@Test
	public void testIsUNC() {

		// negative
		assertTrue("1.0", !IPath.ROOT.isUNC());
		assertTrue("1.1", !IPath.EMPTY.isUNC());

		assertTrue("2.0", !new Path("a").isUNC());
		assertTrue("2.1", !new Path("a/b").isUNC());
		assertTrue("2.2", !new Path("/a").isUNC());
		assertTrue("2.3", !new Path("/a/b").isUNC());

		assertTrue("3.0", !new Path("c:/a/b").isUNC());
		assertTrue("3.1", !new Path("c:a/b").isUNC());
		assertTrue("3.2", !new Path("/F/../").isUNC());

		assertTrue("4.0", !new Path("c://a/").isUNC());
		assertTrue("4.1", !new Path("c:\\/a/b").isUNC());
		assertTrue("4.2", !new Path("c:\\\\").isUNC());

		// positive
		assertTrue("5.0", new Path("//").isUNC());
		assertTrue("5.1", new Path("//a").isUNC());
		assertTrue("5.2", new Path("//a/b").isUNC());
		assertTrue("5.3.win", IPath.forWindows("\\\\ThisMachine\\HOME\\foo.jar").isUNC());

		assertTrue("6.0.win", IPath.forWindows("c://a/").setDevice(null).isUNC());
		assertTrue("6.1.win", IPath.forWindows("c:\\/a/b").setDevice(null).isUNC());
		assertTrue("6.2.win", IPath.forWindows("c:\\\\").setDevice(null).isUNC());
	}

	@Test
	public void testIsValidPath() {
		IPath test = IPath.ROOT;
		// positive
		assertTrue("1.0", test.isValidPath("/first/second/third"));
		assertTrue("1.1", test.isValidPath(""));
		assertTrue("1.2", test.isValidPath("a"));
		assertTrue("1.3", test.isValidPath("c:"));
		assertTrue("1.4", test.isValidPath("//"));
		assertTrue("1.5", test.isValidPath("//a"));
		assertTrue("1.6", test.isValidPath("c:/a"));
		assertTrue("1.7", test.isValidPath("c://a//b//c//d//e//f"));
		assertTrue("1.8", test.isValidPath("//a//b//c//d//e//f"));

		// platform-dependent
		assertFalse("2.1.win", IPath.forWindows("").isValidPath("c:b:"));
		assertFalse("2.2.win", IPath.forWindows("").isValidPath("c:a/b:"));
		assertTrue("2.3.posix", IPath.forPosix("").isValidPath("c:b:"));
		assertTrue("2.4.posix", IPath.forPosix("").isValidPath("c:a/b:"));

		// static methods
		assertFalse("3.1.win", Path.isValidWindowsPath("c:b:"));
		assertFalse("3.2.win", Path.isValidWindowsPath("c:a/b:"));
		assertTrue("3.3.posix", Path.isValidPosixPath("c:b:"));
		assertTrue("3.4.posix", Path.isValidPosixPath("c:a/b:"));
	}

	@Test
	public void testIsValidSegment() {
		IPath test = IPath.ROOT;
		// positive
		assertTrue("1.0", test.isValidSegment("a"));

		// negative
		assertFalse("2.1", test.isValidSegment(""));
		assertFalse("2.2", test.isValidSegment("/"));

		// platform-dependent
		assertFalse("3.1.win", IPath.forWindows("").isValidSegment("\\"));
		assertFalse("3.2.win", IPath.forWindows("").isValidSegment(":"));
		assertTrue("3.3.posix", IPath.forPosix("").isValidSegment("\\"));
		assertTrue("3.4.posix", IPath.forPosix("").isValidSegment(":"));

		// static methods
		assertFalse("4.1.win", Path.isValidWindowsSegment("\\"));
		assertFalse("4.2.win", Path.isValidWindowsSegment(":"));
		assertTrue("4.3.posix", Path.isValidPosixSegment("\\"));
		assertTrue("4.4.posix", Path.isValidPosixSegment(":"));

		// path constants and Path(String) always on local platform
		boolean isLocalPosix = java.io.File.separatorChar == '/';
		assertEquals("5.1", isLocalPosix, IPath.EMPTY.isValidSegment(":"));
		assertEquals("5.2", isLocalPosix, IPath.ROOT.isValidSegment(":"));
		assertEquals("5.3", isLocalPosix, new Path("").isValidSegment(":"));
	}

	@Test
	public void testLastSegment() {

		assertEquals("1.0", "second", new Path("/first/second").lastSegment());

		assertEquals("2.0", "first", new Path("first").lastSegment());
		assertEquals("2.1", "first", new Path("/first/").lastSegment());
		assertEquals("2.2", "second", new Path("first/second").lastSegment());
		assertEquals("2.3", "second", new Path("first/second/").lastSegment());

		assertNull("3.0", IPath.EMPTY.lastSegment());
		assertNull("3.1", IPath.ROOT.lastSegment());
		assertNull("3.2", new Path("//").lastSegment());

		assertEquals("4.0", "second", new Path("//first/second/").lastSegment());
		assertEquals("4.1", "second", new Path("//first/second").lastSegment());
		assertEquals("4.2", "second", new Path("c:/first/second/").lastSegment());
		assertEquals("4.3", "second", new Path("c:first/second/").lastSegment());

		assertEquals("5.0", "first", new Path("//first").lastSegment());
		assertEquals("5.1", "first", new Path("//first/").lastSegment());
	}

	@Test
	public void testMakeAbsolute() {
		IPath anyPath = new Path("first/second/third").makeAbsolute();
		assertTrue("1.0", anyPath.isAbsolute());
		assertEquals("1.1", new Path("/first/second/third"), anyPath);

		anyPath = new Path("").makeAbsolute();
		assertTrue("2.0", anyPath.isAbsolute());
		assertEquals("2.1", IPath.ROOT, anyPath);
	}

	@Test
	public void testMakeRelative() {
		IPath anyPath = new Path("/first/second/third").makeRelative();
		assertTrue("1.0", !anyPath.isAbsolute());
		assertEquals("1.1", new Path("first/second/third"), anyPath);

		anyPath = IPath.ROOT.makeRelative();
		assertTrue("2.0", !anyPath.isAbsolute());
		assertEquals("2.1", new Path(""), anyPath);
	}

	/**
	 * Tests for {@link Path#makeRelativeTo(IPath)}.
	 */
	@Test
	public void testMakeRelativeTo() {
		// valid cases
		IPath[] bases = new IPath[] { new Path("/a/"), new Path("/a/b") };
		IPath[] children = new IPath[] { new Path("/a/"), new Path("/a/b"), new Path("/a/b/c") };
		for (int i = 0; i < bases.length; i++) {
			for (int j = 0; j < children.length; j++) {
				final IPath base = bases[i];
				final IPath child = children[j];
				IPath result = child.makeRelativeTo(base);
				assertTrue("1." + i + ',' + j, !result.isAbsolute());
				assertEquals("2." + i + ',' + j, base.append(result), child);
			}
		}

		// for equal/identical paths, the relative path should be empty
		IPath equalBase = new Path("/a/b");
		assertEquals("3.1", "", new Path("/a/b").makeRelativeTo(equalBase).toString());
		assertEquals("3.2", "", new Path("/a/b/").makeRelativeTo(equalBase).toString());
		assertEquals("3.3", "", equalBase.makeRelativeTo(equalBase).toString());

		// invalid cases (no common prefix)
		bases = new IPath[] { new Path("/"), new Path("/b"), new Path("/b/c") };
		children = new IPath[] { new Path("/a/"), new Path("/a/b"), new Path("/a/b/c") };
		for (int i = 0; i < bases.length; i++) {
			for (int j = 0; j < children.length; j++) {
				final IPath base = bases[i];
				final IPath child = children[j];
				IPath result = child.makeRelativeTo(base);
				assertTrue("4." + i + ',' + j, !result.isAbsolute());
				assertEquals("5." + i + ',' + j, base.append(result), child);
			}
		}
	}

	/**
	 * Tests for {@link Path#makeRelativeTo(IPath)}.
	 */
	@Test
	public void testMakeRelativeToWindows() {
		IPath[] bases = new IPath[] { IPath.forWindows("c:/a/"), IPath.forWindows("c:/a/b") };
		IPath[] children = new IPath[] { IPath.forWindows("d:/a/"), IPath.forWindows("d:/a/b"),
				IPath.forWindows("d:/a/b/c") };
		for (int i = 0; i < bases.length; i++) {
			for (int j = 0; j < children.length; j++) {
				final IPath base = bases[i];
				final IPath child = children[j];
				IPath result = child.makeRelativeTo(base);
				assertTrue("1." + i + ".win," + j, result.isAbsolute());
				assertEquals("2." + i + ".win," + j, child, result);
			}
		}

	}

	@Test
	public void testMakeUNC() {
		List<IPath> inputs = new ArrayList<>();
		List<String> expected = new ArrayList<>();
		List<String> expectedNon = new ArrayList<>();

		inputs.add(IPath.ROOT);
		expected.add("//");
		expectedNon.add("/");

		inputs.add(IPath.EMPTY);
		expected.add("//");
		expectedNon.add("");

		inputs.add(new Path("a"));
		expected.add("//a");
		expectedNon.add("a");

		inputs.add(new Path("a/b"));
		expected.add("//a/b");
		expectedNon.add("a/b");

		inputs.add(new Path("/a/b/"));
		expected.add("//a/b/");
		expectedNon.add("/a/b/");

		inputs.add(new Path("//"));
		expected.add("//");
		expectedNon.add("/");

		inputs.add(new Path("//a"));
		expected.add("//a");
		expectedNon.add("/a");

		inputs.add(new Path("//a/b"));
		expected.add("//a/b");
		expectedNon.add("/a/b");

		inputs.add(new Path("//a/b/"));
		expected.add("//a/b/");
		expectedNon.add("/a/b/");

		inputs.add(new Path("c:", "/"));
		expected.add("//");
		expectedNon.add("c:/");

		inputs.add(new Path("c:", ""));
		expected.add("//");
		expectedNon.add("c:");

		inputs.add(new Path("c:", "a"));
		expected.add("//a");
		expectedNon.add("c:a");

		inputs.add(new Path("c:", "a/b"));
		expected.add("//a/b");
		expectedNon.add("c:a/b");

		inputs.add(new Path("c:", "/a"));
		expected.add("//a");
		expectedNon.add("c:/a");

		inputs.add(new Path("c:", "/a/b"));
		expected.add("//a/b");
		expectedNon.add("c:/a/b");

		assertEquals("0.0", inputs.size(), expected.size());
		assertEquals("0.1", inputs.size(), expectedNon.size());

		for (int i = 0; i < inputs.size(); i++) {
			IPath path = inputs.get(i);
			IPath result = path.makeUNC(true);
			assertTrue("1.0." + path + " (" + result + ")", result.isUNC());
			assertEquals("1.1." + path, expected.get(i), result.toString());
			result = path.makeUNC(false);
			assertTrue("1.2." + path, !result.isUNC());
			assertEquals("1.3." + path, expectedNon.get(i), result.toString());
		}
	}

	/**
	 * This test is for bizarre cases that previously caused errors.
	 */
	@Test
	public void testRegression() {
		new Path("C:\\/eclipse");

		IPath path = IPath.forWindows("d:\\\\ive");
		assertTrue("2.0.win", !path.isUNC());
		assertEquals("2.1.win", 1, path.segmentCount());
		assertEquals("2.2.win", "ive", path.segment(0));
	}

	@Test
	public void testRemoveFirstSegments() {
		assertEquals("1.0", new Path("second"), new Path("/first/second").removeFirstSegments(1));
		assertEquals("1.1", new Path("second/third/"), new Path("/first/second/third/").removeFirstSegments(1));
		assertEquals("1.2", IPath.EMPTY, new Path("first").removeFirstSegments(1));
		assertEquals("1.3", IPath.EMPTY, new Path("/first/").removeFirstSegments(1));
		assertEquals("1.4", new Path("second"), new Path("first/second").removeFirstSegments(1));
		assertEquals("1.5", IPath.EMPTY, new Path("").removeFirstSegments(1));
		assertEquals("1.6", IPath.EMPTY, IPath.ROOT.removeFirstSegments(1));
		assertEquals("1.7", IPath.EMPTY, new Path("/first/second/").removeFirstSegments(2));
		assertEquals("1.8", IPath.EMPTY, new Path("/first/second/").removeFirstSegments(3));
		assertEquals("1.9", new Path("third/fourth"), new Path("/first/second/third/fourth").removeFirstSegments(2));

		assertEquals("2.0.win", IPath.forWindows("c:second"),
				IPath.forWindows("c:/first/second").removeFirstSegments(1));
		assertEquals("2.1.win", IPath.forWindows("c:second/third/"),
				IPath.forWindows("c:/first/second/third/").removeFirstSegments(1));
		assertEquals("2.2.win", IPath.forWindows("c:"), IPath.forWindows("c:first").removeFirstSegments(1));
		assertEquals("2.3.win", IPath.forWindows("c:"), IPath.forWindows("c:/first/").removeFirstSegments(1));
		assertEquals("2.4.win", IPath.forWindows("c:second"),
				IPath.forWindows("c:first/second").removeFirstSegments(1));
		assertEquals("2.5.win", IPath.forWindows("c:"), IPath.forWindows("c:").removeFirstSegments(1));
		assertEquals("2.6.win", IPath.forWindows("c:"), IPath.forWindows("c:/").removeFirstSegments(1));
		assertEquals("2.7.win", IPath.forWindows("c:"), IPath.forWindows("c:/first/second/").removeFirstSegments(2));
		assertEquals("2.8.win", IPath.forWindows("c:"), IPath.forWindows("c:/first/second/").removeFirstSegments(3));
		assertEquals("2.9.win", IPath.forWindows("c:third/fourth"),
				IPath.forWindows("c:/first/second/third/fourth").removeFirstSegments(2));

		assertEquals("3.0", new Path("second"), new Path("//first/second").removeFirstSegments(1));
		assertEquals("3.1", new Path("second/third/"), new Path("//first/second/third/").removeFirstSegments(1));
		assertEquals("3.2", IPath.EMPTY, new Path("//first/").removeFirstSegments(1));
		assertEquals("3.3", IPath.EMPTY, new Path("//").removeFirstSegments(1));
		assertEquals("3.4", IPath.EMPTY, new Path("//first/second/").removeFirstSegments(2));
		assertEquals("3.5", IPath.EMPTY, new Path("//first/second/").removeFirstSegments(3));
		assertEquals("3.6", new Path("third/fourth"), new Path("//first/second/third/fourth").removeFirstSegments(2));
	}

	@Test
	public void testRemoveLastSegments() {

		assertEquals("1.0", new Path("/first"), new Path("/first/second").removeLastSegments(1));
		assertEquals("1.1", new Path("//first"), new Path("//first/second").removeLastSegments(1));
		assertEquals("1.2", new Path("c:/first"), new Path("c:/first/second").removeLastSegments(1));
		assertEquals("1.3", new Path("c:first"), new Path("c:first/second").removeLastSegments(1));

		assertEquals("2.0", new Path("/first/second/"), new Path("/first/second/third/").removeLastSegments(1));
		assertEquals("2.1", new Path("//first/second/"), new Path("//first/second/third/").removeLastSegments(1));
		assertEquals("2.2", new Path("c:/first/second/"), new Path("c:/first/second/third/").removeLastSegments(1));
		assertEquals("2.3", new Path("c:first/second/"), new Path("c:first/second/third/").removeLastSegments(1));

		assertEquals("3.0", IPath.EMPTY, new Path("first").removeLastSegments(1));
		assertEquals("3.1", IPath.ROOT, new Path("/first/").removeLastSegments(1));
		assertEquals("3.2", new Path("first"), new Path("first/second").removeLastSegments(1));

		assertEquals("4.0", IPath.EMPTY, new Path("").removeLastSegments(1));
		assertEquals("4.1", IPath.ROOT, IPath.ROOT.removeLastSegments(1));
		assertEquals("4.2", new Path("//"), new Path("//").removeLastSegments(1));
	}

	@Test
	public void testRemoveTrailingSeparator() {

		IPath with = new Path("/first/second/third/");
		IPath without = new Path("/first/second/third");

		assertSame("1.0", without, without.removeTrailingSeparator());
		assertEquals("1.1", without, with.removeTrailingSeparator());
		// trailing separators have no bearing on path equality so check via
		// other means....
		assertTrue("1.2", !with.removeTrailingSeparator().hasTrailingSeparator());
		assertTrue("1.3", !without.hasTrailingSeparator());
		assertEquals("1.4", without.toString(), with.removeTrailingSeparator().toString());

		assertSame("2.0", IPath.ROOT, IPath.ROOT.removeTrailingSeparator());
		assertEquals("2.1", IPath.EMPTY, new Path("").removeTrailingSeparator());

		assertEquals("3.0", new Path("//"), new Path("//").removeTrailingSeparator());
		assertEquals("3.1", new Path("//a"), new Path("//a").removeTrailingSeparator());
		assertEquals("3.2", new Path("//a"), new Path("//a/").removeTrailingSeparator());
		assertEquals("3.3", new Path("//a/b"), new Path("//a/b").removeTrailingSeparator());
		assertEquals("3.4", new Path("//a/b"), new Path("//a/b/").removeTrailingSeparator());

		assertEquals("4.0", new Path("c:"), new Path("c:").removeTrailingSeparator());
		assertEquals("4.1", new Path("c:/"), new Path("c:/").removeTrailingSeparator());
		assertEquals("4.2", new Path("c:/a"), new Path("c:/a/").removeTrailingSeparator());
		assertEquals("4.3", new Path("c:/a/b"), new Path("c:/a/b").removeTrailingSeparator());
		assertEquals("4.4", new Path("c:/a/b"), new Path("c:/a/b/").removeTrailingSeparator());

		assertEquals("5.0", new Path("c:a"), new Path("c:a/").removeTrailingSeparator());
		assertEquals("5.1", new Path("c:a/b"), new Path("c:a/b").removeTrailingSeparator());
		assertEquals("5.2", new Path("c:a/b"), new Path("c:a/b/").removeTrailingSeparator());
	}

	@Test
	public void testSegments() {

		IPath anyPath = null;
		String[] segs = null;

		// Case One: typical case
		anyPath = new Path("/first/second/third/fourth");
		segs = anyPath.segments();

		assertEquals("1.0", 4, segs.length);
		assertEquals("1.1", "first", segs[0]);
		assertEquals("1.2", "second", segs[1]);
		assertEquals("1.3", "third", segs[2]);
		assertEquals("1.4", "fourth", segs[3]);

		// Case Two: trailing separator
		anyPath = new Path("/first/second/");
		segs = anyPath.segments();

		assertEquals("2.0", 2, segs.length);
		assertEquals("2.1", "first", segs[0]);
		assertEquals("2.2", "second", segs[1]);

		// Case Three: no leading or trailing separators
		anyPath = new Path("first/second");
		segs = anyPath.segments();

		assertEquals("3.0", 2, segs.length);
		assertEquals("3.1", "first", segs[0]);
		assertEquals("3.2", "second", segs[1]);

		// Case Four: single segment
		anyPath = new Path("first");
		segs = anyPath.segments();

		assertEquals("4.0", 1, segs.length);
		assertEquals("4.1", "first", segs[0]);

		// Case Five(a): no segments
		anyPath = IPath.EMPTY;
		segs = anyPath.segments();

		assertEquals("5.0", 0, segs.length);

		// Case Five(b): no segments
		anyPath = IPath.ROOT;
		segs = anyPath.segments();

		assertEquals("6.0", 0, segs.length);

		// Case Six: UNC path
		anyPath = new Path("//server/volume/a/b/c");
		segs = anyPath.segments();
		assertEquals("7.0", 5, segs.length);
		assertEquals("7.1", "server", segs[0]);
		assertEquals("7.2", "volume", segs[1]);
		assertEquals("7.3", "a", segs[2]);
		assertEquals("7.4", "b", segs[3]);
		assertEquals("7.5", "c", segs[4]);
	}

	@Test
	public void testToString() {

		IPath anyPath = new Path("/first/second/third");
		assertEquals("1.0", "/first/second/third", anyPath.toString());

		assertEquals("1.1", "/", IPath.ROOT.toString());
		assertEquals("1.2", "", IPath.EMPTY.toString());
	}

	@Test
	public void testHash() {
		// actual hash codes may depend on JDK implementation of String.hashCode()

		assertEquals("a", new Path("a").hashCode(), new Path("a").hashCode());
		// small chance a VM may actually provide equals hashes for "a" and "b" which
		// would result in failing test:
		if ("a".hashCode() != "b".hashCode()) {
			assertNotEquals("a vs b", new Path("a").hashCode(), new Path("b").hashCode());
		}
		if ("d".hashCode() != "e".hashCode()) {
			assertNotEquals("d vs e", new Path("d").hashCode(), new Path("e").hashCode());
		}
		assertEquals("b", new Path("b").hashCode(), new Path("b").hashCode());
		assertEquals("c", new Path("c:\\d").hashCode(), new Path("c:\\d").hashCode());
		assertEquals("cd", new Path("c:\\").append("d").hashCode(), new Path("c:\\").append("d").hashCode());
		assertEquals("cd", IPath.forWindows("c:\\d").hashCode(), IPath.forWindows("c:\\").append("d").hashCode());
		assertEquals("OS independent", IPath.forWindows("p").append("d").hashCode(),
				IPath.forPosix("p").append("d").hashCode());
		assertEquals("OS independent", IPath.forWindows("p").append("d").hashCode(),
				IPath.forPosix("p").append("d").hashCode());
		assertEquals("trailing independent", new Path("p").removeTrailingSeparator().hashCode(),
				new Path("p").addTrailingSeparator().hashCode());
	}

	@Test
	public void testEquals() {
		assertEquals("a", new Path("a"), new Path("a"));
		assertEquals("b", new Path("a"), new Path("a"));
		assertEquals("c", new Path("c").append("d"), new Path("c").append("d"));
		assertEquals("c", new Path("c:\\d"), new Path("c:\\d"));
		assertEquals("c", IPath.forWindows("c:\\d"), IPath.forWindows("c:\\d"));
		assertEquals("c", IPath.forPosix("c:\\d"), IPath.forPosix("c:\\d"));
		assertEquals("c", IPath.forWindows("c:/d"), IPath.forWindows("c:/d"));
		assertEquals("c", IPath.forPosix("c:/d"), IPath.forPosix("c:/d"));
		assertEquals("cd", IPath.forWindows("c:\\d"), IPath.forWindows("c:\\").append("d"));
		assertEquals("trailing independent", new Path("p").removeTrailingSeparator(),
				new Path("p").addTrailingSeparator());
		assertEquals("OS independent", IPath.forWindows("p"), IPath.forPosix("p"));
		Path hashed = new Path("a");
		hashed.hashCode();
		assertEquals("hash independent", new Path("a"), hashed);

		assertNotEquals("unc dependent", new Path("p").makeUNC(true), new Path("p").makeUNC(false));
		assertNotEquals("absolute dependent", new Path("p"), new Path("p").makeAbsolute());
		assertNotEquals("leading/ dependent", new Path("/p"), new Path("p"));
		assertNotEquals("leading\\ dependent", new Path("\\p"), new Path("p"));
	}

	@Test
	public void testUptoSegment() {

		// Case 1, absolute path with no trailing separator
		IPath anyPath = new Path("/first/second/third");

		assertEquals("1.0", IPath.ROOT, anyPath.uptoSegment(0));
		assertEquals("1.1", new Path("/first"), anyPath.uptoSegment(1));
		assertEquals("1.2", new Path("/first/second"), anyPath.uptoSegment(2));
		assertEquals("1.3", new Path("/first/second/third"), anyPath.uptoSegment(3));
		assertEquals("1.4", new Path("/first/second/third"), anyPath.uptoSegment(4));

		// Case 2, absolute path with trailing separator
		anyPath = new Path("/first/second/third/");

		assertEquals("2.0", IPath.ROOT, anyPath.uptoSegment(0));
		assertEquals("2.1", new Path("/first/"), anyPath.uptoSegment(1));
		assertEquals("2.2", new Path("/first/second/"), anyPath.uptoSegment(2));
		assertEquals("2.3", new Path("/first/second/third/"), anyPath.uptoSegment(3));
		assertEquals("2.4", new Path("/first/second/third/"), anyPath.uptoSegment(4));

		// Case 3, relative path with no trailing separator
		anyPath = new Path("first/second/third");

		assertEquals("3.0", IPath.EMPTY, anyPath.uptoSegment(0));
		assertEquals("3.1", new Path("first"), anyPath.uptoSegment(1));
		assertEquals("3.2", new Path("first/second"), anyPath.uptoSegment(2));
		assertEquals("3.3", new Path("first/second/third"), anyPath.uptoSegment(3));
		assertEquals("3.4", new Path("first/second/third"), anyPath.uptoSegment(4));

		// Case 4, relative path with trailing separator
		anyPath = new Path("first/second/third/");

		assertEquals("4.0", IPath.EMPTY, anyPath.uptoSegment(0));
		assertEquals("4.1", new Path("first/"), anyPath.uptoSegment(1));
		assertEquals("4.2", new Path("first/second/"), anyPath.uptoSegment(2));
		assertEquals("4.3", new Path("first/second/third/"), anyPath.uptoSegment(3));
		assertEquals("4.4", new Path("first/second/third/"), anyPath.uptoSegment(4));

		// bug 58835 - upToSegment(0) needs to preserve device
		anyPath = IPath.forWindows("c:/first/second/third");
		assertEquals("5.0.win", IPath.forWindows("c:/"), anyPath.uptoSegment(0));
		anyPath = IPath.forWindows("c:/first/second/third/");
		assertEquals("5.1.win", IPath.forWindows("c:/"), anyPath.uptoSegment(0));
		anyPath = IPath.forWindows("c:first/second/third/");
		assertEquals("5.2.win", IPath.forWindows("c:"), anyPath.uptoSegment(0));
		anyPath = new Path("//one/two/three");
		assertEquals("5.3", new Path("//"), anyPath.uptoSegment(0));
		anyPath = new Path("//one/two/three/");
		assertEquals("5.4", new Path("//"), anyPath.uptoSegment(0));
	}

	@Test
	public void testToPath() {

		// Case 1, absolute path with no trailing separator
		IPath anyPath = new Path("/first/second/third");

		assertNotNull(anyPath.toPath());
		assertEquals(java.nio.file.Path.of("/first/second/third"), anyPath.toPath());

		// Case 2, absolute path with trailing separator
		anyPath = new Path("/first/second/third/");

		assertNotNull(anyPath.toPath());
		assertEquals(java.nio.file.Path.of("/first/second/third/"), anyPath.toPath());

		// Case 3, relative path with no trailing separator
		anyPath = new Path("first/second/third");

		assertNotNull(anyPath.toPath());
		assertEquals(java.nio.file.Path.of("first/second/third"), anyPath.toPath());

		// Case 4, relative path with trailing separator
		anyPath = new Path("first/second/third/");

		assertNotNull(anyPath.toPath());
		assertEquals(java.nio.file.Path.of("first/second/third/"), anyPath.toPath());

	}
}
