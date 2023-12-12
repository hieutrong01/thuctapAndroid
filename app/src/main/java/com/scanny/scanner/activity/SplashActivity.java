package com.scanny.scanner.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.scanny.scanner.R;
import com.scanny.scanner.activity.uiv2.UIV2MainActivity;

public class SplashActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.splash_acvtivity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               // nextActivit();
//                startActivity(new Intent(SplashActivity.this, UIV2MainActivity.class));
//                finish();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);
    }

//    private void nextActivit() {
//        FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
//        if (user==null){
//            //chua login
//            startActivity( new Intent(SplashActivity.this,LoginActivity.class) );
//        }else {
//            startActivity(new Intent(SplashActivity.this, MainActivity.class));
//            finish();
//        }
//    }
}
