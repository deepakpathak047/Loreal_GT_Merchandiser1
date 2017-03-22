package com.cpm.dailyentry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cpm.Constants.CommonString1;
import com.cpm.capitalfoods.R;
import com.cpm.database.GSKDatabase;
import com.cpm.xmlGetterSetter.BrandGetterSetter;
import com.cpm.xmlGetterSetter.CategoryMasterGetterSetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ShareOfShelfActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    FloatingActionButton fab;
    ExpandableListView expandableListView;
    SharedPreferences preferences;
    ExpandableListAdapter expandableListAdapter;
    GSKDatabase db;
    Context context;
    String store_cd;
    ArrayList<CategoryMasterGetterSetter> categoryList;
    ArrayList<CategoryMasterGetterSetter> listDataHeader;
    HashMap<CategoryMasterGetterSetter, List<BrandGetterSetter>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_of_shelf);

        declaration();
        prepareListData();

    }


    void declaration() {
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Share of Shelf");
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        expandableListView = (ExpandableListView) findViewById(R.id.shareofshelf_expandable);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        store_cd = preferences.getString(CommonString1.KEY_STORE_CD, null);
        db = new GSKDatabase(context);
        db.open();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fab:
                if(validate())
            {
                db.insertShareShelfHeaderData(store_cd,listDataHeader,listDataChild);
            }
                break;
        }

    }


    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<CategoryMasterGetterSetter> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<CategoryMasterGetterSetter, List<BrandGetterSetter>> _listDataChild;

        public ExpandableListAdapter(Context context, List<CategoryMasterGetterSetter> listDataHeader, HashMap<CategoryMasterGetterSetter, List<BrandGetterSetter>> listChildData) {
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

            final BrandGetterSetter childText = (BrandGetterSetter) getChild(groupPosition, childPosition);
            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_child_expandable_shareofshelf, null);

                holder = new ViewHolder();

                holder.cardView = (CardView) convertView.findViewById(R.id.card_view);
                holder.item_ll = (LinearLayout) convertView.findViewById(R.id.item_ll);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//----------------------------------
            TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
            EditText edtListChild = (EditText) convertView.findViewById(R.id.quantity_edt);

            txtListChild.setText(childText.getBrand().get(0));

            _listDataChild
                    .get(listDataHeader.get(groupPosition))
                    .get(childPosition).setBrand(childText.getBrand().get(0));

            _listDataChild
                    .get(listDataHeader.get(groupPosition))
                    .get(childPosition).setBrand_cd(childText.getBrand_cd().get(0));

            edtListChild.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    EditText edt = (EditText)v;
                    String val = edt.getText().toString();
                    if(val.equalsIgnoreCase(""))
                    {
                        _listDataChild
                                .get(listDataHeader.get(groupPosition))
                                .get(childPosition).setQuantity("");
                    }
                    else
                    {
                        _listDataChild
                                .get(listDataHeader.get(groupPosition))
                                .get(childPosition).setQuantity(val);
                    }
                }
            });

            edtListChild.setText( _listDataChild
                    .get(listDataHeader.get(groupPosition))
                    .get(childPosition).getQuantity());


          /*  if (!checkflag) {
                if (listDataChild
                        .get(listDataHeader.get(groupPosition))
                        .get(childPosition).getSkuChecked() == -1) {
                    holder.item_ll.setBackgroundColor(getResources().getColor(R.color.red));
                } else {
                    holder.item_ll.setBackgroundColor(getResources().getColor(R.color.white));
                }

            }
*/
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
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_header_expandable_shareofshelf, null);
            }

            final CategoryMasterGetterSetter headerTitle = (CategoryMasterGetterSetter) getGroup(groupPosition);

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            EditText edt_quantity = (EditText) convertView
                    .findViewById(R.id.headerQty_edt);
            CardView cardView = (CardView) convertView
                    .findViewById(R.id.card_view);

            lblListHeader.setText(headerTitle.getCategory().get(0));

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (expandableListView.isGroupExpanded(groupPosition)) {
                        expandableListView.collapseGroup(groupPosition);
                    } else {
                        expandableListView.expandGroup(groupPosition);
                    }
                }
            });

            edt_quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText edt = (EditText)v;
                    String val =edt.getText().toString();
                    if(val.equalsIgnoreCase(""))
                    {
                        listDataHeader.get(groupPosition).setQuantity("");
                    }
                    else
                    {
                        listDataHeader.get(groupPosition).setQuantity(val);
                    }
                }
            });

            edt_quantity.setText(listDataHeader.get(groupPosition).getQuantity());
          /*  if (!checkflag) {
                if (checkHeaderArray.contains(groupPosition)) {
                    lblListHeader.setBackgroundColor(getResources().getColor(R.color.red));
                } else {
                    lblListHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }*/



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
        CardView cardView;
        TextView textview;
        LinearLayout item_ll;
    }

    public boolean validate()
    {
        boolean isvalidate = true;

        return isvalidate;
    }


    private void prepareListData() {
        listDataHeader = new ArrayList<CategoryMasterGetterSetter>();
        ArrayList<BrandGetterSetter> childata = new ArrayList<BrandGetterSetter>();
        listDataChild = new HashMap<CategoryMasterGetterSetter, List<BrandGetterSetter>>();
        categoryList = db.getCategoryMasterData();
        listDataHeader.clear();
        listDataChild.clear();

        if (categoryList.size() > 0) {
            // Adding child data
            for (int i = 0; i < categoryList.size(); i++) {
                listDataHeader.add(categoryList.get(i));
                childata = db.getChildDataForHeader(categoryList.get(i).getCategory_cd().get(0));
                if (childata.size() > 0) {
                    listDataChild.put(listDataHeader.get(i), childata); // Header, Child data
                }
            }


            if (listDataHeader.size() > 0) {
                expandableListAdapter = new ExpandableListAdapter(context, listDataHeader, listDataChild);
                expandableListView.setAdapter(expandableListAdapter);
            }

        }


    }
}
