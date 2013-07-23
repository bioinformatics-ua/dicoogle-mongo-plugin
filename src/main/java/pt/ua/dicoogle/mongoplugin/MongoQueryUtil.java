/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
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
    
    public List<DBObject> processQuery(DBCollection collection){
        List<DBObject> result = new ArrayList<DBObject>();
        DBCursor cursor = collection.find(this.query);
        while(cursor.hasNext()){
            result.add(cursor.next());
        }
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
