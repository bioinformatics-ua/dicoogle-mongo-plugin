/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.settings.Settings;

/**
 *
 * @author Louis
 */
public class MongoPluginTest {

    private static MongoPlugin instance;

    public MongoPluginTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        Settings settings = new Settings(new File(".\\settings\\mongoplugin.xml"));
        instance = new MongoPlugin(settings);
        instance.enable();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of query method, of class MongoPlugin.
     */
    @Test
    public void testQuery() {
        System.out.println("query");
        String query = "SOPInstanceUID:[1.3.6.1.4.1.9328.50.4.207 TO 1.3.6.1.4.1.9328.50.4.230]";
        Object[] parameters = null;
        Object result = instance.query(query, parameters);
        Assert.assertThat(result, IsNull.notNullValue());
        Assert.assertThat(result, IsInstanceOf.instanceOf(Iterable.class));
        Iterator<Object> it = ((Iterable) result).iterator();
        int i = 0;
        while (it.hasNext()) {
            Object obj = it.next();
            Assert.assertThat(obj, IsInstanceOf.instanceOf(SearchResult.class));
            i++;
        }
        Assert.assertThat(i, IsNot.not(0));
    }

    /**
     * Test of enable method, of class MongoPlugin.
     */
    @Test
    public void testEnable() {
        System.out.println("enable");
        boolean result = instance.enable();
        assertEquals(result, true);
    }

    /**
     * Test of disable method, of class MongoPlugin.
     */
    @Test
    public void testDisable() {
        System.out.println("disable");
        boolean result = instance.disable();
        assertEquals(result, true);
    }
    
    public void testAt() throws URISyntaxException, IOException {
        System.out.println("at");
        URI location = new URI(instance.getLocation() + "07aea52b-5dfb-4c78-8a65-79ec8e51b198");
        Object result = instance.at(location);
        Assert.assertThat(result, IsNull.notNullValue());
        Assert.assertThat(result, IsInstanceOf.instanceOf(Iterable.class));
        Iterator<Object> it = ((Iterable) result).iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            Assert.assertThat(obj, IsInstanceOf.instanceOf(StorageInputStream.class));
            Assert.assertThat(obj, IsInstanceOf.instanceOf(MongoStorageInputStream.class));
            int res = ((StorageInputStream) obj).getInputStream().read();
            Assert.assertThat(res, IsNot.not(-1));
        }
    }

    private static ArrayList<String> retrieveFileList(File file, int level) {
        ArrayList<String> fileList = new ArrayList<String>();
        ArrayList<String> fileList2;
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                fileList2 = retrieveFileList(f, level + 1);
                for (int i = 0; i < fileList2.size(); i++) {
                    fileList.add(fileList2.get(i));
                }
            } else {
                if (f.getAbsolutePath().contains(".dcm")) {
                    fileList.add(f.getAbsolutePath());
                }
            }
        }
        return fileList;
    }

    /**
     * Test of store method, of class MongoPlugin.
     */
    @Test
    public void testStore_DicomObject() throws IOException {
        ArrayList<String> fileList = retrieveFileList(new File("D:\\DICOM_data\\DATA\\"), 0);
        //int borne = fileList.size();
        int borne = 100;
        for (int i = 0; i < borne; i++) {
            URI result = null;
            try {
                DicomInputStream inputStream = new DicomInputStream(new File(fileList.get(i)));
                DicomObject dcmObj = inputStream.readDicomObject();
                System.out.println("Store  from " + fileList.get(i) + " - " + (i + 1) + "th file");
                result = instance.store(dcmObj);
            } catch (IOException e) {
            }
            Assert.assertThat(result, IsNull.notNullValue());
            boolean b = instance.handles((URI) result);
            assertEquals(b, true);
            /*System.out.println("Remove to   " + result);
            instance.remove(result);*/
        }
    }

    /**
     * Test of store method, of class MongoPlugin.
     */
    @Test
    public void testStore_DicomInputStream() throws Exception {
        ArrayList<String> fileList = retrieveFileList(new File("D:\\DICOM_data\\DATA\\"), 0);
        //int borne = fileList.size();
        int borne = 100;
        for (int i = 0; i < borne; i++) {
            URI result = null;
            try {
                DicomInputStream inputStream = new DicomInputStream(new File(fileList.get(i)));
                //System.out.println("Store  from " + fileList.get(i) + " - " + (i + 1) + "th file");
                result = instance.store(inputStream);
            } catch (IOException e) {
            }
            Assert.assertThat(result, IsNull.notNullValue());
            boolean b = instance.handles((URI) result);
            assertEquals(b, true);
            //System.out.println("Remove to   " + tempURI);
            instance.remove(result);
        }
    }
}