package com.example.alin.lechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

public class GroupAddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);

        String[] str={"Option 1","Option 2","Option 3",
                "Option 4","Option 5","Option 6"};

        MultiAutoCompleteTextView mt=(MultiAutoCompleteTextView)
                findViewById(R.id.multiAutoCompleteTextViewGroups);

        mt.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        ArrayAdapter<String> adp=new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,str);
        mt.setInputType(InputType.TYPE_CLASS_TEXT);

        mt.setThreshold(1);
        mt.setAdapter(adp);

    }
}
