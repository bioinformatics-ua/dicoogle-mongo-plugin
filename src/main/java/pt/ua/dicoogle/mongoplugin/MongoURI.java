/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.dicoogle.mongoplugin;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Louis
 */
public class MongoURI {

    private URI uri;
    private String dbName;
    private String colletionName;
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

    public void getInformation() {
        String path = uri.getPath();
        String str = "";
        int i = 1;
        char currentChar = 0;
        System.out.println(path);
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
        colletionName = str;
        str = "";
        while (i < path.length()) {
            currentChar = path.charAt(i);
            i++;
            if (currentChar != '/') {
                str += currentChar;
            }
        }
        uID = str;
    }
//"://host:port/dataBaseName/collectionName"
    public boolean verify() {
        String uri = this.uri.toString();
        String str = "";
        int cmp = 0, i = 0, nbSlash = 0;
        char currentChar = 0;
        while (currentChar != ':' && cmp < uri.length()) {
            currentChar = uri.charAt(cmp);
            cmp++;
            if(currentChar != ':')
                str += currentChar;
        }   
        if (!str.equals("mongodb") || cmp+2>=uri.length()) {
            return false;
        }
        if(!uri.substring(cmp, cmp+2).equals("//"))
            return false;
        cmp += 2;
        currentChar = uri.charAt(cmp);
        while (currentChar != ':' && cmp < uri.length()) {
            currentChar = uri.charAt(cmp);
            cmp++;
            if(currentChar != ':')
                i++;
        }
        if(i==0)
            return false;
        currentChar = 0;
        i = 0;
        while (currentChar != '/' && cmp < uri.length()) {
            currentChar = uri.charAt(cmp);
            cmp++;
            if(currentChar != '/')
                i++;
        }
        if(i==0)
            return false;
        currentChar = 0;
        i = 0;
        while (currentChar != '/' && cmp < uri.length()) {
            currentChar = uri.charAt(cmp);
            cmp++;
            if(currentChar != '/')
                i++;
        }
        if(i==0)
            return false;
        currentChar = 0;
        i = 0;
        while (currentChar != '/' && cmp < uri.length()) {
            currentChar = uri.charAt(cmp);
            cmp++;
            if(currentChar != '/')
                i++;
        }
        if(i==0)
            return false;
        if(cmp >= uri.length())
            return false;
        return true;
    }

    public String getDBName() {
        return dbName;
    }

    public String getCollectionName() {
        return colletionName;
    }

    public String getUID() {
        return uID;
    }
}
