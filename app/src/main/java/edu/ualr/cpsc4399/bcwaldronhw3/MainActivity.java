package edu.ualr.cpsc4399.bcwaldronhw3;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity  extends AppCompatActivity{

    private Button FtoCbutton;
    private Button CtoFbutton;
    private Messenger boundService = null;
    private boolean isBound = false;
    private EditText FtoC;
    private EditText CtoF;
    private TextView FtoCResult;
    private TextView CtoFResult;
    private int originalTemp;
    private int newTemp;
    private final Messenger mess = new Messenger(new IncomingHandler());

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign ui components
        FtoCbutton = (Button) findViewById(R.id.FtoCbutton);
        CtoFbutton = (Button) findViewById(R.id.CtoFbutton);
        FtoC = (EditText) findViewById(R.id.FtoCedit_text);
        CtoF = (EditText) findViewById(R.id.CtoFedit_text);
        FtoCResult = (TextView) findViewById(R.id.FtoCresult);
        CtoFResult = (TextView) findViewById(R.id.CtoFresult);

        doBindService();

        FtoCbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){


                originalTemp = Integer.parseInt(FtoC.getText().toString()); //gets original temperature from the editText

                Message msg = Message.obtain(null, lilService.MSG_CONVERT_FtoC, originalTemp, 0); //send message to the service, to perform calculations
                msg.replyTo = mess; //send the msg
                try{
                    Log.i("MainActivity", "before send()");
                    boundService.send(msg);
                    Log.i("MainActivity", "after send()");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        CtoFbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                originalTemp = Integer.parseInt(CtoF.getText().toString());//assign original temp from editText

                Message msg = Message.obtain(null, lilService.MSG_CONVERT_CtoF, originalTemp, 0); //build msg with original temp
                msg.replyTo = mess; //send it to the service
                try{
                    Log.i("MainActivity", "before send()");
                    boundService.send(msg);
                    Log.i("MainActivity", "after send()");
                } catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
        doUnbindService(); //unbind from lilService
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            boundService = new Messenger(service);

            try{

                Message msg = Message.obtain(null, lilService.MSG_REGISTER_CLIENT); //register the activity with lilService
                msg.replyTo = mess;
                boundService.send(msg);
                msg = Message.obtain(null, lilService.MSG_SET_VALUE, this.hashCode(), 0);
                boundService.send(msg);
            } catch (RemoteException e){


            }
            Toast.makeText(MainActivity.this, R.string.connect_toast, Toast.LENGTH_SHORT).show(); //show toast notification
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            boundService = null;

            Toast.makeText(MainActivity.this, R.string.disconnect_toast, Toast.LENGTH_SHORT).show(); //show toast

        }
    };

    void doBindService(){

        Intent i = new Intent(MainActivity.this, lilService.class); //create intent to bind lilService

        bindService(i, connection, Context.BIND_AUTO_CREATE); //bind to lilService
        isBound = true; //set bound flag

    }

    void doUnbindService(){

        if(isBound){

            if(boundService != null){
                try{
                    Message msg = Message.obtain(null, lilService.MSG_UNREGISTER_CLIENT); //attempt to unregister from lilService

                    msg.replyTo = mess;
                    boundService.send(msg);
                } catch (RemoteException e) {

                }
            }

            //detach connection

            unbindService(connection);
            isBound = false;

        }
    }

    class IncomingHandler extends Handler { //handles incoming messages from lilService

        @Override
        public void handleMessage(Message msg){

            switch (msg.what){
                case lilService.MSG_SET_VALUE: //error response
                    FtoCResult.setText("Received from service: " + msg.arg1);
                    break;
                case lilService.MSG_CONVERT_CtoF: //store and display new temp
                    newTemp = msg.arg1;
                    CtoFResult.setText(originalTemp + " in Celsius is equal to " + newTemp + " in Fahrenheit");
                    CtoFResult.setVisibility(View.VISIBLE);
                    break;
                case lilService.MSG_CONVERT_FtoC: //store and display new temp
                    newTemp = msg.arg1;
                    FtoCResult.setText(originalTemp + " in Fahrenheit is equal to " + newTemp + " in Celsius");
                    FtoCResult.setVisibility(View.VISIBLE);
                    break;
                default:
                    super.handleMessage(msg);


            }
        }
    }

}
