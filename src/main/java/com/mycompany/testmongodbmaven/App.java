package com.mycompany.testmongodbmaven;

import pt.ua.dicoogle.mongoplugin.MongoPlugin;
import com.mongodb.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.settings.Settings;

/**
 * Getting started with MongoDB
 *
 * @author Louis Beroud
 */
public class App{

    //private static String QUERY = "(AcquisitionDate:[19960227 TO 19970205] AND (Columns:[500.0 TO 600.0] OR Columns:396.0)) OR (PatientName:TOSHIBA AND BitsAllocated:16.0) AND NOT InstitutionName:kodak";
    private static String QUERY = "str:test";
    private static String pathConfigFile = "D:\\Louis\\Projects NetBeans\\Dicoogle\\MongoPlugin\\configClient.xml";
    
    public static void main(String[] args) {
        Settings settings = null;
        try{
            settings = new Settings(new File(pathConfigFile));
        }catch(IOException e){
            System.out.println("Error while opening the configuration file\n"+e.getMessage());
        }
        MongoPlugin plugin = new MongoPlugin(settings);
        plugin.enable();
        Iterable<SearchResult> res = plugin.query(QUERY, (Object[]) null);
        Iterator<SearchResult> it = res.iterator();
        while(it.hasNext())
            System.out.println(it.next().getExtraData().toString());
        plugin.disable();
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
