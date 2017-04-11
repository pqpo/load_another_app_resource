package pw.qlm.remoteinflater;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //1.根据包名创建可以加载到其他应用资源的Context
        InflaterContext inflaterContext = new InflaterContext(this, "pw.qlm.otherapp");
        //2.根据上面Context创建LayoutInflater
        LayoutInflater layoutInflater = LayoutInflater.from(inflaterContext);
        //3.获取其他应用的资源id
        int layoutId = inflaterContext.getResources().getIdentifier("activity_main", "layout", inflaterContext.getPackageName());
        //4.根据资源id加载布局
        View view = layoutInflater.inflate(layoutId, (ViewGroup) getWindow().getDecorView(), false);
        setContentView(view);
    }

}
