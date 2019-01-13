package com.hosseinzadeh.zahra.prmissionchecker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnAppClickListener {

    RecyclerView mRecyclerView;
    AppListAdapter adapter;
    ArrayList<AppItem> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.appList);
        apps = getInstalledPackages();

        adapter = new AppListAdapter(apps, getApplicationContext());
        mRecyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(MainActivity.this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        adapter.setClickListener(this);


    }

    protected ArrayList<AppItem> getInstalledPackages() {
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<AppItem> applist = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            String packageName = activityInfo.applicationInfo.packageName;
            String label = (String) packageManager.getApplicationLabel(activityInfo.applicationInfo);
            Drawable icon = packageManager.getApplicationIcon(activityInfo.applicationInfo);
            AppItem item = new AppItem();
            item.setPackageName(packageName);
            item.setAppName(label);
            item.setAppIcon(icon);
            applist.add(item);
        }
        return applist;
    }

    protected String getPermissionsByPackageName(String packageName) {
        StringBuilder builder = new StringBuilder();

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            int counter = 1;
            for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    String permission = packageInfo.requestedPermissions[i];
                    builder.append("" + counter + ". " + permission + "\n");
                    counter++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    @Override
    public void onAppSelected(View view, int position) {
        AppItem app = apps.get(position);
        String permissions = getPermissionsByPackageName(app.getPackageName());
        Intent i = new Intent(MainActivity.this, ViewPermissions.class);
        Bitmap bmp = DrawableToBitmap.drawableToBitmap(app.getAppIcon());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        i.putExtra("icon", byteArray);
        i.putExtra("name", app.getAppName());
        i.putExtra("package", app.getPackageName());
        i.putExtra("permissions", permissions);
        startActivity(i);
//        Toast.makeText(MainActivity.this, permissions, Toast.LENGTH_LONG).show();
    }
}
