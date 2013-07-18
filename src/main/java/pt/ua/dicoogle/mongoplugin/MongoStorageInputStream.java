/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.InputStream;
import java.net.URI;
import pt.ua.dicoogle.sdk.StorageInputStream;

/**
 *
 * @author Louis
 */
public class MongoStorageInputStream implements StorageInputStream {

    URI uri = null;

    public MongoStorageInputStream(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public InputStream getInputStream() {
        if (MongoPluginSet.mongoClient == null) {
            return null;
        }
        MongoURI mUri = new MongoURI(this.uri);
        mUri.getInformation();
        DB db = MongoPluginSet.mongoClient.getDB(mUri.getDBName());
        GridFS fs = new GridFS(db);
        GridFSDBFile in = fs.findOne(mUri.getFileName());
        if (in == null) {
            return null;
        }
        return in.getInputStream();
    }
}
