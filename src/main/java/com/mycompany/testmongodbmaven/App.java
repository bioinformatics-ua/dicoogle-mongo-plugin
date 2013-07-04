package com.mycompany.testmongodbmaven;

import com.mongodb.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Getting started with MongoDB
 *
 * @author Louis Beroud
 */
public class App {

    private static String host = "localhost";
    private static int port = 27017;

    public static void main(String[] args) {
        // Get the Mongo Client
        MongoClient mongoClient = MongoUtil.getMongoClient(host, port);
        DB db = mongoClient.getDB("DICOMtest");
        DBCollection collection = db.getCollection("DICOMdata");
        //initData(collection);
        
        System.out.println("Query : BitsAllocated = 8.0");
        BasicDBObject query = MongoUtil.madeQueryIsValue("BitsAllocated", 8.0);
        DBObject[] resultDBobjs = MongoUtil.processQuery(collection, query);
        MongoUtil.printResult(resultDBobjs);
        
        System.out.println("Query : BitsAllocated = 8.0 AND Columns = 512.0");
        query = MongoUtil.madeQueryAND(MongoUtil.madeQueryIsValue("BitsAllocated", 8.0),
                                        MongoUtil.madeQueryIsValue("Columns", 512.0));
        resultDBobjs = MongoUtil.processQuery(collection, query);
        MongoUtil.printResult(resultDBobjs);
        
        System.out.println("Query : (BitsAllocated = 8.0 AND Columns = 512.0) OR PatientName = TOSHIBA^TARO");
        query = MongoUtil.madeQueryOR(query, MongoUtil.madeQueryIsValue("PatientName", "TOSHIBA^TARO"));
        resultDBobjs = MongoUtil.processQuery(collection, query);
        MongoUtil.printResult(resultDBobjs);
    }

    private static void initData(DBCollection collection) {
        BasicDBObject document = new BasicDBObject();
        document.put("AcquisitionDate", 19970205);
        document.put("AcquisitionNumber", 890.0);
        document.put("AcquisitionTime", 074737);
        document.put("BitsAllocated", 8.0);
        document.put("BitsStored", 8.0);
        document.put("Columns", 396.0);
        document.put("ConversionType", "WSD ");
        document.put("InstitutionName", "BOSTON MED CENT E.N.C   ");
        document.put("PatientName", "CT BRONCHOSCOPY ");
        collection.insert(document);

        document = new BasicDBObject();
        document.put("AcquisitionDate", 19960227);
        document.put("AcquisitionTime", 082430.000);
        document.put("BitsAllocated", 8.0);
        document.put("BitsStored", 8.0);
        document.put("Columns", 512.0);
        document.put("InstitutionName", "kodak");
        document.put("PatientName", "KODAK_DEMO_DISK ");
        collection.insert(document);

        document = new BasicDBObject();
        document.put("AcquisitionNumber", 6.0);
        document.put("BitsAllocated", 16.0);
        document.put("BitsStored", 16.0);
        document.put("Columns", 512.0);
        document.put("InstitutionName", "TOSHIBA     ");
        document.put("PatientName", "TOSHIBA^TARO");
        collection.insert(document);

        document = new BasicDBObject();
        document.put("BitsAllocated", 8.0);
        document.put("BitsStored", 8.0);
        document.put("Columns", 512.0);
        document.put("ImageComments", "Hicor DCM ");
        document.put("InstitutionName", "THORAX CENTER ROTTERDAM ");
        document.put("PatientTelephoneNumbers", 19990401);
        collection.insert(document);
    }

    public static int[] getByteArrayFromFile(File file) {
        long taille = file.length();
        BufferedInputStream bis;
        int byteReaded, i = 0;
        int[] byteArray = new int[(int) taille];
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            try {
                while ((byteReaded = bis.read()) != -1) {
                    byteArray[i] = byteReaded;
                    i++;
                }
            } finally {
                bis.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found : " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error while reading the file : " + e.getMessage());
        }
        return byteArray;
    }
}
