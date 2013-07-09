/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import java.io.InputStream;
import java.net.URI;
import pt.ua.dicoogle.sdk.StorageInputStream;

/**
 *
 * @author Louis
 */
public class MongoStorageInputStream implements StorageInputStream {

    InputStream ins = null;
    URI uri = null;
    
    public MongoStorageInputStream(){
        
    }
    
    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public InputStream getInputStream() {
        return ins;
    }
}
