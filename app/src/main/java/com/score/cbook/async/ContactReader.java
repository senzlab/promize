package com.score.cbook.async;

import android.content.Context;
import android.os.AsyncTask;

import com.score.cbook.interfaces.IContactReaderListener;
import com.score.cbook.pojo.Contact;
import com.score.cbook.utils.PhoneBookUtil;

import java.util.ArrayList;

/**
 * Created by eranga on 12/10/16.
 */
public class ContactReader extends AsyncTask<String, String, ArrayList<Contact>> {

    private Context context;
    private IContactReaderListener listener;

    public ContactReader(Context context, IContactReaderListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected ArrayList<Contact> doInBackground(String... params) {
        return PhoneBookUtil.getContactList(context);
    }

    @Override
    protected void onPostExecute(ArrayList<Contact> contactList) {
        super.onPostExecute(contactList);

        listener.onPostRead(contactList);
    }

}
