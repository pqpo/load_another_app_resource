package pw.qlm.remoteinflater;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.LayoutInflater;

import dalvik.system.DexClassLoader;

/**
 * InflaterContext
 * Created by Administrator on 2017/4/11.
 */
public class InflaterContext extends ContextWrapper {

    private Context contextForResources;
    private LayoutInflater inflater;
    private ClassLoader mClassLoader;

    public InflaterContext(Context base, String packageName) {
        super(base);
        //关键1.根据包名创建可以读取对方资源的Context
        contextForResources = ApplicationUtil.createApplicationContext(base, packageName);
        //关键2.使用该ContextWrapper克隆LayoutInflater
        inflater = LayoutInflater.from(base).cloneInContext(this);
        //关键3.若布局中用到了自定义View，需要使用该ClassLoader
        mClassLoader = new DexClassLoader(ApplicationUtil.getApkPath(contextForResources), getDir("dex", 0).getAbsolutePath(), getDir("so", 0).getAbsolutePath(), super.getClassLoader());
    }

    @Override
    public Object getSystemService(String name) {
        if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
            return inflater;
        }
        return super.getSystemService(name);
    }

    @Override
    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    @Override
    public Resources.Theme getTheme() {
        return contextForResources.getTheme();
    }

    @Override
    public Resources getResources() {
        return contextForResources.getResources();
    }

    @Override
    public String getPackageName() {
        return contextForResources.getPackageName();
    }

//    详见 RemoteView.apply
//
//    public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
//        RemoteViews rvToApply = getRemoteViewsToApply(context);
//
//        View result;
//        // RemoteViews may be built by an application installed in another
//        // user. So build a context that loads resources from that user but
//        // still returns the current users userId so settings like data / time formats
//        // are loaded without requiring cross user persmissions.
//        final Context contextForResources = getContextForResources(context);
//        Context inflationContext = new ContextWrapper(context) {
//            @Override
//            public Resources getResources() {
//                return contextForResources.getResources();
//            }
//            @Override
//            public Resources.Theme getTheme() {
//                return contextForResources.getTheme();
//            }
//            @Override
//            public String getPackageName() {
//                return contextForResources.getPackageName();
//            }
//        };
//
//        LayoutInflater inflater = (LayoutInflater)
//                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        // Clone inflater so we load resources from correct context and
//        // we don't add a filter to the static version returned by getSystemService.
//        inflater = inflater.cloneInContext(inflationContext);
//        inflater.setFilter(this);
//        result = inflater.inflate(rvToApply.getLayoutId(), parent, false);
//
//        rvToApply.performApply(result, parent, handler);
//
//        return result;
//    }

}
