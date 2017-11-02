package com.score.cbook.interfaces;

import com.score.cbook.pojo.Contact;

import java.util.ArrayList;

/**
 * Created by eranga on 12/10/16.
 */

public interface IContactReaderListener {
    void onPostRead(ArrayList<Contact> contactList);
}
