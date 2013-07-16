/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        if (MongoPlugin.mongoClient == null) {
            return null;
        }
        MongoURI mUri = new MongoURI(uri);
        if (mUri.verify()) {
            mUri.getInformation();
            String fileName = mUri.getFileName();
            DB db = MongoPlugin.mongoClient.getDB(mUri.getDBName());
            GridFS fs = new GridFS(db);
            GridFSDBFile in = fs.findOne(fileName);
            if (in == null) {
                return null;
            }
            return in.getInputStream();
        }
        else{
            File f = new File(this.getURI());
            try {
                FileInputStream fis = new FileInputStream(f);
                return fis;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MongoStorageInputStream.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}
