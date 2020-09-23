//This is not a proper stack class but rather a custom one created to ease undo/redo processes
package com.snehashis.litext;

import android.util.Log;

import java.util.ArrayList;

public class Stack {
    public ArrayList<String> data;
    public int top;
    boolean isEmpty;
    public Stack() {
        this.data = new ArrayList<>();
        top = -1;
        isEmpty = true;
    }

    public void push(String s){
        //if(!s.equals("")) {
            data.add(s);
            top++;
            isEmpty = false;
            Log.d("Received","" + s+"\t Top="+top);
        //}
    }

    public String pop(){
        String popped = null;
        if(top >= 0){
            popped = data.remove(top);
            top--;
            isEmpty = (top == -1);
        }
        else Log.d("Stack","Underflow");
        Log.d("Pop","" + popped+"\t Top="+top+"\tEmpty="+isEmpty);
        return popped;
    }
}
