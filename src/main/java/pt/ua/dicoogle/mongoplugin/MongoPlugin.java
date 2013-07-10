/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Hashtable;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.dcm4che2.data.DicomObject;
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

    private String host, dbName = "DICOMtest", collectionName = "DICOMdata";
    private int port;
    protected static MongoClient mongoClient = null;
    private DB db;
    private DBCollection collection = null;
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
        if (collection == null) {
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
        collection = db.getCollection(collectionName);
        try {
            location = new URI(this.getName() + "://" + host + ":" + port + "/" + dbName);
        } catch (URISyntaxException e) {
            return false;
        }
        if (mongoClient != null) {
            isEnable = true;
        }
        //this.remove(this.store("test"));
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

    // Just to test
    public URI store(String str) {
        if (!isEnable || mongoClient == null) {
            return null;
        }
        GridFS saveFs = new GridFS(this.db);
        String fileName = UUID.randomUUID().toString();
        GridFSInputFile ins = saveFs.createFile((this.getLocation() + "/" + fileName).getBytes());
        ins.setFilename(fileName);
        ins.setMetaData(new BasicDBObject("str", str));
        ins.save();
        URI uri = null;
        try {
            uri = new URI(this.getLocation() + "/" + fileName);
        } catch (URISyntaxException e) {
        }
        return uri;
    }

    @Override
    public URI store(DicomObject dicomObject) {
        if (!isEnable || mongoClient == null) {
            return null;
        }
        GridFS saveFs = new GridFS(this.db);
        String fileName = UUID.randomUUID().toString();
        GridFSInputFile ins = saveFs.createFile();
        ins.setFilename(fileName);
        DictionaryAccess dictionary = DictionaryAccess.getInstance();
        Hashtable<String, Integer> hTable = dictionary.getTagList();
        Iterator<String> it = hTable.keySet().iterator();
        while(it.hasNext()){
            String key = it.next();
            ins.setMetaData(new BasicDBObject(key, dicomObject.getBytes(hTable.get(key))));
        }
        ins.save();
        URI uri = null;
        try {
            uri = new URI(this.getLocation() + "/" + fileName);
        } catch (URISyntaxException e) {
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
            if (str.contains("<DefaultCollection>") && str.contains("</DefaultCollection>")) {
                while (currentChar != '>') {
                    currentChar = str.charAt(i);
                    i++;
                }
                collectionName = "";
                while (currentChar != '<') {
                    currentChar = str.charAt(i);
                    i++;
                    if (currentChar != '<') {
                        collectionName += currentChar;
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

    public void setCollectionName(String name) {
        this.collectionName = name;
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
