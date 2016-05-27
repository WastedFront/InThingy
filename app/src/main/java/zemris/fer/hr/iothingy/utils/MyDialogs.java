package zemris.fer.hr.iothingy.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import zemris.fer.hr.iothingy.R;
import zemris.fer.hr.iothingy.communication.CommunicationTask;

/**
 * Class provides methods which show different dialogs to user and interact with him.
 */
public class MyDialogs {

    /**
     * Method for creating dialog which will display some destination addresses.
     *
     * @param context
     *         context of some activity
     */
    public static void chooseDestination(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = context.getResources().getString(R.string.text_select_destination);
        builder.setTitle(Html.fromHtml("<font color='#FF7F27'>" + title + "</font>"));
        ListView modeList = new ListView(context);
        final String[] addresses = StoringUtils.getDestinationAddresses(context);
        ArrayAdapter modeAdapter = new ArrayAdapter<>(context, R.layout.custom_checked_text_view,
                android.R.id.text1, addresses);
        modeList.setAdapter(modeAdapter);
        builder.setView(modeList);
        final Dialog dialog = builder.create();
        dialog.show();
        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((EditText) ((Activity) context).findViewById(R.id.etDestination)).setText(addresses[position]);
                dialog.dismiss();
            }
        });
    }

    /**
     * Method for showing received messages. It also provides respond to those messages just by user clicking on them.
     * It makes {@code AlertDialog} with list view in which there are messages.
     * On {@code OnItemClickListener} user can automatically reply to message and on {@code OnItemLongClickListener} user
     * can see full message format.
     *
     * @param context
     *         context of some activity
     */
    public static void showReceivedMessages(final Context context) throws Exception {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = context.getResources().getString(R.string.text_recevied_messages);
        builder.setTitle(Html.fromHtml("<font color='#FF7F27'>" + title + "</font>"));
        ListView listView = new ListView(context);
        final List<ReceivedServerMessage> msgs = StoringUtils.getReceivedMessages(context);
        String[] messages = new String[msgs.size()];
        for (int i = 0, len = msgs.size(); i < len; ++i) {
            messages[i] = "" + (i + 1) + ". " + msgs.get(i).getSrcID() + ": " + msgs.get(i).returnMsgDataInfo();
        }
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
                android.R.id.text1, messages);
        listView.setAdapter(modeAdapter);
        builder.setView(listView);
        final Dialog dialog = builder.create();
        dialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new CommunicationTask(context, msgs.get(position).responseMessage(context.getApplicationContext()), true);
                StoringUtils.removeReceivedMessage(context.getApplicationContext(), msgs.get(0));
                dialog.dismiss();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                createMessageLongClick(msgs.get(position), context);
                return false;
            }
        });
    }

    /**
     * Method for handling long click on received messages.
     *
     * @param message
     *         stored received message.
     * @param context
     *         context of some activity
     */
    private static void createMessageLongClick(ReceivedServerMessage message, Context context) {
        TextView tvText = new TextView(context);
        tvText.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        tvText.setText(message.messageSummary());
        String title = context.getResources().getString(R.string.text_message_info_title);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(Html.fromHtml("<font color='#FF7F27'>" + title + "</font>"))
                .setView(tvText)
                .setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Method for creating toast with red text.
     *
     * @param context
     *         context of activity
     * @param text
     *         text which will be shown
     */
    public static void makeRedTextToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        tv.setTextColor(Color.RED);
        toast.show();
    }

    /**
     * Method for creating toast with green text.
     *
     * @param context
     *         context of activity
     * @param text
     *         text which will be shown
     */
    public static void makeGreenTextToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
        tv.setTextColor(Color.GREEN);
        toast.show();
    }
}
