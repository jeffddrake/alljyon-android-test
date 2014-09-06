/*
 * Copyright (c) 2011, AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.alljoyn.bus.sample.chat;

import org.alljoyn.bus.sample.chat.ChatApplication;
import org.alljoyn.bus.sample.chat.Observable;
import org.alljoyn.bus.sample.chat.Observer;
import org.alljoyn.bus.sample.chat.DialogBuilder;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.app.Activity;
import android.app.Dialog;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.util.Log;
import android.widget.TextView.OnEditorActionListener;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONStringer;

public class UseActivity extends Activity implements Observer {
    private static final String TAG = "chat.UseActivity";

    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.use);

        mHistoryList = new ArrayAdapter<String>(this, android.R.layout.test_list_item);
        ListView hlv = (ListView) findViewById(R.id.useHistoryList);
        hlv.setAdapter(mHistoryList);

        mChatApplication = (ChatApplication)getApplication();

        mChatApplication.useSetChannelName("test");
        mChatApplication.useJoinChannel();

//JDD - removed for now as we do not need to type in text
//        EditText messageBox = (EditText)findViewById(R.id.useMessage);
//        messageBox.setImeActionLabel("Send", KeyEvent.KEYCODE_ENTER);
//
//        messageBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            public boolean onEditorAction(TextView View, int actionId, KeyEvent event) {
//
//                String message = View.getText().toString();
//                Log.i(TAG, "useMessage.onEditorAction(): got message " + message + ")");
//                mChatApplication.newLocalUserMessage(message);
//                View.setText("");
//
//                return true;
//            }
//
//         });

        mJoinButton = (Button)findViewById(R.id.useJoin);
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_JOIN_ID);
            }
        });

        mStartOven = (Button)findViewById(R.id.useStartOven);
        mStartOven.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //JDD - repurposed this is not the join button any more
                //JDD - it's to start the oven
                JSONObject obj = new JSONObject();
                try {
                    obj.put("mode", new Integer(18));
                    obj.put("cookTemperature", new Integer(350));
                    obj.put("cookHours", new Integer(1));
                    obj.put("cookMinutes", new Integer(0));
                }
                catch (Exception e)
                {
                    Log.i(TAG, e.toString());
                }

                String packet = obj.toString();
                mChatApplication.newLocalUserMessage(packet);
            }
        });

        mToggleLight = (Button)findViewById(R.id.useToggleLight);
        mToggleLight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //JDD - repurposed this is not the join button any more
                //JDD - it's to start the oven
                String packet = "hello, toggle light";
                mChatApplication.newLocalUserMessage(packet);
            }
        });

        mTestNotification = (Button)findViewById(R.id.useTestNotificationSend);
        mTestNotification.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //JDD - repurposed this is not the join button any more
                //JDD - it's to start the oven
                String packet = "hello, test notification";
                mChatApplication.newLocalUserMessage(packet);

                String replyLabel = getString(R.string.voice_notification_label);
                RemoteInput remoteInput = new RemoteInput.Builder(getResources().getString(R.string.EXTRA_VOICE_REPLY))
                        .setLabel(replyLabel)
                        .build();

                //JDD - create a new intent
                //Intent replyIntent = new Intent(this, DialogBuilder.class);
                Intent replyIntent = new Intent(getApplicationContext(), HandleVoiceResponseActivity.class);


                int notificationId = 001;

                PendingIntent viewPendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, replyIntent, 0);

                // Create the reply action and add the remote input
                NotificationCompat.Action action =
                        new NotificationCompat.Action.Builder(R.drawable.ic_launcher_1b, getString(R.string.voice_notification_label), viewPendingIntent).addRemoteInput(remoteInput).build();


                Notification notification =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_launcher_1b)
                                .setContentTitle("Your Dryer Is Summoning You")
                                .setContentText("Your Laundry Is Done, Stop Drinking Beer and Get It.")
                                .extend(new NotificationCompat.WearableExtender().addAction(action))
                                .build();

                // Get an instance of the NotificationManager service
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(getApplicationContext());
                // Build the notification and issues it with notification manager.
                notificationManager.notify(notificationId, notification);
            }
        });


        mLeaveButton = (Button)findViewById(R.id.useLeave);
        mLeaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_LEAVE_ID);
            }
        });

        //mChannelName = (TextView)findViewById(R.id.useChannelName);
        //mChannelStatus = (TextView)findViewById(R.id.useChannelStatus);

        /*
         * Keep a pointer to the Android Appliation class around.  We use this
         * as the Model for our MVC-based application.    Whenever we are started
         * we need to "check in" with the application so it can ensure that our
         * required services are running.
         */
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.checkin();

        /*
         * Call down into the model to get its current state.  Since the model
         * outlives its Activities, this may actually be a lot of state and not
         * just empty.
         */
        updateChannelState();
        updateHistory();

        /*
         * Now that we're all ready to go, we are ready to accept notifications
         * from other components.
         */
        mChatApplication.addObserver(this);

    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.deleteObserver(this);
        super.onDestroy();
    }

    public static final int DIALOG_JOIN_ID = 0;
    public static final int DIALOG_LEAVE_ID = 1;
    public static final int DIALOG_ALLJOYN_ERROR_ID = 2;

    protected Dialog onCreateDialog(int id) {
        Log.i(TAG, "onCreateDialog()");
        Dialog result = null;
        switch(id) {
        case DIALOG_JOIN_ID:
            {
                DialogBuilder builder = new DialogBuilder();
                result = builder.createUseJoinDialog(this, mChatApplication);
            }
            break;
        case DIALOG_LEAVE_ID:
            {
                DialogBuilder builder = new DialogBuilder();
                result = builder.createUseLeaveDialog(this, mChatApplication);
            }
            break;
        case DIALOG_ALLJOYN_ERROR_ID:
            {
                DialogBuilder builder = new DialogBuilder();
                result = builder.createAllJoynErrorDialog(this, mChatApplication);
            }
            break;
        }
        return result;
    }

    public synchronized void update(Observable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;

        if (qualifier.equals(ChatApplication.APPLICATION_QUIT_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
            mHandler.sendMessage(message);
        }

        if (qualifier.equals(ChatApplication.HISTORY_CHANGED_EVENT)) {

            Message message = mHandler.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }

        if (qualifier.equals(ChatApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }

        if (qualifier.equals(ChatApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
    }

    private void updateHistory() {
        Log.i(TAG, "updateHistory()");
        mHistoryList.clear();
        List<String> messages = mChatApplication.getHistory();
        for (String message : messages) {
            //JDD - putting the notifications here because it is ugly

            //JDD - debug
            String testMatchString = "end of cycle";
            Pattern pattern = Pattern.compile(testMatchString);
            Matcher match = pattern.matcher(message);
            int count = 0;
            Log.i("TEST", "About to Start checking");
            while(match.find())
            {
                count++;
                Log.i("TEST", "Match Number" + new Integer(count).toString());

                String replyLabel = getString(R.string.voice_notification_label);
                RemoteInput remoteInput = new RemoteInput.Builder(getResources().getString(R.string.EXTRA_VOICE_REPLY))
                        .setLabel(replyLabel)
                        .build();

                //JDD - create a new intent
                //Intent replyIntent = new Intent(this, DialogBuilder.class);
                Intent replyIntent = new Intent(getApplicationContext(), HandleVoiceResponseActivity.class);


                int notificationId = 001;

                PendingIntent viewPendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, replyIntent, 0);

                // Create the reply action and add the remote input
                NotificationCompat.Action action =
                        new NotificationCompat.Action.Builder(R.drawable.ic_launcher_1b, getString(R.string.voice_notification_label), viewPendingIntent).addRemoteInput(remoteInput).build();


                Notification notification =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_launcher_1b)
                                .setContentTitle("Your Dryer Is Summoning You")
                                .setContentText("Your Laundry Is Done, Stop Drinking Beer and Get It, Slob.")
                                .extend(new NotificationCompat.WearableExtender().addAction(action))
                                .build();

                // Get an instance of the NotificationManager service
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(getApplicationContext());
                // Build the notification and issues it with notification manager.
                notificationManager.notify(notificationId, notification);
                break;
            }


            mHistoryList.add(message);
        }
        mHistoryList.notifyDataSetChanged();
    }

    private void updateChannelState() {
        Log.i(TAG, "updateHistory()");
        AllJoynService.UseChannelState channelState = mChatApplication.useGetChannelState();
        String name = mChatApplication.useGetChannelName();
        if (name == null) {
            name = "Not set";
        }
        //mChannelName.setText(name);

        switch (channelState) {
        case IDLE:
            //mChannelStatus.setText("Idle");
            mJoinButton.setEnabled(true);
            mLeaveButton.setEnabled(false);
            break;
        case JOINED:
            //mChannelStatus.setText("Joined");
            mJoinButton.setEnabled(false);
            mLeaveButton.setEnabled(true);
            break;
        }
    }

    /**
     * An AllJoyn error has happened.  Since this activity pops up first we
     * handle the general errors.  We also handle our own errors.
     */
    private void alljoynError() {
        if (mChatApplication.getErrorModule() == ChatApplication.Module.GENERAL ||
            mChatApplication.getErrorModule() == ChatApplication.Module.USE) {
            showDialog(DIALOG_ALLJOYN_ERROR_ID);
        }
    }

    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
    private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 2;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_APPLICATION_QUIT_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
                    finish();
                }
                break;
            case HANDLE_HISTORY_CHANGED_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");
                    updateHistory();
                    break;
                }
            case HANDLE_CHANNEL_STATE_CHANGED_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_CHANNEL_STATE_CHANGED_EVENT");
                    updateChannelState();
                    break;
                }
            case HANDLE_ALLJOYN_ERROR_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
                    alljoynError();
                    break;
                }
            default:
                break;
            }
        }
    };

    private ChatApplication mChatApplication = null;

    private ArrayAdapter<String> mHistoryList;

    private Button mJoinButton;
    private Button mLeaveButton;
    private Button mStartOven;
    private Button mToggleLight;
    private Button mTestNotification;

    //private TextView mChannelName;

    //private TextView mChannelStatus;
}
