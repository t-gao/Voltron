package com.voltron.demo.app;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import com.voltron.router.EndPointType;
import com.voltron.router.api.VRouter;

public class FragContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_container);
        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFrag();
            }
        });
    }

    private void showFrag() {
        Pair<EndPointType, Class> pair = VRouter.resolveEndPoint("voltron://demoa/frag1");
        if (pair == null || pair.second == null ||
                (pair.first != EndPointType.FRAGMENT_V4) && pair.first != EndPointType.FRAGMENT) {
            return;
        }

        if (pair.first == EndPointType.FRAGMENT_V4) {
            try {
                Fragment fragment = (Fragment) pair.second.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, fragment).commitAllowingStateLoss();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
