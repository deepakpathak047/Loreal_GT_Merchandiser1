package com.cpm.dailyentry;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cpm.Constants.CommonString1;
import com.cpm.capitalfoods.R;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.keyboard.BasicOnKeyboardActionListener;
import com.cpm.keyboard.CustomKeyboardView;
import com.cpm.xmlGetterSetter.StockNewGetterSetter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class StockAvailability2Activity extends AppCompatActivity implements View.OnClickListener {

    String _pathforcheck, str;
    String img1 = "";
    static int grp_position = -1;
    static int currentapiVersion = 1;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    TextView tvheader;
    List<Integer> checkHeaderArray = new ArrayList<Integer>();
    List<Integer> checkChildArray = new ArrayList<Integer>();
    boolean checkflag=true;
    int saveBtnFlag = 0;
    int arrayEditText[] = {R.id.etAs_Per_Mccain, R.id.etactual_listed, R.id.etOpening_Stock_Cold_Room, R.id.etOpening_Stock_Mccain_Df, R.id.etTotal_Facing_McCain_DF, R.id.etOpening_Stock_Store_DF, R.id.etTotal_Facing_Store_DF, R.id.etmaterial_wellness_thawed_quantity};
    List<StockNewGetterSetter> listDataHeader;
    HashMap<StockNewGetterSetter, List<StockNewGetterSetter>> listDataChild;

    private SharedPreferences preferences;
    String store_cd;

    ArrayList<StockNewGetterSetter> brandData;
    ArrayList<StockNewGetterSetter> skuData;
    GSKDatabase db;

    EditText et = null;

    String arrayData[] = new String[arrayEditText.length + 1];

    String sku_cd;

    CustomKeyboardView mKeyboardView;
    Keyboard mKeyboard;

    String visit_date, username, intime;

    private ArrayList<StockNewGetterSetter> stockData = new ArrayList<StockNewGetterSetter>();
    boolean dataExists = false;

    boolean openmccaindfFlag = false;

    String Error_Message;
    FloatingActionButton fab;
    boolean ischangedflag = false;
    Snackbar snackbar;
    String a1, STATE_CD, STORE_TYPE_CD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_availability2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Stock Availability");
        fab = (FloatingActionButton) findViewById(R.id.fab);
        currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // get the list view
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        tvheader = (TextView) findViewById(R.id.txt_idealFor);
        mKeyboard = new Keyboard(this, R.xml.keyboard);

        str = CommonString1.FILE_PATH;

        mKeyboardView = (CustomKeyboardView) findViewById(R.id.keyboard_view);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView
                .setOnKeyboardActionListener(new BasicOnKeyboardActionListener(
                        this));

        db = new GSKDatabase(getApplicationContext());
        db.open();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        store_cd = preferences.getString(CommonString1.KEY_STORE_CD, null);

        visit_date = preferences.getString(CommonString1.KEY_DATE, null);
        username = preferences.getString(CommonString1.KEY_USERNAME, null);
        intime = preferences.getString(CommonString1.KEY_STORE_IN_TIME, "");
        STORE_TYPE_CD = preferences.getString(CommonString1.KEY_STORE_TYPE_CD, "");
        STATE_CD = preferences.getString(CommonString1.KEY_STATE_CD, "");

        // preparing list data
        prepareListData();

        openmccaindfFlag = preferences.getBoolean("opnestkmccaindf", false);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        fab.setOnClickListener(this);

        expListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }

            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
                expListView.invalidateViews();
            }

        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition).getBrand()
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition).getSku(), Toast.LENGTH_LONG)
                        .show();


                findViewById(R.id.lvExp).setVisibility(View.INVISIBLE);
                findViewById(R.id.entry_data).setVisibility(View.VISIBLE);
                tvheader.setText(listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition).getSku());
                sku_cd = listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition).getSku_cd();

                saveBtnFlag = 1;

                return false;
            }
        });


    }

    private void prepareListData() {
        listDataHeader = new ArrayList<StockNewGetterSetter>();
        listDataChild = new HashMap<StockNewGetterSetter, List<StockNewGetterSetter>>();

        brandData = db.getStockAvailabilityData(STATE_CD, STORE_TYPE_CD);
        if (!(brandData.size() > 0)) {

            //brandData = db.getHeaderStockImageData(store_cd, visit_date);
            brandData = db.getHeaderStockImageData(STATE_CD, STORE_TYPE_CD);
        }


        if (brandData.size() > 0) {

            // Adding child data

            for (int i = 0; i < brandData.size(); i++) {
                listDataHeader.add(brandData.get(i));

                skuData = db.getOpeningStockDataFromDatabase(store_cd, brandData.get(i).getBrand_cd());
                if (!(skuData.size() > 0) || (skuData.get(0).getOpenning_total_stock() == null) ||
                        (skuData.get(0).getOpening_facing().equals("")) || (skuData.get(0).getStock_under45days().equals(""))) {
                    skuData = db.getStockSkuData(brandData.get(i).getBrand_cd(), STATE_CD, STORE_TYPE_CD);
                } else {
                    fab.setImageResource(R.drawable.upload_data_ico);
                }

                List<StockNewGetterSetter> skulist = new ArrayList<StockNewGetterSetter>();
                for (int j = 0; j < skuData.size(); j++) {
                    skulist.add(skuData.get(j));
                }

                listDataChild.put(listDataHeader.get(i), skulist); // Header, Child data
            }

        }


    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.fab) {

            expListView.clearFocus();

            expListView.invalidateViews();

            if (validated()) {

                if (snackbar == null || !snackbar.isShown()) {

                    if (true) {


                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Are you sure you want to save")
                                .setCancelable(false)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int id) {

                                                db.open();
                                                getMid();

                                                dataExists = db.checkStock(store_cd);
                                                if (dataExists) {

                                                    db.UpdateOpeningStocklistData2(store_cd, listDataChild, listDataHeader);
                                                } else {
                                                    db.InsertOpeningStocklistData2(store_cd, STATE_CD, STORE_TYPE_CD, listDataChild, listDataHeader);
                                                }

                                                Toast.makeText(getApplicationContext(),
                                                        "Data has been saved", Toast.LENGTH_LONG).show();

                                                finish();

                                                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                                            }
                                        })
                                .setNegativeButton("No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();

                        alert.show();
                    }
                }

            }
        }
    }

    public Boolean  validated()
    {
        boolean isgood=true;
        checkHeaderArray.clear();
        checkChildArray.clear();
        checkflag=true;

        int lineno=0,itemno = 0;
        for(int i=0;i<expListView.getExpandableListAdapter().getGroupCount();i++) {

            for(int j=0;j<listDataChild.get(listDataHeader.get(i)).size();j++)
            {
                if(listDataChild
                        .get(listDataHeader.get(i))
                        .get(j).getSkuChecked()==-1)
                {
                    isgood=false;
                    lineno=i;
                    itemno = j;
                    if(!checkHeaderArray.contains(i)){
                        checkHeaderArray.add(i);
                    }
                    if(!checkChildArray.contains(j)){
                        checkChildArray.add(j);
                    }
                    checkflag=false;

                    break;
                }

            }
            if(!isgood)
            {
               // Toast.makeText(getApplicationContext(), "Please fill the data at line no."+String.valueOf(lineno+1)+"and item no."+String.valueOf(itemno+1), Toast.LENGTH_LONG).show();
                break;
            }
        }

        expListView.invalidate();
        listAdapter.notifyDataSetChanged();

        return  isgood;
    }


    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<StockNewGetterSetter> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<StockNewGetterSetter, List<StockNewGetterSetter>> _listDataChild;

        public ExpandableListAdapter(Context context, List<StockNewGetterSetter> listDataHeader,
                                     HashMap<StockNewGetterSetter, List<StockNewGetterSetter>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;

        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @SuppressLint("NewApi")
        @Override
        public View getChildView(final int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final StockNewGetterSetter childText = (StockNewGetterSetter) getChild(groupPosition, childPosition);

            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item_openingstk2, null);

                holder = new ViewHolder();

                holder.cardView = (CardView) convertView.findViewById(R.id.card_view);
                holder.stockSpinner = (Spinner) convertView.findViewById(R.id.Opening_Stock_total_spinner);
                holder.item_ll = (LinearLayout) convertView.findViewById(R.id.item_ll);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//----------------------------------

            holder.stockSpinner.setId(childPosition);

            _listDataChild
                    .get(listDataHeader.get(groupPosition))
                    .get(childPosition).setSku_cd(childText.getSku_cd());
            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.lblListItem);
            txtListChild.setText(childText.getSku());

            holder.stockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position!=0)
                    {
                        int isChecked=-1;

                        switch(position) {
                            case 1:
                                isChecked = 1;
                                break;
                            case 2:
                                isChecked = 0;
                                break;
                        }

                        _listDataChild
                                .get(listDataHeader.get(groupPosition))
                                .get(childPosition).setSkuChecked(isChecked);
                    }
                    else
                    {
                        _listDataChild
                                .get(listDataHeader.get(groupPosition))
                                .get(childPosition).setSkuChecked(-1);
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if( listDataChild
                    .get(listDataHeader.get(groupPosition))
                    .get(childPosition).getSkuChecked()==-1)
            {
                holder.stockSpinner.setSelection(0);
            }
            else if(listDataChild
                    .get(listDataHeader.get(groupPosition))
                    .get(childPosition).getSkuChecked()== 1)
            {
                holder.stockSpinner.setSelection(1);
            }
            else
            {
                holder.stockSpinner.setSelection(2);
            }


            if(!checkflag) {
                if( listDataChild
                        .get(listDataHeader.get(groupPosition))
                        .get(childPosition).getSkuChecked()==-1)
                {
                    holder.item_ll.setBackgroundColor(getResources().getColor(R.color.red));
                }
                else
                {
                    holder.item_ll.setBackgroundColor(getResources().getColor(R.color.white));
                }
//                if(checkChildArray.contains(childPosition) && checkHeaderArray.contains(groupPosition))
//                {
//                    holder.item_ll.setBackgroundColor(getResources().getColor(R.color.red));
//                }
//                else {
//                    holder.item_ll.setBackgroundColor(getResources().getColor(R.color.white));
//                }
            }

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final StockNewGetterSetter headerTitle = (StockNewGetterSetter) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group_opening, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblListHeader);

            lblListHeader.setText(headerTitle.getBrand());

            if(!checkflag){
                if(checkHeaderArray.contains(groupPosition)){
                    lblListHeader.setBackgroundColor(getResources().getColor(R.color.red));
                }
                else{
                    lblListHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }


            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    public class ViewHolder {
        Spinner stockSpinner;
        CardView cardView;
        TextView textview;
        LinearLayout item_ll;
    }

    /***
     * Display the screen keyboard with an animated slide from bottom
     */

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        if (mKeyboardView.getVisibility() == View.VISIBLE) {
            mKeyboardView.setVisibility(View.INVISIBLE);
            /*mKeyboardView.requestFocusFromTouch();*/
        } else {

            if (ischangedflag) {

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        this);
                builder.setMessage(CommonString1.ONBACK_ALERT_MESSAGE)
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {

                                        finish();

                                        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

            } else {
                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
            }

        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mKeyboardView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setVisibility(View.INVISIBLE);
    }

    public long checkMid() {
        return db.CheckMid(visit_date, store_cd);
    }


    public long getMid() {

        long mid = 0;

        mid = checkMid();

        if (mid == 0) {
            CoverageBean cdata = new CoverageBean();
            cdata.setStoreId(store_cd);
            cdata.setVisitDate(visit_date);
            cdata.setUserId(username);
            cdata.setInTime(intime);
            cdata.setOutTime(getCurrentTime());
            cdata.setReason("");
            cdata.setReasonid("0");
            cdata.setSub_reasonId("0");
            cdata.setLatitude("0.0");
            cdata.setLongitude("0.0");
            cdata.setStatus(CommonString1.KEY_CHECK_IN);
            mid = db.InsertCoverageData(cdata);

        }

        return mid;
    }

    public String getCurrentTime() {

        Calendar m_cal = Calendar.getInstance();

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String cdate = formatter.format(m_cal.getTime());
        return cdate;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {

            // NavUtils.navigateUpFromSameTask(this);
            finish();

            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);

        }

        return super.onOptionsItemSelected(item);
    }


}




