/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.testmongodbmaven;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author Louis
 */
public class MongoUtil {
    
    private static MongoClient mongoClient;
    
    public static BasicDBObject madeQueryIsValue(String field, Object value){
        BasicDBObject query = new BasicDBObject();
        query.put(field, value);
        return query;
    }
    
    public static BasicDBObject madeQueryIsDifferent(String field, Object value){
        BasicDBObject query = new BasicDBObject();
        query.put(field, new BasicDBObject("$ne",value));
        return query;
    }
    
    public static BasicDBObject madeQueryIsBetween(String field, Object lowValue, Object highValue){
        BasicDBObject query = new BasicDBObject();
        query.put(field, new BasicDBObject("$gt",lowValue).append("$lt",highValue));
        return query;
    }
    
    public static BasicDBObject madeQueryAND(BasicDBObject dbObj1, BasicDBObject dbObj2){
        BasicDBObject query = new BasicDBObject();
        List<BasicDBObject> listObj = new ArrayList<BasicDBObject>();
        listObj.add(dbObj1);
        listObj.add(dbObj2);
        query.put("$and", listObj);
        return query;
    } 
    
    public static BasicDBObject madeQueryOR(BasicDBObject dbObj1, BasicDBObject dbObj2){
        BasicDBObject query = new BasicDBObject();
        List<BasicDBObject> listObj = new ArrayList<BasicDBObject>();
        listObj.add(dbObj1);
        listObj.add(dbObj2);
        query.put("$or", listObj);
        return query;
    }
    
    public static DBObject[] processQuery(DBCollection collection, BasicDBObject query){
        DBObject[] result = null;
        DBCursor cursor = collection.find(query);
        result = new DBObject[cursor.size()];
        int i = 0;
        while(cursor.hasNext()){
            result[i] = cursor.next();
            i++;
        }
        return result;
    }
    
    public static void printResult(DBObject[] result) {
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i]);
        }
    }
    
    public static MongoClient getMongoClient(String host, int port){
        if(mongoClient == null){
            try{
                mongoClient = new MongoClient(host,port);
            }catch(UnknownHostException e) {
                System.out.println(e.getMessage());
            }catch(MongoException e){
                System.out.println(e.getMessage());
            }	
        }	
        return mongoClient;
    }
}
