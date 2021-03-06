package fiuba.mensajero;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;

import static android.app.AlertDialog.*;

/**
 * Activity principal de la aplicacion. Muestra la lista de conectados y algunos botones de opciones.
 */
public class ListViewFriendsActivity extends ListActivity implements MyResultReceiver.Receiver {
    public MyResultReceiver mReceiver;
    private ArrayList<UserData> contactos;
    private Handler handler;
    private int interval = 10000;   //intervalo entre updates
    private String searchInput;
    public static boolean forcelogout;


    /**
     * Inicializa variables. Inicia el servicio de actualizacion de posicion.
     * @param icicle -
     */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_listviewfriendsactivity);
        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        handler = new Handler();
        EditText et = (EditText) findViewById(R.id.buscar);
        et.addTextChangedListener(watch);
        searchInput = null;
        forcelogout = false;

        startService(new Intent(this, GPSupdater.class)); //servicio de updates de ubicacion gps

    }

    TextWatcher watch = new TextWatcher(){
        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
            if (isEmpty(s.toString()))
                searchInput = null;
            else
                searchInput = s.toString().toLowerCase();
            showList();
        }
    };

    Runnable listUpdater = new Runnable() {
        @Override
        public void run() {
            getUsersOnline();
            handler.postDelayed(listUpdater, interval);
        }
    };

    /**
     * LLama al servicio para obtener la lista de usuarios del servidor.
     */
    public void getUsersOnline() {
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("command", "getListaConectados");
        startService(intent);
    }


    /**
     * Inicia el update periodio de la lista de usuarios
     */
    @Override
    public void onResume() {
        super.onResume();
        if (forcelogout) {
            logout();
        } else
         listUpdater.run();
    }

    /**
     * Detiene el update periodio de la lista de usuarios
     */
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(listUpdater);

    }

    public void onStop() {
        super.onStop();
        handler.removeCallbacks(listUpdater);
    }

    /**
     * Detiene el servicio de actualizacion de posicion
     */
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, GPSupdater.class));
    }

    /**
     * Inicia la activity de chat al presionar sobre un contacto
     * @param l lista de contactos
     * @param v subitem de la lista
     * @param position posicion en la lista
     * @param id -
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        AlertDialog alertDialog = new Builder(this).create();

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("contacto", contactos.get(position));
        intent.putParcelableArrayListExtra("contactos", contactos);
        startActivity(intent);

        super.onListItemClick(l, v, position, id);
    }

    /**
     * Procesa la respuesta del servicio de red. Notifica errores con dialogos.
     * @param resultCode codigo de error del resultado
     * @param resultData datos de la respuesta
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (forcelogout)
            logout();
        switch (resultCode) {
            case NetworkService.RUNNING:
                Log.i("onreceiveresult", "esta corriendo el servicio");
                //aca se podria mostrar algo mientras el servicio esta corriendo
                break;
            case NetworkService.OK:
                Log.d("onreceiveresult", "se completo la operacion ");

                ArrayList<UserData> list = resultData.getParcelableArrayList("result");
                if (list == null)
                  Log.e("onreceiveresult lista", "error inesperado");
                else {
                    contactos = list;
                    if(!this.isFinishing()) {
                        showList();
                    }
                }
                break;
            case NetworkService.ERROR:
                AlertDialog alerta = new AlertDialog.Builder(this).create();
                alerta.setTitle("Error");
                String err = resultData.getString("error");
                if (!err.equals("No se pudo conectar con el servidor")) {
                    alerta.setMessage(err);
                    alerta.setButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    });
                    if (!isFinishing())
                        alerta.show();
                    Log.e("onreceiveresult", err);
                }

                break;
        }
    }

    /**
     * Desloguea al usuario actual y finaliza
     */
    public void logout() {
        LoginActivity.logout(this);
        finish();
    }

    boolean isEmpty(String s) {
        return  s.trim().length() == 0;
    }

    /**
     * Muestra la lista en pantalla, filtrando de acuerdo a lo especificado en el buscador
     */
    public void showList() {
        EditText et = (EditText) findViewById(R.id.buscar);
        //  String search = et.getText().toString().toLowerCase();
        ArrayList<UserData> newlist;
        if (searchInput == null)
            newlist = contactos;
        else {
            newlist = new ArrayList<>();
            for (UserData user : contactos) {
                String userName = user.getNombre();
                if (userName.toLowerCase().contains(searchInput))
                    newlist.add(user);
            }
        }
        AdaptFriends adapt = new AdaptFriends(this, newlist);
        setListAdapter(adapt);
    }

    public void searchFriends(View view) {
        showList();
    }

    /**
     * Inicia la activity de edicion de profile
     * @param view boton que hace el llamado
     */
    public void changeActivityProfile(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    // boton desconectar
    public void logout(View view) {
        LoginActivity.logout(this);
        finish();
    }

}
