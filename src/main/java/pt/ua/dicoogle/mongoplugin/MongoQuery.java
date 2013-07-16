/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFSDBFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import static pt.ua.dicoogle.mongoplugin.MongoPlugin.mongoClient;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

/**
 *
 * @author Louis
 */
class MongoQuery implements QueryInterface {

    private boolean isEnable;
    private URI location;
    private ConfigurationHolder settings;
    private String dbName;
    private String host;
    private int port;
    private static String hostKey = "DefaultServerHost";
    private static String portKey = "DefaultServerPort";
    private static String dbNameKey = "DefaultDataBase";

    public MongoQuery() {
    }

    public MongoQuery(ConfigurationHolder settings) {
        this.settings = settings;
        host = settings.getCnf().getString(hostKey);
        port = settings.getCnf().getInt(portKey);
        dbName = settings.getCnf().getString(dbNameKey);
    }

    @Override
    public Iterable<SearchResult> query(String query, Object... os) {
        Iterable<SearchResult> result;
        if (!isEnable || mongoClient == null) {
            return null;
        }
        MongoQueryUtil mongoQuery = new MongoQueryUtil(query);
        DB db = mongoClient.getDB(dbName);
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
        host = stngs.getCnf().getString(hostKey);
        port = stngs.getCnf().getInt(portKey);
        dbName = stngs.getCnf().getString(dbNameKey);
    }

    @Override
    public ConfigurationHolder getSettings() {
        return settings;
    }
}
