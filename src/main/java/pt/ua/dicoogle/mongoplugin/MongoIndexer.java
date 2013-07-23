/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.net.URI;
import java.util.ArrayList;
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

    private DBCollection collection;
    private boolean isEnable;
    private ConfigurationHolder settings = null;
    private String dbName;
    private String collectionName;
    private static String dbNameKey = "DefaultDataBase";
    private static String collectionNameKey = "DefaultCollection";
    private static String fileName = "log.txt";
    
    public MongoIndexer() {
        System.out.println("INIT->MongoIndexer");
    }

    public MongoIndexer(ConfigurationHolder settings) {
        this.settings = settings;
        dbName = settings.getConfiguration().getString(dbNameKey);
        collectionName = settings.getConfiguration().getString(collectionNameKey);
        collection = mongoClient.getDB(dbName).getCollection(collectionName);
    }

    @Override
    public Task<Report> index(StorageInputStream stream) {
        ArrayList<StorageInputStream> itrbl = new ArrayList<StorageInputStream>();
        itrbl.add(stream);
        MongoCallable c = new MongoCallable(itrbl, collection, fileName);
        Task<Report> task = new Task(c);
        return task;
    }

    @Override
    public Task<Report> index(Iterable<StorageInputStream> itrbl) {
        MongoCallable c = new MongoCallable(itrbl, collection, fileName);
        Task<Report> task = new Task(c);
        return task;
    }

    @Override
    public boolean unindex(URI pUri) {
        MongoURI uri = new MongoURI(pUri);
        if (!isEnable || mongoClient == null) {
            return false;
        }
        Dictionary dicoInstance = Dictionary.getInstance();
        DBObject query = new BasicDBObject();
        query.put(dicoInstance.tagName(Tag.SOPInstanceUID), uri.getFileName());
        collection.findAndRemove(query);
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
        dbName = settings.getConfiguration().getString(dbNameKey);
        collectionName = settings.getConfiguration().getString(collectionNameKey);
        collection = mongoClient.getDB(dbName).getCollection(collectionName);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return this.settings;
    }
}
