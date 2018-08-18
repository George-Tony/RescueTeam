package com.webcrs.rescueteam;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    public static final String MyPREFERENCES = "rescueteam" ;
    public static final String Name = "nameKey";
    public static final String Phone = "phoneKey";
    public static final String Email = "emailKey";
    public static final String ID = "id";
    SharedPreferences.Editor editor;

    SharedPreferences sharedpreferences;



    public SessionManager(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }


    public void savedata(String name, String phoneNo, String emailID){
        editor.putString(Name, name);
        editor.putString(Phone, phoneNo);
        editor.putString(Email, emailID);
        editor.commit();
    }

    public void saveUserID(String id){
        editor.putString(ID, id);
        editor.commit();
    }

    public String getuserID (){
        return sharedpreferences.getString(ID,null);
    }

}
