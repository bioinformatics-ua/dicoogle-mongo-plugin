/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import static pt.ua.dicoogle.mongoplugin.MongoPluginSet.mongoClient;
import pt.ua.dicoogle.sdk.IndexerInterface;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.datastructs.Report;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;
import pt.ua.dicoogle.sdk.task.Task;

/**
 *
 * @author Louis
 */
class MongoIndexer implements IndexerInterface {

    private boolean isEnable;
    private URI location;
    private ConfigurationHolder settings = null;
    private String dbName;
    private String host;
    private int port;
    private static String hostKey = "DefaultServerHost";
    private static String portKey = "DefaultServerPort";
    private static String dbNameKey = "DefaultDataBase";

    public MongoIndexer() {
    }

    public MongoIndexer(ConfigurationHolder settings) {
        this.settings = settings;
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        dbName = settings.getConfiguration().getString(dbNameKey);
    }

    @Override
    public Task<Report> index(StorageInputStream stream) {
        ArrayList<StorageInputStream> itrbl = new ArrayList<StorageInputStream>();
        itrbl.add(stream);
        MongoCallable c = new MongoCallable(itrbl, this.dbName, this.location);
        Task<Report> task = new Task(c);
        return task;
    }

    @Override
    public Task<Report> index(Iterable<StorageInputStream> itrbl) {
        MongoCallable c = new MongoCallable(itrbl, this.dbName, this.location);
        Task<Report> task = new Task(c);
        return task;
    }

    @Override
    public boolean unindex(URI pUri) {
        MongoURI uri = new MongoURI(pUri);
        if (!isEnable || !uri.verify() || mongoClient == null) {
            return false;
        }
        uri.getInformation();
        DB db = mongoClient.getDB(dbName);
        GridFS saveFs = new GridFS(db);
        List<GridFSDBFile> listFile = saveFs.find(uri.getFileName());
        if (listFile.isEmpty()) {
            return false;
        } else {
            for (GridFSDBFile fsDbFile : listFile) {
                fsDbFile.setMetaData(new BasicDBObject());
                fsDbFile.save();
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "mongodb";
    }

    @Override
    public boolean enable() {
        if (mongoClient == null || this.settings == null) {
            return false;
        }
        try {
            location = new URI("mongodb" + "://" + host + ":" + port + "/" + dbName + "/");
        } catch (URISyntaxException e) {
            return false;
        }
        isEnable = true;
        return true;
    }

    @Override
    public boolean disable() {
        isEnable = false;
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnable;
    }

    @Override
    public void setSettings(ConfigurationHolder stngs) {
        this.settings = stngs;
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        dbName = settings.getConfiguration().getString(dbNameKey);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return this.settings;
    }

    private URI store(DicomObject dicomObject) {
        if (!isEnable || mongoClient == null) {
            return null;
        }
        String fileName = dicomObject.get(Tag.SOPInstanceUID).getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
        URI uri;
        try {
            uri = new URI(this.location + fileName);
        } catch (URISyntaxException e) {
            return null;
        }
        Dictionary instance = Dictionary.getInstance();
        Hashtable<String, Integer> hTable = instance.getTagList();
        Iterator<String> it = hTable.keySet().iterator();
        Map<String, Object> docMap = new HashMap<String, Object>();
        while (it.hasNext()) {
            String key = it.next();
            DicomElement dicomElt = dicomObject.get(hTable.get(key));
            Object obj = null;
            if (dicomElt != null) {
                if (!key.equals(instance.tagName(Tag.PixelData))) {
                    String str;
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
            }
        }
        DB db = mongoClient.getDB(dbName);
        GridFS saveFs = new GridFS(db);
        List<GridFSDBFile> listFile = saveFs.find(fileName);
        if (listFile.isEmpty()) { //Should not happend
            return null;
        } else {
            for (GridFSDBFile fsDbFile : listFile) {
                fsDbFile.setMetaData(new BasicDBObject(docMap));
                fsDbFile.save();
            }
        }
        return uri;
    }
    
    
}
