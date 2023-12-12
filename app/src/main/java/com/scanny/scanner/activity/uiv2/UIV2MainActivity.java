package com.scanny.scanner.activity.uiv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.scanny.scanner.R;
import com.scanny.scanner.activity.BaseActivity;
import com.scanny.scanner.activity.uiv2fragment.HomeFragment;
import com.scanny.scanner.activity.uiv2fragment.UIV2DocumentFragment;
import com.scanny.scanner.activity.uiv2fragment.UIV2PersonFragment;
import com.scanny.scanner.activity.uiv2fragment.UIV2qrCodeFragment;
import com.scanny.scanner.adapter.HomeViewPagerAdapter;
import com.scanny.scanner.databinding.ActivityUiv2MainBinding;

public class UIV2MainActivity extends BaseActivity {
    private ActivityUiv2MainBinding binding;
    private HomeViewPagerAdapter adapter;
    private HomeFragment homeFragment;
    private UIV2PersonFragment personFragment;
    private UIV2qrCodeFragment qrCodeFragment;
    private UIV2DocumentFragment documentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding=ActivityUiv2MainBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );

        homeFragment = new HomeFragment();
        personFragment = new UIV2PersonFragment();
        qrCodeFragment = new UIV2qrCodeFragment();
        documentFragment = new UIV2DocumentFragment();

        // Add Fragments to the ViewPager
        adapter = new HomeViewPagerAdapter( getSupportFragmentManager(), getLifecycle() );
        adapter.addFragment( homeFragment );
        adapter.addFragment( documentFragment );
        adapter.addFragment( qrCodeFragment );
        adapter.addFragment( personFragment );

        binding.viewPager2.setAdapter( adapter );
        binding.viewPager2.setOffscreenPageLimit( 4 );
        binding.viewPager2.setOnTouchListener( (v, event) -> true );


        // Set up TabLayout with ViewPager

        binding.viewPager2.registerOnPageChangeCallback( new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected( position );

                switch (position) {
                    case 0:
                        binding.bottomNavi.getMenu().findItem( R.id.bottom_home ).setChecked( true );
                        break;
                    case 1:
                        binding.bottomNavi.getMenu().findItem( R.id.bottom_file ).setChecked( true );
                        break;
                    case 2:
                        binding.bottomNavi.getMenu().findItem( R.id.bottom_qrCode ).setChecked( true );
                        break;
                    case 3:
                        binding.bottomNavi.getMenu().findItem( R.id.bottom_person ).setChecked( true );
                        break;

                }
            }
        } );

        binding.bottomNavi.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_home:
                        binding.viewPager2.setCurrentItem( 0, true );
                        break;
                    case R.id.bottom_file:
                        binding.viewPager2.setCurrentItem( 1, true );
                        break;
                    case R.id.bottom_qrCode:
                        binding.viewPager2.setCurrentItem( 2, true );
                        break;
                    case R.id.bottom_person:
                        binding.viewPager2.setCurrentItem( 3, true );
                        break;
                }
                return false;
            }
        } );
    }
    @Override
    public void onBackPressed() {
        // Kiểm tra xem có Fragment nào trong Back Stack không
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Nếu có, thì quay lại Fragment trước đó
            getSupportFragmentManager().popBackStack();
        } else {
            // Nếu không, kiểm tra xem có ở Fragment đầu tiên không
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("TAG_FIRST_FRAGMENT");
            if (fragment != null && fragment.isVisible()) {
                // Nếu đang ở Fragment đầu tiên, thoát ứng dụng
                finish();
            } else {
                // Nếu không ở Fragment đầu tiên, thực hiện hành động back mặc định
                super.onBackPressed();
            }
        }
    }
}
