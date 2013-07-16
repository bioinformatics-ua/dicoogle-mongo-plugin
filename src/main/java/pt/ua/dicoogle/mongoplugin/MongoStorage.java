/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import static pt.ua.dicoogle.mongoplugin.MongoPlugin.mongoClient;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

/**
 *
 * @author Louis
 */
class MongoStorage implements StorageInterface {

    private ConfigurationHolder settings;
    private URI location;
    private String dbName;
    private String host;
    private int port;
    private boolean isEnable;
    private static String hostKey = "DefaultServerHost";
    private static String portKey = "DefaultServerPort";
    private static String dbNameKey = "DefaultDataBase";

    public MongoStorage() {
    }

    public MongoStorage(ConfigurationHolder settings) {
        this.settings = settings;
        host = settings.getCnf().getString(hostKey);
        port = settings.getCnf().getInt(portKey);
        dbName = settings.getCnf().getString(dbNameKey);
    }

    @Override
    public String getScheme() {
        return this.getName() + "://host:port/dataBaseName/UUIDfileName";
    }

    @Override
    public boolean handles(URI pUri) {
        MongoURI uri = new MongoURI(pUri);
        return uri.verify();
    }

    @Override
    public Iterable<StorageInputStream> at(URI pUri) {
        MongoURI uri = new MongoURI(pUri);
        if (!isEnable || !uri.verify() || mongoClient == null) {
            return null;
        }
        ArrayList<StorageInputStream> list = new ArrayList<StorageInputStream>();
        MongoStorageInputStream MongoStorageIn = new MongoStorageInputStream(pUri);
        if (MongoStorageIn.getInputStream() != null) {
            list.add(MongoStorageIn);
        }
        return list;
    }

    @Override
    public URI store(DicomObject dicomObject) {
        if (!isEnable || mongoClient == null || dicomObject == null) {
            return null;
        }
        String fileName = dicomObject.get(Tag.SOPInstanceUID).getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
        URI uri = null;
        try {
            uri = new URI(this.location + fileName);
        } catch (URISyntaxException e) {
        }
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DicomOutputStream dos = new DicomOutputStream(bos);
            dos.writeDicomFile(dicomObject);
            DB db = mongoClient.getDB(this.dbName);
            GridFS saveFs = new GridFS(db);
            GridFSInputFile ins = saveFs.createFile(file);
            ins.setFilename(fileName);
            ins.save();
        } catch (IOException ex) {
            Logger.getLogger(MongoStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uri;
    }

    @Override
    public URI store(DicomInputStream stream) throws IOException {
        if (!isEnable) {
            return null;
        }
        return this.store(stream.readDicomObject());
    }

    @Override
    public void remove(URI pUri) {
        MongoURI uri = new MongoURI(pUri);
        if (!isEnable || !uri.verify() || mongoClient == null) {
            return;
        }
        uri.getInformation();
        DB db = mongoClient.getDB(uri.getDBName());
        GridFS removeFS = new GridFS(db);
        removeFS.remove(uri.getFileName());
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
            if (mongoClient == null) {
                mongoClient = new MongoClient(host, port);
            }
        } catch (UnknownHostException e) {
            return false;
        } catch (MongoException e) {
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
        mongoClient.close();
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
        host = settings.getCnf().getString(hostKey);
        port = settings.getCnf().getInt(portKey);
        dbName = settings.getCnf().getString(dbNameKey);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return this.settings;
    }
}
