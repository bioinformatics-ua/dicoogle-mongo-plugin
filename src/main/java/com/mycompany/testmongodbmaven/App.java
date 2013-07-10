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
    private static String QUERY = "str:t*t";
    private static String pathConfigFile = ".\\configClient.xml";
    
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
