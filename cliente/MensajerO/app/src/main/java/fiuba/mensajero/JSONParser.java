package fiuba.mensajero;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Clase para extraer la informacion de los JSON provenientes del servidor
 */
public class JSONParser {

    /**
     * Convierte un string JSON en una lista de usuarios
     * @param jsonstr JSON string a parsear
     * @return ArrayList de tipo UserData con la informacion de los usuarios
     */
    public ArrayList<UserData> parseUsersOnline(String jsonstr) {
        ArrayList<UserData> list = new ArrayList<>();
        String nombre, estado, id, foto;
        boolean nuevomsg, nuevobroadcast;
        try {
            JSONObject result = new JSONObject(jsonstr);
            nuevobroadcast = result.getBoolean("nuevobroadcast");
            JSONArray users = result.getJSONArray("usuarios");
            UserData ud = new UserData("broadcast", "Conversacion grupal", true, nuevobroadcast, null);
            list.add(ud);
            for (int i = 0; i < users.length(); i++) {
                boolean conectado = false;
                JSONObject user = users.getJSONObject(i);
                nombre = user.getString("nombre");
                estado = user.getString("estado");
                id = user.getString("id");
                nuevomsg = user.getBoolean("nuevomsg");
                if (estado.equals("conectado")) {
                    conectado = true;
                }
                foto = user.getString("fotochica");
                UserData userData = new UserData(id, nombre, conectado, nuevomsg, foto);
                list.add(userData);
            }
        }
        catch (JSONException e) {
            Log.e("JSON PARSER", "error al parsear lista de usaurios", e);
        }

        return list;
    }

    /**
     * Convierte un string JSON en una lista de mensajes
     * @param jsonstr JSON string a parsear
     * @return ArrayList de tipo MessageData con la informacion de los mensajes de una conversacion
     */
    public ArrayList<MessageData> parseMessages(String jsonstr) {
        ArrayList<MessageData> list = new ArrayList<>();
        String id, time, message;
        int t;
        try {
            JSONArray msgs = new JSONArray(jsonstr);
            for (int i = 0; i < msgs.length(); i++) {
                JSONObject us = msgs.getJSONObject(i);
                id = us.getString("id");
                t = us.getInt("time");
                long dv = (long)t* 1000L;
                Date date = new Date(dv);
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
                df.setTimeZone(TimeZone.getTimeZone("GMT-3"));
                time = df.format(date);
                message = us.getString("msg");
                MessageData msgdata = new MessageData(id, time, message);
                list.add(msgdata);
            }
        }
        catch (JSONException e) {
            Log.e("JSON PARSER", "error al parsear mensajes", e);
        }

        return list;
    }

    /**
     * Convierte un string JSON en un string de un mensaje de error
     * @param jsonstr JSON string a parsear
     * @return String con el mensaje de error
     */
    public String parseError(String jsonstr) {
        String error = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonstr);
            error = jsonObject.getString("error");
        }
        catch (JSONException e) {
            Log.e("JSON PARSER", "error al obtener mensaje de error");
        }

        return error;
    }

    /**
     * Convierte un string JSON en un string de un token
     * @param jsonstr JSON string a parsear
     * @return String con el token
     */
    public String parseToken(String jsonstr) {
        String token = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonstr);
            token = jsonObject.getString("token");
        }
        catch (JSONException e) {
            Log.e("JSON PARSER", "error al obtener el token");
        }

        return token;
    }

    boolean isEmpty(String s) {
        return  s.trim().length() == 0;
    }

    /**
     * Convierte un string JSON en un objeto ProfileData con informacion de un perfil
     * @param jsonstr JSON string a parsear
     * @return ProfileData con informacion de un perfil
     */
    public ProfileData parseProfile(String jsonstr) {
        ProfileData profile = null;
        String nombre, foto, ultimoacceso, telefono, email, ubicacion;
        int t;
        try {
            JSONObject jsonObject = new JSONObject(jsonstr);
            nombre = jsonObject.getString("nombre");
            if (isEmpty(nombre)) nombre = null;

            foto = jsonObject.getString("foto");
            if (isEmpty(foto)) foto = null;

            ubicacion = jsonObject.getString("ubicacion");
            if (isEmpty(ubicacion)) ubicacion = null;

            telefono = jsonObject.getString("telefono");
            if (isEmpty(telefono)) telefono = null;

            email = jsonObject.getString("email");
            if (isEmpty(email)) email = null;

            t = jsonObject.getInt("ultimoacceso");
            long dv = (long)t* 1000L;
            Date date = new Date(dv);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT-3"));
            ultimoacceso = df.format(date);
            profile = new ProfileData(nombre, foto, ultimoacceso, telefono, email, ubicacion);

        }
        catch (JSONException e) {
            Log.e("JSON PARSER", "error al parsear un profile");
        }
        return profile;
    }
}
