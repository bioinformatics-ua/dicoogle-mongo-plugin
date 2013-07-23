/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import java.net.UnknownHostException;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.configuration.ConfigurationException;
import pt.ua.dicoogle.sdk.IndexerInterface;
import pt.ua.dicoogle.sdk.PluginBase;
import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

/**
 *
 * @author Louis
 */
@PluginImplementation
public class MongoPluginSet extends PluginBase {

    private String host;
    private int port;
    protected static MongoClient mongoClient = null;
    private MongoQuery plugQuery;
    private MongoIndexer plugIndexer;
    private MongoStorage plugStorage;
    private static String hostKey = "DefaultServerHost";
    private static String portKey = "DefaultServerPort";
    
    public MongoPluginSet() {
       System.out.println("INIT-->MongoDb plugin");
        
        plugQuery = new MongoQuery();
        this.queryPlugins.add(plugQuery);
        plugIndexer = new MongoIndexer();
        this.indexPlugins.add(plugIndexer);
        plugStorage = new MongoStorage();
        this.storagePlugins.add(plugStorage);
    }

   public MongoPluginSet(ConfigurationHolder settings) throws ConfigurationException {
        plugQuery = new MongoQuery();
        plugIndexer = new MongoIndexer();
        plugStorage = new MongoStorage();

        this.settings = settings;
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        try {
            if (mongoClient == null) {
                mongoClient = new MongoClient(host, port);
            }
        } catch (UnknownHostException e) {
            return ;
        } catch (MongoException e) {
            return;
        }
        plugQuery.setSettings(this.settings);
        plugIndexer.setSettings(this.settings);
        plugStorage.setSettings(this.settings);
        plugQuery.enable();
        plugIndexer.enable();
        plugStorage.enable();
        this.queryPlugins.add(plugQuery);
        this.indexPlugins.add(plugIndexer);
        this.storagePlugins.add(plugStorage);
    }
    
    @Override
    public String getName() {
        return "mongoplugin";
    }

    @Override
    public void setSettings(ConfigurationHolder settings) {
        this.settings = settings;
        host = settings.getConfiguration().getString(hostKey);
        port = settings.getConfiguration().getInt(portKey);
        
        try {
            if (mongoClient == null) {
                mongoClient = new MongoClient(host, port);
            }
        } catch (UnknownHostException e) {
            return ;
        } catch (MongoException e) {
            return ;
        }
        for(QueryInterface plugin : this.getQueryPlugins()){
            plugin.setSettings(settings);
            plugin.enable();
        }
        for(IndexerInterface plugin : this.getIndexPlugins()){
            plugin.setSettings(settings);
            plugin.enable();
        }
        for(StorageInterface plugin : this.getStoragePlugins()){
            plugin.setSettings(settings);
            plugin.enable();
        }
    }

    @Override
    public ConfigurationHolder getSettings() {
        return settings;
    }
    
}
