package fiuba.mensajero;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerRequest {

    private String url;     //url base
    private String user;
    private String token;
    private String errormsg;


    public ServerRequest(Context c) throws ServerAddressNotFoundException {
        Context context;
        errormsg = null;
        SharedPreferences sharedPref= c.getSharedPreferences("appdata", 0);
        String ip = sharedPref.getString("ip", null);
        token = sharedPref.getString("token", null);
        user = sharedPref.getString("user", null);

        if (ip == null) {
            throw new ServerAddressNotFoundException("La IP del servidor no fue especificada");
        }
        url="http://" + ip + ":5000";
        Log.i("URL DEL SERVER ES", url);
    }

    public String getErrormsg() {
        return errormsg;
    }

    //devuelve un array con todos los usuarios o null si fallo el get
    public ArrayList<UserData> getUsersOnline () {
        String finalURL = url + "/usuarios" + "?r_user=" + this.user + "&token=" + this.token;
        RestMethod rest = new RestMethod();
        String resp = rest.GET(finalURL);
        int respCode = rest.getStatusCode();
        ArrayList<UserData> list = null ;
        JSONParser jp = new JSONParser();
        switch (respCode) {
            case 200:   list = jp.parseUsersOnline(resp);
                        break;
            case 401:   errormsg = jp.parseError(resp);
                        break;
            case -1:    errormsg = "No se pudo conectar con el servidor";
                        break;
            default:    errormsg = "Http protocol error: " + String.valueOf(respCode);
                        break;
        }

       return list;
    }

    //devuelve un string si el registro es exitoso o null si fallo
    public String register(String user, String password, String name) {
        String finalURL = url + "/usuario/" + user;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("password", password);
            jsonObject.accumulate("nombre", name);
            jsonObject.accumulate("foto", "vacio");
            jsonObject.accumulate("ubicacion", "vacio");
        }
        catch (JSONException e) {
            Log.e("register", "json fallo crear register body");
        }
        RestMethod rest = new RestMethod();
        String resp = rest.POST(finalURL, jsonObject);
        int respCode = rest.getStatusCode();

        String ret = null;
        switch (respCode) {
            case 201:   ret = "ok";
                        break;
            case 401:   JSONParser jp = new JSONParser();
                        errormsg = jp.parseError(resp);
                        break;
            case -1:    errormsg = "No se pudo conectar con el servidor";
                        break;
            default:    errormsg = "Http protocol error: " + String.valueOf(respCode);
                        break;
        }

        return ret;
    }

    //si tuvo exito devuelve el token valido, si fallo devuleve null
    public String logIn(String user, String password) {
        String finalURL = url + "/login";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("password", password);
            jsonObject.accumulate("id", user);
        }
        catch (JSONException e) {
            Log.e("register", "json fallo crear login body");
        }
        RestMethod rest = new RestMethod();
        String resp = rest.POST(finalURL, jsonObject);
        int respCode = rest.getStatusCode();
        String token = null;
        JSONParser jp = new JSONParser();
        switch (respCode) {
            case 201:   token = jp.parseToken(resp);
                        break;
            case 401:   errormsg = jp.parseError(resp);
                        break;
            case -1:    errormsg = "No se pudo conectar con el servidor";
                        break;
            default:    errormsg = "Http protocol error: " + String.valueOf(respCode);
                        break;
        }
        return token;
    }


    //devuelve un array con todos los mensajes o null si fallo el get
    public ArrayList<MessageData> getMessages(String user2) {
        String finalURL = url + "/conversacion/" + user2 + "?r_user=" + user + "&token=" + token;
        RestMethod rest = new RestMethod();
        String resp = rest.GET(finalURL);
        int respCode = rest.getStatusCode();
        ArrayList<MessageData> list = null;
        JSONParser jp = new JSONParser();
        switch (respCode) {
            case 200:   list = jp.parseMessages(resp);
                        break;
            case 401:   errormsg = jp.parseError(resp);
                        break;
            case -1:    errormsg = "No se pudo conectar con el servidor";
                        break;
            default:    errormsg = "Http protocol error: " + String.valueOf(respCode);
                        break;
        }
        return list;
    }

    //si no se especifica user2 (destinatario) se envia el mensaje a todos los usuarios
    public String sendMessage(String user2, String message) {
        String finalURL;
        if (user2 == null)
            finalURL = url + "/broadcast/" + "?r_user=" + user + "&token=" + token;
        else
            finalURL = url + "/conversacion/" + user2 + "?r_user=" + user + "&token=" + token;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("message", message);
        }
        catch (JSONException e) {
            Log.e("register", "json fallo crear register body");
        }
        RestMethod rest = new RestMethod();
        String resp = rest.POST(finalURL, jsonObject);
        int respCode = rest.getStatusCode();

        String ret = null;
        switch (respCode) {
            case 201:   ret = "ok";
                        break;
            case 401:   JSONParser jp = new JSONParser();
                        errormsg = jp.parseError(resp);
                        break;
            case -1:    errormsg = "No se pudo conectar con el servidor";
                        break;
            default:    errormsg = "Http protocol error: " + String.valueOf(respCode);
                        break;
        }
        return ret;
    }

    public ProfileData getProfile(String user2) {
        String finalURL = url + "/usuario/" + user2 + "?r_user=" + user + "&token=" + token;
        RestMethod rest = new RestMethod();
        String resp = rest.GET(finalURL);
        int respCode = rest.getStatusCode();
        ProfileData ret = null;
        JSONParser jp = new JSONParser();
        switch (respCode) {
            case 200:   ret = jp.parseProfile(resp);
                break;
            case 401:   errormsg = jp.parseError(resp);
                break;
            case -1:    errormsg = "No se pudo conectar con el servidor";
                break;
            default:    errormsg = "Http protocol error: " + String.valueOf(respCode);
                break;
        }
        return ret;
    }


    public String editProfile(String nombre, String password, String foto) {
        String finalURL = url + "/usuario/" + user + "?r_user=" + user + "&token=" + token;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("password", password);
            jsonObject.accumulate("nombre", nombre);
            jsonObject.accumulate("foto", foto);
        }
        catch (JSONException e) {
            Log.e("register", "json fallo crear register body");
        }
        RestMethod rest = new RestMethod();
        String resp = rest.PUT(finalURL, jsonObject);
        int respCode = rest.getStatusCode();

        String ret = null;
        switch (respCode) {
            case 201:   ret = "ok";
                        break;
            case 401:   JSONParser jp = new JSONParser();
                        errormsg = jp.parseError(resp);
                        break;
            case -1:    errormsg = "No se pudo conectar con el servidor";
                        break;
            default:    errormsg = "Http protocol error: " + String.valueOf(respCode);
                        break;
        }

        return ret;
    }
}
