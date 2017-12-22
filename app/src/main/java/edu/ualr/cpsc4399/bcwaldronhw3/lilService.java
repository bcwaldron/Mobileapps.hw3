package edu.ualr.cpsc4399.bcwaldronhw3;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;


public class lilService extends Service {

    private NotificationManager nm;

    ArrayList<Messenger> clients = new ArrayList<Messenger>();

    private int oldTemp;
    private int newTemp;
    private int value;


    //initaliaze flags
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_VALUE = 3;
    static final int MSG_CONVERT_FtoC = 4;
    static final int MSG_CONVERT_CtoF = 5;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private int NOTIFICATION_ID = R.string.connect_toast;

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) { //handles incoming messages for registration, and set value

            switch (msg.what) {

                case MSG_REGISTER_CLIENT:
                    clients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    clients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    value = msg.arg1;
                    for (int i = clients.size() - 1; i >= 0; i--) {

                        try {
                            clients.get(i).send(Message.obtain(null, MSG_SET_VALUE, value, 0));
                        } catch (RemoteException e) {

                            clients.remove(i);
                        }
                    }
                    break;

                case MSG_CONVERT_CtoF: //will convert celsius to fahrenheit
                    try {
                        Log.i("lilService", "received MSG_CONVERT_CtoF");//log it
                        oldTemp = msg.arg1; //store original temp
                        newTemp = CtoF(oldTemp); //perform calculations and assign new temp
                        msg.replyTo.send(Message.obtain(null, MSG_CONVERT_CtoF,newTemp, 0)); //send the converted temp back
                        Log.i("lilService", "confirmed CtoF"); //log it
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case MSG_CONVERT_FtoC: //will convert fahrenheit to celsius
                    try{
                        Log.i("lilService", "received MSG_CONVERT_FtoC"); //log it
                        oldTemp = msg.arg1; //store old temp
                        newTemp = FtoC(oldTemp);//perform calculation, store new temp
                        msg.replyTo.send(Message.obtain(null, MSG_CONVERT_FtoC, newTemp, 0)); //return new temp
                        Log.i("lilService", "Confirmed, CONVERT_FtoC"); //log it
                    } catch (RemoteException e){
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }


        }
    }

    @Override
    public void onCreate(){ //when service is called, show toast notification

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Toast.makeText(this, R.string.service_started, Toast.LENGTH_SHORT).show();

        showNotification();
    }

    @Override
    public void onDestroy(){ //cancel the notification manager, create a toast notification

        nm.cancel(NOTIFICATION_ID);

        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent){

        return mMessenger.getBinder();
    }

    private void showNotification(){

        CharSequence text = getText(R.string.service_started);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0); //this can be called because MainActivity is only activity that will call this service

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setSmallIcon(R.drawable.ic_menu_refresh)
                .setContentText(text)
                .setContentIntent(contentIntent);

        nm.notify(NOTIFICATION_ID, builder.build());
    }

    private int CtoF(int x){

        int newTemp;

        newTemp = (x * 9 / 5) + 32;

        return newTemp;
    }

    private int FtoC(int x){

        int newTemp;

        newTemp = (x - 32) * 5 / 9;

        return newTemp;
    }
}
