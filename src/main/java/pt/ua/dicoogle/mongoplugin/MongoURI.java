/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Louis
 */
public class MongoURI {

    private URI uri;
    private String dbName;
    private String fileName;
    private String uID;

    public MongoURI(String uri) {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            this.uri = null;
        }
    }

    public MongoURI(URI uri) {
        this.uri = uri;
    }

    private void getInformationNoMongo() {
        String str = this.uri.toString();
        int index = str.lastIndexOf("/");
        if(index == -1)
            index = str.lastIndexOf("\\");
        String result = str.substring(index + 1);
        if (result.endsWith(".dcm")) {
            result = result.substring(0, result.length() - 4);
        }
        this.fileName = result;
    }

    public void getInformation() {
        if (!this.verify()) {
            getInformationNoMongo();
            return;
        }
        String path = uri.getPath();
        String str = "";
        int i = 1;
        char currentChar = 0;
        while (currentChar != '/') {
            currentChar = path.charAt(i);
            i++;
            if (currentChar != '/') {
                str += currentChar;
            }
        }
        dbName = str;
        str = "";
        currentChar = 0;
        while (currentChar != '/' && i < path.length()) {
            currentChar = path.charAt(i);
            i++;
            if (currentChar != '/') {
                str += currentChar;
            }
        }
        fileName = str;
    }

    public boolean verify() {
        String strUri = this.uri.toString();
        String str = "";
        int cmp = 0, i = 0;
        char currentChar = 0;
        while (currentChar != ':' && cmp < strUri.length()) {
            currentChar = strUri.charAt(cmp);
            cmp++;
            if (currentChar != ':') {
                str += currentChar;
            }
        }
        if (!str.equals("mongodb") || cmp + 2 >= strUri.length()) {
            return false;
        }
        if (!strUri.substring(cmp, cmp + 2).equals("//")) {
            return false;
        }
        cmp += 2;
        currentChar = strUri.charAt(cmp);
        while (currentChar != ':' && cmp < strUri.length()) {
            currentChar = strUri.charAt(cmp);
            cmp++;
            if (currentChar != ':') {
                i++;
            }
        }
        if (i == 0) {
            return false;
        }
        currentChar = 0;
        i = 0;
        while (currentChar != '/' && cmp < strUri.length()) {
            currentChar = strUri.charAt(cmp);
            cmp++;
            if (currentChar != '/') {
                i++;
            }
        }
        if (i == 0) {
            return false;
        }
        currentChar = 0;
        i = 0;
        while (currentChar != '/' && cmp < strUri.length()) {
            currentChar = strUri.charAt(cmp);
            cmp++;
            if (currentChar != '/') {
                i++;
            }
        }
        if (i == 0) {
            return false;
        }
        currentChar = 0;
        i = 0;
        while (currentChar != '/' && cmp < strUri.length()) {
            currentChar = strUri.charAt(cmp);
            cmp++;
            if (currentChar != '/') {
                i++;
            }
        }
        if (i == 0) {
            return false;
        }
        return true;
    }

    public String getDBName() {
        return dbName;
    }

    public String getFileName() {
        String str = this.uri.toString();
        int index = str.lastIndexOf("/");
        if(index == -1)
            index = str.lastIndexOf("\\");
        String result = str.substring(index + 1);
        if (result.endsWith(".dcm")) {
            result = result.substring(0, result.length() - 4);
        }
        return result;
    }

    public String getUID() {
        return uID;
    }
}
