package fiuba.mensajero;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

/**
 * Activity para el logueo de un usuario.
 */
public class LoginRegActivity extends ActionBarActivity implements MyResultReceiver.Receiver {

    public MyResultReceiver mReceiver;

    /**
     * Inicializa variables. Si existe el usuario localmente, loguea automaticamente
     * @param savedInstanceState -
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_reg);
        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = getIntent();
        if (intent.hasExtra("user")) {
            logIn(intent.getStringExtra("user"), intent.getStringExtra("password"));
        }
    }

    public boolean isEmpty(String s) {
        return s.trim().length() == 0;
    }

    /**
     * LLama al metodo logIn() si los campos no estan vacios.
     * @param view boton que invoca el metodo
     */
    public void handTerminar(View view) {
        EditText nom = (EditText) findViewById(R.id.editTextNombre);
        String user = nom.getText().toString();
        EditText pass = (EditText) findViewById(R.id.editTextPass);
        String password = pass.getText().toString();
        if (isEmpty(password) || isEmpty(user)) {
            AlertDialog alerta = new AlertDialog.Builder(this).create();
            alerta.setTitle("Error");
            alerta.setMessage("Ingrese los datos requeridos");
            alerta.setButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    EditText user = (EditText) findViewById(R.id.editTextNombre);
                    user.setText("");
                    EditText pass = (EditText) findViewById(R.id.editTextPass);
                    pass.setText("");
                }
            });
            //alerta.setIcon(R.drawable.noo);
            alerta.show();
        }
        else {
            SharedPreferences sharedPref = getSharedPreferences("appdata", 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("user", user);
            editor.putString("password", password);

            editor.commit();
            logIn(user, password);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_reg, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Invoca al servicio de logueo.
     * @param user Id del usuario a loguear
     * @param password password del usuario a loguear
     */
    public void logIn(String user, String password) {
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("command", "logIn");
        intent.putExtra("user", user);
        intent.putExtra("password", password);
        startService(intent);
    }

    /**
     * Procesa la respuesta del servicio de red. Notifica el resultado con dialogos.
     * @param resultCode codigo de error del resultado
     * @param resultData datos de la respuesta
     */
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case NetworkService.RUNNING:
                Log.i("onreceiveresult", "esta corriendo el servicio de login");
                //aca se podria mostrar algo mientras el servicio esta corriendo
                break;
            case NetworkService.OK:
                Log.d("onreceiveresult", "se completo la operacion de login ");
                String res = resultData.getString("result");
                String token = res.substring(0,16);
                String nombre = res.substring(16);
                if (res == null)
                    Log.e("ONRECEIVERESULT REG", "error inesperado");
                else {
                    //guardo el token en sharedpreferences
                    SharedPreferences sharedPref = getSharedPreferences("appdata", 0);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("token", token);
                    editor.putString("nombre", nombre);
                    editor.commit();
                    Log.i("TOKEN OBTENIDO: ", token);
                    Intent flist = new Intent(this, ListViewFriendsActivity.class);
                    flist.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(flist);
                    finish();

                }
                break;
            case NetworkService.ERROR:
                SharedPreferences sharedPref = getSharedPreferences("appdata", 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("user");
                editor.remove("password");
                editor.commit();
                AlertDialog alerta = new AlertDialog.Builder(this).create();
                alerta.setTitle("Error");
                String err = resultData.getString("error");
                alerta.setMessage(err);
                alerta.setButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });
                //alerta.setIcon(R.drawable.noo);
                alerta.show();
                Log.e("onreceiveresult", "salto un error en login");
                break;
        }
    }

    /**
     * Termina activity y desloguea al usuario.
     */
    public void logout() {
        LoginActivity.logout(this);
        finish();
    }

}
