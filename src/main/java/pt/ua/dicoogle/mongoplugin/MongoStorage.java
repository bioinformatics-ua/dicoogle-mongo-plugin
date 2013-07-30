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
import java.io.ByteArrayOutputStream;
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
import static pt.ua.dicoogle.mongoplugin.MongoPluginSet.mongoClient;
import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

/**
 *
 * @author Louis
 */
class MongoStorage implements StorageInterface {

    private DB db;
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
        System.out.println("INIT->MongoStorage");
    }

    public MongoStorage(ConfigurationHolder settings) {
        this.settings = settings;
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        dbName = settings.getConfiguration().getString(dbNameKey);
        db = mongoClient.getDB(this.dbName);
    }

    @Override
    public String getScheme() {
        return this.getName() + "://" + host + ":" + port + "/" + dbName + "/";
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
        URI uri;
        try {
            uri = new URI(this.location + fileName);
        } catch (URISyntaxException e) {
            System.out.println("Error : URISyntaxException");
            return null;
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DicomOutputStream dos = new DicomOutputStream(os);
            dos.writeDicomFile(dicomObject);
            GridFS saveFs = new GridFS(db);
            GridFSInputFile ins = saveFs.createFile(os.toByteArray());
            ins.setFilename(fileName);
            ins.save(ins.getChunkSize());
        } catch (IOException ex) {
            Logger.getLogger(MongoStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uri;
    }

    @Override
    public URI store(DicomInputStream stream) throws IOException {
        if (!isEnable || mongoClient == null || stream == null) {
            return null;
        }
        return this.store(stream.readDicomObject());
        /*DicomObject dicomObject = stream.readDicomObject();

        String fileName = dicomObject.get(Tag.SOPInstanceUID).getValueAsString(dicomObject.getSpecificCharacterSet(), 0);
        URI uri;
        try {
            uri = new URI(this.location + fileName);
        } catch (URISyntaxException e) {
            System.out.println("Error : URISyntaxException");
            return null;
        }
        GridFS saveFs = new GridFS(db);
        GridFSInputFile ins = saveFs.createFile(stream);
        ins.setFilename(fileName);
        ins.save(ins.getChunkSize());
        return uri;*/
    }

    @Override
    public void remove(URI pUri) {
        MongoURI uri = new MongoURI(pUri);
        if (!isEnable || !uri.verify() || mongoClient == null) {
            return;
        }
        uri.getInformation();
        GridFS removeFS = new GridFS(db);
        removeFS.remove(uri.getFileName());
        System.out.println("Remove done");
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
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        dbName = settings.getConfiguration().getString(dbNameKey);
        db = mongoClient.getDB(this.dbName);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return this.settings;
    }
}
