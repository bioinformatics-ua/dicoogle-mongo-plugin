/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import static pt.ua.dicoogle.mongoplugin.MongoPluginSet.mongoClient;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.Utils.DictionaryAccess;
import pt.ua.dicoogle.sdk.datastructs.Report;

/**
 *
 * @author Louis
 */
public class MongoCallable implements Callable<Report> {

    private Iterable<StorageInputStream> itrblStorageInputStream = null;
    private String dbName;
    private URI location;
    
    public MongoCallable(Iterable<StorageInputStream> itrbl, String pDbName, URI pLocation) {
        super();
        this.itrblStorageInputStream = itrbl;
        this.dbName = pDbName;
        this.location = pLocation;
    }

    public void setItrblStorageInputStrem(Iterable<StorageInputStream> itrbl) {
        this.itrblStorageInputStream = itrbl;
    }

    public Report call() throws Exception {
        if (itrblStorageInputStream == null) {
            return null;
        }
        for (StorageInputStream stream : itrblStorageInputStream) {
            InputStream is = stream.getInputStream();
            InputStream bufferedIn = new BufferedInputStream(is);
            try {
                DicomInputStream dis = new DicomInputStream(bufferedIn);
                DicomObject dicomObj = dis.readDicomObject();
                this.store(dicomObj);
            } catch (IOException ex) {
                Logger.getLogger(MongoIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Report();
    }
    
    private URI store(DicomObject dicomObject) {
        if (mongoClient == null) {
            return null;
        }
        String fileName = dicomObject.get(Tag.SOPInstanceUID).getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
        URI uri;
        try {
            uri = new URI(location + fileName);
        } catch (URISyntaxException e) {
            return null;
        }
        DictionaryAccess instance = DictionaryAccess.getInstance();
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
