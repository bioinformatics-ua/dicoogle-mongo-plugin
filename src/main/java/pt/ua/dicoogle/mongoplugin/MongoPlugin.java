/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Hashtable;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dcm4che2.data.ElementDictionary;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.settings.Settings;
import pt.ua.dicoogle.sdk.Utils.DictionaryAccess;

/**
 *
 * @author Louis
 */
public class MongoPlugin implements QueryInterface, StorageInterface {

    private String host, dbName = "DICOMtest";
    private int port;
    protected static MongoClient mongoClient = null;
    private DB db;
    private boolean isEnable = false;
    private Settings settings;
    private URI location;

    public MongoPlugin() {
    }

    public MongoPlugin(Settings settings) {
        this.settings = settings;
        parseSettings();
    }

    @Override
    public Iterable<SearchResult> query(String query, Object... parameters) {
        Iterable<SearchResult> result;
        if (!isEnable || mongoClient == null) {
            return null;
        }
        MongoQuery mongoQuery = new MongoQuery(query);
        List<GridFSDBFile> resultDBobjs = mongoQuery.processQuery(db);
        result = MongoUtil.getListFromResult(resultDBobjs, location, (float) 0.0);
        return result;
    }

    @Override
    public String getName() {
        return "mongodb";
    }

    @Override
    public boolean enable() {
        try {
            if (mongoClient == null) {
                mongoClient = new MongoClient(host, port);
            }
        } catch (UnknownHostException e) {
            return false;
        } catch (MongoException e) {
            return false;
        }
        db = mongoClient.getDB(dbName);
        try {
            location = new URI(this.getName() + "://" + host + ":" + port + "/" + dbName + "/");
        } catch (URISyntaxException e) {
            return false;
        }
        if (mongoClient != null) {
            isEnable = true;
        }
        /*try {
            DicomInputStream inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\1.dcm"));
            this.remove(this.store(inputStream));
            inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\2.dcm"));
            this.remove(this.store(inputStream));
            inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\3.dcm"));
            this.remove(this.store(inputStream));
            inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\4.dcm"));
            this.remove(this.store(inputStream));
            inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\5.dcm"));
            this.remove(this.store(inputStream));
            inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\6.dcm"));
            this.remove(this.store(inputStream));
            inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\7.dcm"));
            this.remove(this.store(inputStream));
            inputStream = new DicomInputStream(new File("D:\\Louis\\Desktop\\Dicoogle\\DICOM_Images\\8.dcm"));
            this.remove(this.store(inputStream));
        } catch (IOException e) {
        }*/
        return isEnable;
    }

    @Override
    public boolean disable() {
        mongoClient.close();
        isEnable = false;
        return !isEnable;
    }

    @Override
    public boolean isEnabled() {
        return isEnable;
    }

    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
        parseSettings();
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public String getScheme() {
        return this.getName() + "://host:port/dataBaseName/UUIDfileName";
    }

    @Override
    public boolean handles(URI location) {
        MongoURI uri = new MongoURI(location);
        return uri.verify();
    }

    @Override
    public Iterable<StorageInputStream> at(URI location) {
        MongoURI uri = new MongoURI(location);
        if (!isEnable || !uri.verify() || mongoClient == null) {
            return null;
        }
        ArrayList<StorageInputStream> list = new ArrayList<StorageInputStream>();
        list.add(new MongoStorageInputStream(location));
        return list;
    }

