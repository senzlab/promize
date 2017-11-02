package com.score.cbook.interfaces;

import com.score.cbook.pojo.Contact;

import java.util.ArrayList;

public interface IContactReaderListener {
    void onPostRead(ArrayList<Contact> contactList);
}
