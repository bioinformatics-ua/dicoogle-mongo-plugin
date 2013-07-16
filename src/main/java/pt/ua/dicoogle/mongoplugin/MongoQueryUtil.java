/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.util.List;

/**
 *
 * @author Louis
 */
public class MongoQueryUtil {
    
    private BasicDBObject query;
    
    public MongoQueryUtil(){
        
    }
    
    public MongoQueryUtil(String query){
        this.query = MongoUtil.parseStringToQuery(query);
    }
    
    public MongoQueryUtil(BasicDBObject query){
        this.query = query;
    }
    
    public List<GridFSDBFile> processQuery(DB database) {
        GridFS fs = new GridFS(database);
        List<GridFSDBFile> result = fs.find(this.query);
        return result;
    }
    
    public void setQuery(String query){
        this.query = MongoUtil.parseStringToQuery(query);
    }
    
    public void setQuery(BasicDBObject query){
        this.query = query;
    }
    
    public BasicDBObject getQuery(){
        return this.query;
    }
}
