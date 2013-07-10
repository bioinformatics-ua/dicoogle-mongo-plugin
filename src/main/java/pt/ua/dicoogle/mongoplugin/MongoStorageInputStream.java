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
import com.mongodb.io.ByteBufferInputStream;
import java.io.FileInputStream;

/**
 *
 * @author Louis
 */

public class MongoStorageInputStream implements StorageInputStream {

    ByteBufferInputStream ins = null;
    URI uri = null;
    
    public MongoStorageInputStream(URI uri){
        this.uri = uri;
    }
    
    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public InputStream getInputStream() {
        if(MongoPlugin.mongoClient == null)
            return null;
        MongoURI mUri= new MongoURI(uri);
        mUri.getInformation();
        String fileName = mUri.getFileName();
        DB db = MongoPlugin.mongoClient.getDB(mUri.getDBName());
        GridFS fs = new GridFS(db);
        GridFSDBFile in = fs.findOne(fileName);
        return in.getInputStream();
    }
}
