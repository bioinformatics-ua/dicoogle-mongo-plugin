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
    private static String pathConfigFile;
    private static String QUERY;
    public static void main(String[] args) {
        if(!retrieveArg(args)){
            System.out.println("---------HELP----------");
            System.out.println("You have to set the query and the path of the configuration file");
            System.out.println("-q [QUERY] - set the query");
            System.out.println("-c [pathConfigurationFile] - set configuration file");
            System.out.println("EXEMPLE : MongoPlugin.java -q BitsStored:12 AND FilterType:0 -c .\\settings\\mongoplugin.xml");
            return;
        }
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
        int cmp = 0;
        while(it.hasNext()){
            System.out.println(it.next().getExtraData().toString());
            cmp++;
        }
        plugin.disable();
        System.err.println("Number of result : " + cmp);
    }

    private static boolean retrieveArg(String[] args){
        boolean query = false, path = false;
        if(args.length == 0)
            return false;
        QUERY = "";
        pathConfigFile = "";
        for(int i=0; i<args.length; i++){
            if(args[i].equals("-q")){
                query = true;
                path = false;
            }
            if(args[i].equals("-c")){
                query = false;
                path = true;
            }
            if(query && !args[i].equals("-q"))
                QUERY += (" " + args[i]);
            if(path && !args[i].equals("-c"))
                pathConfigFile += (args[i]);
        }
        if(QUERY.length() == 0 || pathConfigFile.length() == 0)
            return false;
        return true;
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
