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
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.datastructs.Report;

/**
 *
 * @author Louis
 */
public class MongoCallable implements Callable<Report> {

    private DB db;
    private Iterable<StorageInputStream> itrblStorageInputStream = null;
    private URI location;

    public MongoCallable(Iterable<StorageInputStream> itrbl, URI pLocation, DB pDb) {
        super();
        this.itrblStorageInputStream = itrbl;
        this.location = pLocation;
        db = pDb;
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
                this.store(retrieveHeader(dicomObj), dicomObj.get(Tag.SOPInstanceUID).getValueAsString(dicomObj.getSpecificCharacterSet(), 0));
            } catch (IOException ex) {
                Logger.getLogger(MongoIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Report();
    }

    private URI store(HashMap<String, Object> map, String fileName) {
        URI uri;
        try {
            uri = new URI(location + fileName);
        } catch (URISyntaxException e) {
            return null;
        }
        GridFSDBFile file = new GridFS(db).findOne(fileName);
        file.setMetaData(new BasicDBObject(map));
        file.save();
        return uri;
    }

    private HashMap<String, Object> retrieveHeader(DicomObject dicomObject) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator iter = dicomObject.datasetIterator();
        while (iter.hasNext()) {
            DicomElement element = (DicomElement) iter.next();
            int tag = element.tag();
            try {
                String tagName = dicomObject.nameOf(tag);
                if (dicomObject.vrOf(tag).toString().equals("SQ")) {
                    if (element.hasItems()) {
                        map.putAll(retrieveHeader(element.getDicomObject()));
                        continue;
                    }
                }
                String tagValue = dicomObject.getString(tag);
                if (tagValue == null) {
                    continue;
                }
                map.put(tagName, tagValue);
            } catch (Exception e) {
            }
        }
        return map;
    }
}
