/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
    private String fileName;

    public MongoCallable(Iterable<StorageInputStream> itrbl, URI pLocation, DB pDb, String fileName) {
        super();
        this.itrblStorageInputStream = itrbl;
        this.location = pLocation;
        this.db = pDb;
        this.fileName = fileName;
    }

    public void setItrblStorageInputStrem(Iterable<StorageInputStream> itrbl) {
        this.itrblStorageInputStream = itrbl;
    }

    public Report call() throws Exception {
        if (itrblStorageInputStream == null) {
            return null;
        }
        for (StorageInputStream stream : itrblStorageInputStream) {
            BufferedWriter bufWriter;
            FileWriter fileWriter;
            String SOPInstanceUID;
            long start, end;
            start = System.currentTimeMillis();
            InputStream is = stream.getInputStream();
            InputStream bufferedIn = new BufferedInputStream(is);
            try {
                DicomInputStream dis = new DicomInputStream(bufferedIn);
                DicomObject dicomObj = dis.readDicomObject();
                SOPInstanceUID = dicomObj.get(Tag.SOPInstanceUID).getValueAsString(dicomObj.getSpecificCharacterSet(), 0);
                this.store(retrieveHeader(dicomObj), SOPInstanceUID);
                
                end = System.currentTimeMillis();
                fileWriter = new FileWriter(fileName, true);
                bufWriter = new BufferedWriter(fileWriter);
                bufWriter.newLine();
                bufWriter.write(String.format("%s %d %d", SOPInstanceUID, start, end));
                bufWriter.close();
                fileWriter.close();
                System.out.println("Indexed " + stream.getURI());
            } catch (IOException ex) {
                Logger.getLogger(MongoIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Report();
    }

    private URI store(HashMap<String, Object> map, String fileName) {
        URI uri;
        try {
            //uri = new URI(location + fileName);
            uri = new URI(location + fileName + ".MD");
        } catch (URISyntaxException e) {
            return null;
        }
        //GridFSDBFile file = new GridFS(db).findOne(fileName);
        byte[] data = {0};
        GridFSInputFile file = new GridFS(db).createFile(data);
        file.setFilename(fileName + ".MD");
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
                DicomElement dicomElt = dicomObject.get(tag);
                String tagValue = dicomElt.getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
                if (tagValue == null) {
                    continue;
                }
                Object obj;
                try {
                    obj = Double.parseDouble(tagValue);
                } catch (NumberFormatException e) {
                    obj = tagValue;
                }
                map.put(tagName, obj);
            } catch (Exception e) {
            }
        }
        return map;
    }
}
