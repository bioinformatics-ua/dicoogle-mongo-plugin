/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.List;

/**
 *
 * @author Louis
 */
public class MongoQuery {
    
    private BasicDBObject query;
    
    public MongoQuery(){
        
    }
    
    public MongoQuery(String query){
        this.query = MongoUtil.parseStringToQuery(query);
    }
    
    public MongoQuery(BasicDBObject query){
        this.query = query;
    }
    
    public List<DBObject> processQuery(DBCollection collection) {
        DBObject[] result;
        DBCursor cursor = collection.find(this.query);
        return cursor.toArray();
        /*result = new DBObject[cursor.size()];
        int i = 0;
        while (cursor.hasNext()) {
            result[i] = cursor.next();
            i++;
        }
        return result;*/
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
