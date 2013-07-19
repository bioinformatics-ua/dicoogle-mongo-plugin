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
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private DB db;
    private boolean isEnable;
    private URI location;
    private ConfigurationHolder settings = null;
    private String dbName;
    private String host;
    private int port;
    private static String hostKey = "DefaultServerHost";
    private static String portKey = "DefaultServerPort";
    private static String dbNameKey = "DefaultDataBase";
    private static String fileName = "log.txt";
    
    public MongoIndexer() {
        System.out.println("INIT->MongoIndexer");
    }

    public MongoIndexer(ConfigurationHolder settings) {
        this.settings = settings;
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        dbName = settings.getConfiguration().getString(dbNameKey);
        db = mongoClient.getDB(dbName);
    }

    @Override
    public Task<Report> index(StorageInputStream stream) {
        ArrayList<StorageInputStream> itrbl = new ArrayList<StorageInputStream>();
        itrbl.add(stream);
        MongoCallable c = new MongoCallable(itrbl, this.location, db, fileName);
        Task<Report> task = new Task(c);
        return task;
    }

    @Override
    public Task<Report> index(Iterable<StorageInputStream> itrbl) {
        MongoCallable c = new MongoCallable(itrbl, this.location, db, fileName);
        Task<Report> task = new Task(c);
        return task;
    }

    @Override
    public boolean unindex(URI pUri) {
        String str = pUri.toString();
        URI uriTemp;
        try {
            uriTemp = new URI(str.replaceAll(".B", ".MD"));
        } catch (URISyntaxException ex) {
            Logger.getLogger(MongoIndexer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        MongoURI uri = new MongoURI(uriTemp);
        //MongoURI uri = new MongoURI(pUri);
        if (!isEnable || !uri.verify() || mongoClient == null) {
            return false;
        }
        uri.getInformation();
        GridFS saveFs = new GridFS(db);

        /*GridFSDBFile file = saveFs.findOne(uri.getFileName());
         if (file == null) {
         return false;
         } else {
         file.setMetaData(new BasicDBObject());
         file.save();
         }
         return true;*/

        saveFs.remove(uri.getFileName());
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
        db = mongoClient.getDB(dbName);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return this.settings;
    }
}
