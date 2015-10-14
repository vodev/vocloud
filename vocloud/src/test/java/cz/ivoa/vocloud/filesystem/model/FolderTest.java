package cz.ivoa.vocloud.filesystem.model;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing methods for the Folder class which serves as model in
 * FilesystemManipulator EJB bean
 *
 * @author radio.koza
 */
public class FolderTest {

    private static final Logger LOG = Logger.getLogger(FolderTest.class.getName());

    /**
     * Testing that null value passed as name parameter in constructor throws
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitializationNullName() {
        LOG.info("testing initialization with null name");
        String folderName = null;
        String relativeDir = "folder/tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that null value passed as relative directory parameter in
     * constructor throws exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitializationNullRelativeDir() {
        LOG.info("testing initialization with null relative direcotry");
        String folderName = "foo";
        String relativeDir = null;
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that empty String passed as name parameter in constructor throws
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitializationEmptyName() {
        LOG.info("testing initialization with empty directory name");
        String folderName = "";
        String relativeDir = "folder/tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that if name parameter contains only whitespaces it throws
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitializationWhitespacesInName() {
        LOG.info("testing initialization with only whitespaces in directory name");
        String folderName = "  \n ";
        String relativeDir = "folder/tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that slash is forbidden in folder name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSlashInName() {
        LOG.info("testing initialization with slash in the name of folder");
        String folderName = "foo/bar";
        String relativeDir = "folder/tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that backslash is forbidden in folder name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBackslashInName() {
        LOG.info("testing initialization with backslash in the name of folder");
        String folderName = "foo\\bar";
        String relativeDir = "folder/tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that backslashes are forbidden in relative directory constructor
     * parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBackslashInRelativeDir() {
        LOG.info("testing initialization with backslash in the relative dir");
        String folderName = "bar";
        String relativeDir = "folder\\tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that multiple slashes passed in sequence in relative directory
     * are forbidden.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMultipleSlashesInRelativeDir() {
        LOG.info("testing initialization with multiple slashes in the relative dir");
        String folderName = "bar";
        String relativeDir = "folder//tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that folder relative dir is really relative dir - absolute
     * directories starting with slashes must be forbidden.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSlashStartRelativeDir() {
        LOG.info("testing initialization with slash as the first character in relative dir");
        String folderName = "bar";
        String relativeDir = "/folder/tmp";
        Folder folder = new Folder(folderName, relativeDir);//should throw exception
        fail("Expected exception was not thrown");
    }

    /**
     * Testing that trimming whitespaces around is applied on bot folder name
     * and folder relative directory.
     */
    @Test
    public void testTrimming() {
        LOG.info("testing that trimming is applicated on both folder name and relative dir");
        String folderName = "   bar  ";
        String relativeDir = "  folder/tmp/baf ahoj  ";
        Folder folder = new Folder(folderName, relativeDir);
        String expectedName = "bar";
        String expectedDir = "folder/tmp/baf ahoj";
        assertEquals(expectedName, folder.getName());
        assertEquals(expectedDir, folder.getPrefix());

    }

    private final static String invalidFolderNameChars = "\\/?\"<>|";

    /**
     * Testing that folder name does not support names with characters that are
     * forbidden on most platforms.
     */
    @Test
    public void testInvalidFolderName() {
        LOG.info("testing folder name with invalid characters");
        String pre = "foo";
        String post = "bar";
        String relativeDir = "folder/tmp";
        String folderName;
        boolean failed = false;
        for (char c : invalidFolderNameChars.toCharArray()) {
            folderName = pre + c + post;
            try {
                Folder folder = new Folder(folderName, relativeDir);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            LOG.log(Level.WARNING, "  character {0} seems to be valid in folder name", c);
            failed = true;
        }
        if (failed){
            fail("Expected exception was not thrown");
        }
    }

    /**
     * Testing construction of complete path - this is a concatenation of relative directory
     * and folder name. This concatenation must be done properly and it must not contain
     * double slashes in the point of concatenation.
     */
    @Test
    public void testGetCompletePath() {
        LOG.info("testing get complete path method");
        String relativeDir1 = "tmp/folder1/";
        String relativeDir2 = "tmp/folder2";
        String folderName = "ahoj";
        String expected1 = "tmp/folder1/ahoj";
        String expected2 = "tmp/folder2/ahoj";
        Folder folder1 = new Folder(folderName, relativeDir1);
        Folder folder2 = new Folder(folderName, relativeDir2);
        assertEquals(expected1, folder1.getCompletePath());
        assertEquals(expected2, folder2.getCompletePath());
    }

}