    @Override
    public URI store(DicomObject dicomObject) {
        if (!isEnable || mongoClient == null) {
            return null;
        }
        GridFS saveFs = new GridFS(this.db);
        String fileName = UUID.randomUUID().toString();
        DictionaryAccess instance = DictionaryAccess.getInstance();
        Hashtable<String, Integer> hTable = instance.getTagList();
        Iterator<String> it = hTable.keySet().iterator();
        Map<String, Object> docMap = new HashMap<String, Object>();
        while (it.hasNext()) {
            String key = it.next();
            DicomElement dicomElt = dicomObject.get(hTable.get(key));
            Object obj = null;
            if (dicomElt != null) {
                if (!key.equals("PixelData")) {
                    String str = null;
                    if (dicomElt.hasDicomObjects()) {
                        int nbItems = dicomElt.countItems();
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        for (int i = 0; i < nbItems; i++) {
                            DicomObject dicomObj = dicomElt.getDicomObject(i);
                            Iterator<DicomElement> itTemp = dicomObj.iterator();
                            while (itTemp.hasNext()) {
                                DicomElement dicomEltTemp = itTemp.next();
                                map.put(instance.tagName(dicomEltTemp.tag()), dicomEltTemp.getValueAsString(dicomObject.getSpecificCharacterSet(), 0));
                            }
                        }
                        obj = map;
                    } else {
                        str = dicomElt.getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
                        if (str != null) {
                            try {
                                obj = Double.parseDouble(str);
                            } catch (NumberFormatException e) {
                                obj = str;
                            }
                        }
                    }
                    docMap.put(key, obj);
                }
                else
                    docMap.put(key, dicomElt.getBytes());
            }
        }
        GridFSInputFile ins = saveFs.createFile((this.getLocation() + fileName).getBytes());
        ins.setFilename(fileName);
        ins.setMetaData(new BasicDBObject(docMap));
        ins.save();
        URI uri;
        try {
            uri = new URI(this.getLocation() + fileName);
        } catch (URISyntaxException e) {
            return null;
        }
        return uri;
    }

    @Override
    public URI store(DicomInputStream inputStream) throws IOException {
        if (!isEnable) {
            return null;
        }
        return this.store(inputStream.readDicomObject());
    }

    @Override
    public void remove(URI location) {
        MongoURI uri = new MongoURI(location);
        if (!isEnable || !uri.verify() || mongoClient == null) {
            return;
        }
        uri.getInformation();
        DB dbTemp = mongoClient.getDB(uri.getDBName());
        GridFS removeFS = new GridFS(dbTemp);
        removeFS.remove(uri.getFileName());
    }

    private void parseSettings() {
        String str = this.settings.getXml();
        int i;
        char currentChar = 0;
        Scanner scanner = new Scanner(str);
        while (scanner.hasNextLine()) {
            i = 0;
            str = scanner.nextLine();
            if (str.contains("<DefaultServerHost>") && str.contains("</DefaultServerHost>")) {
                while (currentChar != '>') {
                    currentChar = str.charAt(i);
                    i++;
                }
                host = "";
                while (currentChar != '<' && str.contains("</DefaultServerHost>")) {
                    currentChar = str.charAt(i);
                    i++;
                    if (currentChar != '<') {
                        host += currentChar;
                    }
                }
            }
            if (str.contains("<DefaultServerPort>") && str.contains("</DefaultServerPort>")) {
                while (currentChar != '>') {
                    currentChar = str.charAt(i);
                    i++;
                }
                String portStr = "";
                while (currentChar != '<') {
                    currentChar = str.charAt(i);
                    i++;
                    if (currentChar != '<') {
                        portStr += currentChar;
                    }
                }
                port = Integer.parseInt(portStr);
            }
            if (str.contains("<DefaultDataBase>") && str.contains("</DefaultDataBase>")) {
                while (currentChar != '>') {
                    currentChar = str.charAt(i);
                    i++;
                }
                dbName = "";
                while (currentChar != '<') {
                    currentChar = str.charAt(i);
                    i++;
                    if (currentChar != '<') {
                        dbName += currentChar;
                    }
                }
            }
        }
    }

    public void setLocation(URI location) {
        this.location = location;
    }

    public URI getLocation() {
        return this.location;
    }

    public void setDbName(String name) {
        this.dbName = name;
    }

    public void setHost(String host) {
        this.setHost(host);
    }

    public String getHost() {
        return this.host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }
}
