/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import static pt.ua.dicoogle.mongoplugin.MongoPluginSet.mongoClient;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

/**
 *
 * @author Louis
 */
class MongoQuery implements QueryInterface {

    private DBCollection collection;
    private boolean isEnable;
    private URI location;
    private ConfigurationHolder settings;
    private String dbName;
    private String collectionName;
    private String host;
    private int port;
    private static String hostKey = "DefaultServerHost";
    private static String portKey = "DefaultServerPort";
    private static String dbNameKey = "DefaultDataBase";
    private static String collectionNameKey = "DefaultCollection";

    public MongoQuery() {
        System.out.println("INIT->MongoQuery");
    }

    public MongoQuery(ConfigurationHolder settings) {
        this.settings = settings;
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        dbName = settings.getConfiguration().getString(dbNameKey);
        collectionName = settings.getConfiguration().getString(collectionNameKey);
        collection = mongoClient.getDB(dbName).getCollection(collectionName);
    }

    @Override
    public Iterable<SearchResult> query(String query, Object... os) {
        Iterable<SearchResult> result;
        if (!isEnable || mongoClient == null) {
            return null;
        }
        MongoQueryUtil mongoQuery = new MongoQueryUtil(query);
        List<DBObject> resultDBobjs = mongoQuery.processQuery(collection);
        result = MongoUtil.getListFromResult(resultDBobjs, location, (float) 0.0);
        return result;
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
        collectionName = settings.getConfiguration().getString(collectionNameKey);
        collection = mongoClient.getDB(dbName).getCollection(collectionName);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return settings;
    }
}
