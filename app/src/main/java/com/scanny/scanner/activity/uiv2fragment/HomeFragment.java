package com.scanny.scanner.activity.uiv2fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.scanny.scanner.R;
import com.scanny.scanner.activity.BaseActivity;
import com.scanny.scanner.activity.MainActivity;
import com.scanny.scanner.activity.uiv2.UIV2MainActivity;
import com.scanny.scanner.adapter.AllGroupAdapter;
import com.scanny.scanner.adapter.DrawerItemAdapter;
import com.scanny.scanner.databinding.FragmentHomeBinding;
import com.scanny.scanner.db.DBHelper;
import com.scanny.scanner.main_utils.Constant;
import com.scanny.scanner.models.DBModel;
import com.scanny.scanner.models.DrawerModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;


public class HomeFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    private FragmentHomeBinding binding;
    private static final String TAG = "MainActivity";
    public static HomeFragment mainActivity;
    public static UIV2MainActivity activity;
    public String current_group;
    public DBHelper dbHelper;
    public ArrayList<DBModel> groupList;
    public String[] tabList = {"All Docs", "Business Card", "ID Card", "Academic Docs", "Personal Tag"};
    public TextView tv_empty;
    private AllGroupAdapter allGroupAdapter;
    protected String current_mode;
    protected DrawerItemAdapter drawerItemAdapter;
    public SharedPreferences preferences;
    protected SharedPreferences.Editor editor;
    protected LinearLayoutManager layoutManager;
    protected String selected_sorting;
    protected int selected_sorting_pos;
    ArrayList<DBModel> modelArrayList = new ArrayList<>();
    String selectedFolderName = "";
    private ArrayList<DrawerModel> drawerList = new ArrayList<>();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString( ARG_PARAM1, param1 );
        args.putString( ARG_PARAM2, param2 );
        fragment.setArguments( args );
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (getArguments() != null) {
            mParam1 = getArguments().getString( ARG_PARAM1 );
            mParam2 = getArguments().getString( ARG_PARAM2 );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate( inflater, container, false );
        groupList = new ArrayList<>();
        dbHelper = new DBHelper( requireContext() );
        setTab();
        onClickListen();
        return binding.getRoot();
    }

    private void onClickListen() {
        binding.ivFolder1.setOnClickListener( view -> {
            openNewFolderDialog( "" );
        } );
        binding.ivDrawer1.setOnClickListener( view -> {

        } );
        binding.ivMore1.setOnClickListener( view -> {
            PopupMenu popupMenu = new PopupMenu( requireContext(), view );
            popupMenu.setOnMenuItemClickListener( this );
            popupMenu.inflate( R.menu.group_more );
            try {
                Field declaredField = PopupMenu.class.getDeclaredField( "mPopup" );
                declaredField.setAccessible( true );
                Object obj = declaredField.get( popupMenu );
                obj.getClass().getDeclaredMethod( "setForceShowIcon", Boolean.TYPE ).invoke( obj, true );
                popupMenu.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } );
        binding.ivSearch1.setOnClickListener( view -> {
            binding.ivSearch1.setVisibility( View.GONE );
            binding.rlSearchBar.setVisibility( View.VISIBLE );
            showSoftKeyboard( binding.etSearch );
        } );
        binding.ivClearTxt.setOnClickListener( view -> {
            binding.etSearch.setText( "" );
            binding.ivClearTxt.setVisibility( View.GONE );
        } );
        binding.ivCloseSearch.setOnClickListener( view -> {
            binding.ivSearch1.setVisibility( View.VISIBLE );
            binding.rlSearchBar.setVisibility( View.GONE );
            binding.etSearch.setText( "" );
            activity.hideSoftKeyboard( binding.etSearch );
        } );

    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            Context context = requireContext(); // Lấy đối tượng Context của Fragment

            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService( Context.INPUT_METHOD_SERVICE );

            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput( view, InputMethodManager.SHOW_IMPLICIT );
            }
        }
    }

    public void filter(String str) {
        ArrayList arrayList = new ArrayList();
        Iterator<DBModel> it = groupList.iterator();
        while (it.hasNext()) {
            DBModel next = it.next();
            if (next.getGroup_name().toLowerCase().contains( str.toLowerCase() )) {
                arrayList.add( next );
            }
        }
        allGroupAdapter.filterList( arrayList );
    }

    private void setTab() {
        for (String text : tabList) {
            TabLayout tabLayout = binding.tagTabs1;
            tabLayout.addTab( tabLayout.newTab().setText( (CharSequence) text ) );
        }
        Constant.current_tag = "All Docs";
        binding.tagTabs1.addOnTabSelectedListener( (TabLayout.OnTabSelectedListener) new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Log.e(MainActivity.TAG, "onTabSelected: " + Constant.current_tag);
                Constant.current_tag = tabList[tab.getPosition()];
                new setAllGroupAdapter().execute( new String[0] );
            }
        } );
        binding.etSearch.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (i3 == 0) {
                    binding.ivClearTxt.setVisibility( View.INVISIBLE );
                } else if (i3 == 1) {
                    binding.ivClearTxt.setVisibility( View.VISIBLE );
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (groupList.size() > 0) {
                    filter( editable.toString() );
                }
            }
        } );
    }

    private void openNewFolderDialog(String groupName) {
        final Dialog dialog = new Dialog( requireContext() );
        dialog.requestWindowFeature( 1 );
        dialog.getWindow().setBackgroundDrawable( new ColorDrawable( android.graphics.Color.TRANSPARENT ) );

        dialog.setContentView( R.layout.create_folder_dialog );
        dialog.getWindow().setLayout( -1, -2 );
        dialog.setCanceledOnTouchOutside( false );
        dialog.setCancelable( false );
        EditText et_folder_name = (EditText) dialog.findViewById( R.id.et_folder_name );
        String folder_name = "ScanToPDF" + Constant.getDateTime( "ddMMHHmmss" );
        et_folder_name.setText( folder_name );

        ((TextView) dialog.findViewById( R.id.tv_create )).setOnClickListener( view -> {
            String finalFolderName = et_folder_name.getText().toString().trim();
            if (!finalFolderName.isEmpty()) {
                String group_date = Constant.getDateTime( "yyyyMMdd  hh:mm a" );
                if (groupName.isEmpty()) {        // for create new folder
                    dbHelper.createDocTable( finalFolderName );
                    dbHelper.addGroup( new DBModel( finalFolderName, group_date, "", Constant.current_tag ) );
                } else {
                    dbHelper.createDocTable( finalFolderName );
                    dbHelper.addGroup( new DBModel( finalFolderName, group_date, "", Constant.current_tag ) );
                    // for move new folder
                    boolean isSuccess = false;
                    ArrayList<DBModel> allFileList = dbHelper.getGroupDocs( groupName );
                    for (int i = 0; i < allFileList.size(); i++) {
                        DBModel newDbModel = allFileList.get( i );
                        long isMove = dbHelper.moveGroupDoc( finalFolderName, newDbModel.getGroup_doc_img(), newDbModel.getGroup_doc_name(), "Insert text here..." );
                        if (isMove <= 0) {
                            isSuccess = false;
                            break;
                        } else {
                            isSuccess = true;
                        }
                    }
                    if (isSuccess) {
                        Toast.makeText( getContext(), "Move successfully", Toast.LENGTH_SHORT ).show();
                        dbHelper.deleteGroup( groupName );
                    }
                }
                new setAllGroupAdapter().execute( new String[0] );
                dialog.dismiss();
            } else {
                Toast.makeText( getContext(), "Folder name is required", Toast.LENGTH_SHORT ).show();
            }

        } );
        ((ImageView) dialog.findViewById( R.id.iv_close )).setOnClickListener( view -> dialog.dismiss() );
        dialog.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.grid_view:
                editor = preferences.edit();
                editor.putString( "ViewMode", "Grid" );
                editor.apply();
                new setAllGroupAdapter().execute( new String[0] );
                break;
            case R.id.import_from_gallery:
                ActivityCompat.requestPermissions( requireActivity(), new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"}, 1 );
                break;
            case R.id.list_view:
                editor = preferences.edit();
                editor.putString( "ViewMode", "List" );
                editor.apply();
                new setAllGroupAdapter().execute( new String[0] );
                break;
            case R.id.share_all:
                new shareAllGroup().execute( new String[0] );
                break;
            case R.id.sort_by:
                AlertDialog.Builder builder = new AlertDialog.Builder( requireContext() );
                builder.setTitle( (CharSequence) "Sort By" );
                String[] strArr = {"Ascending date", "Descending date", "Ascending name", "Descending name"};
                if (selected_sorting.equals( Constant.ascending_date )) {
                    selected_sorting_pos = 0;
                } else if (selected_sorting.equals( Constant.descending_date )) {
                    selected_sorting_pos = 1;
                } else if (selected_sorting.equals( Constant.ascending_name )) {
                    selected_sorting_pos = 2;
                } else if (selected_sorting.equals( Constant.descending_name )) {
                    selected_sorting_pos = 3;
                }
                builder.setSingleChoiceItems( (CharSequence[]) strArr, selected_sorting_pos, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            mainActivity.editor = mainActivity.preferences.edit();
                            editor.putString( "sortBy", Constant.ascending_date );
                            editor.apply();
                            new setAllGroupAdapter().execute( new String[0] );
                            dialogInterface.dismiss();
                        } else if (i == 1) {
                            mainActivity.editor = mainActivity.preferences.edit();
                            editor.putString( "sortBy", Constant.descending_date );
                            editor.apply();
                            new setAllGroupAdapter().execute( new String[0] );
                            dialogInterface.dismiss();
                        } else if (i == 2) {
                            mainActivity.editor = mainActivity.preferences.edit();
                            editor.putString( "sortBy", Constant.ascending_name );
                            editor.apply();
                            new setAllGroupAdapter().execute( new String[0] );
                            dialogInterface.dismiss();
                        } else if (i == 3) {
                            mainActivity.editor = mainActivity.preferences.edit();
                            editor.putString( "sortBy", Constant.descending_name );
                            editor.apply();
                            new setAllGroupAdapter().execute( new String[0] );
                            dialogInterface.dismiss();
                        }
                    }
                } );
                builder.show();
                break;
        }
        return true;
    }

    public class setAllGroupAdapter extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        public setAllGroupAdapter() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog( requireContext() );
            this.progressDialog.setIndeterminate( true );
            this.progressDialog.setMessage( "Loading Data..." );
            this.progressDialog.setCancelable( false );
            this.progressDialog.setCanceledOnTouchOutside( false );
            this.progressDialog.show();
        }

        @Override
        public String doInBackground(String... strArr) {
            if (Constant.current_tag.equals( "All Docs" )) {
                groupList = dbHelper.getAllGroups();
                return null;
            } else if (Constant.current_tag.equals( "Business Card" )) {
                groupList = dbHelper.getGroupsByTag( "Business Card" );
                return null;
            } else if (Constant.current_tag.equals( "ID Card" )) {
                groupList = dbHelper.getGroupsByTag( "ID Card" );
                return null;
            } else if (Constant.current_tag.equals( "Academic Docs" )) {
                groupList = dbHelper.getGroupsByTag( "Academic Docs" );
                return null;
            } else if (Constant.current_tag.equals( "Personal Tag" )) {
                groupList = dbHelper.getGroupsByTag( "Personal Tag" );
                return null;
            } else {
                groupList = dbHelper.getAllGroups();
                return null;
            }
        }

        @Override
        public void onPostExecute(String str) {
            super.onPostExecute( str );
            if (groupList.size() > 0) {
                binding.rvGroup.setVisibility( View.VISIBLE );
                binding.lyEmpty1.setVisibility( View.GONE );

                mainActivity.selected_sorting = mainActivity.preferences.getString( "sortBy", Constant.descending_date );
                if (selected_sorting.equals( Constant.ascending_date )) {
                    Log.e( TAG, "onPostExecute: ascending_date" );
                } else if (selected_sorting.equals( Constant.descending_date )) {
                    Collections.reverse( groupList );
                } else if (selected_sorting.equals( Constant.ascending_name )) {
                    Collections.sort( groupList, new SortByName() );
                } else if (selected_sorting.equals( Constant.descending_name )) {
                    Collections.sort( groupList, new SortByName() );
                }

                mainActivity.current_mode = mainActivity.preferences.getString( "ViewMode", "List" );
                if (current_mode.equals( "Grid" )) {
                    mainActivity.layoutManager = new GridLayoutManager( requireActivity(), 2, RecyclerView.VERTICAL, false );
                } else {
                    mainActivity.layoutManager = new LinearLayoutManager( requireActivity(), RecyclerView.VERTICAL, false );
                }
                binding.rvGroup.setHasFixedSize( true );
                binding.rvGroup.setLayoutManager( layoutManager );
                mainActivity.allGroupAdapter = new AllGroupAdapter( requireActivity(), mainActivity.groupList, current_mode );
                binding.rvGroup.setAdapter( allGroupAdapter );
            } else {
                mainActivity.selected_sorting = mainActivity.preferences.getString( "sortBy", Constant.descending_date );
                binding.rvGroup.setVisibility( View.GONE );
                binding.lyEmpty1.setVisibility( View.VISIBLE );
                if (Constant.current_tag.equals( "All Docs" )) {
                    tv_empty.setText( getResources().getString( R.string.all_docs_empty ) );
                } else if (Constant.current_tag.equals( "Business Card" )) {
                    tv_empty.setText( getResources().getString( R.string.business_card_empty ) );
                } else if (Constant.current_tag.equals( "ID Card" )) {
                    tv_empty.setText( getResources().getString( R.string.id_card_empty ) );
                } else if (Constant.current_tag.equals( "Academic Docs" )) {
                    tv_empty.setText( getResources().getString( R.string.academic_docs_empty ) );
                } else if (Constant.current_tag.equals( "Personal Tag" )) {
                    tv_empty.setText( getResources().getString( R.string.personal_tag_empty ) );
                } else {
                    tv_empty.setText( getResources().getString( R.string.all_docs_empty ) );
                }
            }
            progressDialog.dismiss();
        }
    }

    class SortByName implements Comparator<DBModel> {
        SortByName() {
        }

        @Override
        public int compare(DBModel dBModel, DBModel dBModel2) {
            if (selected_sorting.equals( Constant.ascending_name )) {
                return new File( dBModel.group_name ).getName().compareToIgnoreCase( new File( dBModel2.group_name ).getName() );
            }
            if (selected_sorting.equals( Constant.descending_name )) {
                return new File( dBModel2.group_name ).getName().compareToIgnoreCase( new File( dBModel.group_name ).getName() );
            }
            return 0;
        }
    }

    private class shareAllGroup extends AsyncTask<String, Void, String> {
        ArrayList<Uri> allPDFList;
        ProgressDialog progressDialog;

        private shareAllGroup() {
            allPDFList = new ArrayList<>();
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog( requireContext() );
            progressDialog.setIndeterminate( true );
            progressDialog.setMessage( "Please Wait..." );
            progressDialog.setCancelable( false );
            progressDialog.setCanceledOnTouchOutside( false );
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... strArr) {
            Iterator it = groupList.iterator();
            while (it.hasNext()) {
                String group_name = ((DBModel) it.next()).getGroup_name();
                new ArrayList().clear();
                ArrayList<DBModel> groupDocs = dbHelper.getShareGroupDocs( group_name.replace( " ", "" ) );
                ArrayList arrayList = new ArrayList();
                Iterator<DBModel> it2 = groupDocs.iterator();
                while (it2.hasNext()) {
                    DBModel next = it2.next();
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        arrayList.add( BitmapFactory.decodeStream( new FileInputStream( next.getGroup_doc_img() ), (Rect) null, options ) );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (arrayList.size() > 0) {
                    BaseActivity baseActivity = (BaseActivity) requireActivity();
                    baseActivity.createPDFfromBitmap(group_name, arrayList, "temp");

                    // Thêm URI của PDF vào danh sách
                    allPDFList.add(baseActivity.getURIFromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + getResources().getString(R.string.app_name) + "/" + group_name + ".pdf",baseActivity));
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(String str) {
            super.onPostExecute( str );
            Intent intent = new Intent();
            intent.setAction( "android.intent.action.SEND_MULTIPLE" );
            intent.setType( "application/pdf" );
            intent.putExtra( "android.intent.extra.STREAM", allPDFList );
            intent.putExtra( "android.intent.extra.SUBJECT", "Share All" );
            intent.setFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
            Intent createChooser = Intent.createChooser( intent, (CharSequence) null );
            progressDialog.dismiss();
            startActivity( createChooser );
        }
    }


}