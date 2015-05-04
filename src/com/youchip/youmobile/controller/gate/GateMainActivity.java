package com.youchip.youmobile.controller.gate;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.AbstractAppControlActivity;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.model.gate.AreaConfig;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_KEY_A;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_GATE_CONFIG;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_MODE_NAME;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_USER_ID;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GateMainActivity extends AbstractAppControlActivity {
    
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat quickDate= new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");

    private List<AreaConfig> gateConfig;
    private String keyA;
    private String userID;
    
    private static final String LOG_TAG = GateMainActivity.class.getName();
    
    private final OnItemClickListener onItemClick = new OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String areaName = ((TextView)view).getText().toString();
            AreaConfig activeSettings = null;
            
            for (AreaConfig cfg:gateConfig){
                if (cfg.getAreaTitle().equals(areaName)){
                    activeSettings = cfg;
                }
            }
            
            if (activeSettings != null) {
                Log.d(LOG_TAG,"Loading Activity 'GateAccessActivity'");
                Intent intent = new Intent(GateMainActivity.this, GateAccessActivity.class);
                intent.putExtra(INTENT_EXTRA_GATE_CONFIG, (Serializable) activeSettings);
                intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, keyA);
                intent.putExtra(INTENT_EXTRA_USER_ID, GateMainActivity.this.userID);
                intent.putExtra(INTENT_EXTRA_MODE_NAME, GateMainActivity.this.getTitle() + " - " + activeSettings.getAreaTitle());
                startActivity(intent);
            } else {
                Log.w(LOG_TAG,"Loading Area config failed!");
            }
        }
    };
    
//    private OnItemLongClickListener onItemLongClick = new OnItemLongClickListener(){
//
//        @Override
//        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//            
//            String roles = GateMainActivity.this.getString(R.string.hint_gate_permitted_visitor_roles) +":\n";
//            String areaName = ((TextView)view).getText().toString();
//            for (AreaConfig cfg:gateConfig){
//                if (cfg.getAreaTitle().equals(areaName)){
//                    roles += "\n" + cfg.getRole() + "\n("+ quickDate.format(cfg.getValidTimeStart()) + " - " + quickDate.format(cfg.getValidTimeStop()) +")\n";
//                }
//            }
//            Toast.makeText(GateMainActivity.this, roles, Toast.LENGTH_LONG).show();
//            return true;
//        }
//        
//    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_main);
        
        Intent intent = getIntent();
        this.keyA   = intent.getStringExtra(INTENT_EXTRA_CHIP_KEY_A);
        this.userID = intent.getStringExtra(INTENT_EXTRA_USER_ID);
    }
 
    
    @Override
    public void onStart() {
        super.onStart();
        
        gateConfig = ConfigAccess.getAreaConfig(this);
        refreshListView();
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.reset_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
    
    private void refreshListView(){

        List<String> viewList = new ArrayList<>();
        if (gateConfig != null){
            for (AreaConfig element:gateConfig){
                if (!viewList.contains(element.getAreaTitle())){
                    viewList.add(element.getAreaTitle());
                }
            }
        }

        ListView listView = (ListView) findViewById(R.id.list_view_gate_config_list);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, viewList);
        listView.setOnItemClickListener(onItemClick);
//        listView.setOnItemLongClickListeer(onItemLongClick);
        listView.setAdapter(listAdapter);
    }

}
