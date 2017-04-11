# load_another_app_resource
Load another application's resource like RemoteView do,  just for fun!

## 声明：
通过包名和资源名即可加载到其他应用的资源，仅用于学习RemoteView远程加载布局，复杂布局下存在很多兼容性问题。

## 使用步骤:

1.根据包名创建可以加载到其他应用资源的Context  
```java 
InflaterContext inflaterContext = new InflaterContext(this, "pw.qlm.otherapp");
```  
2.根据上面Context创建LayoutInflater  
```java 
LayoutInflater layoutInflater = LayoutInflater.from(inflaterContext);
```  
3.获取其他应用的资源id  
```java 
int layoutId = inflaterContext.getResources().getIdentifier("activity_main", "layout", inflaterContext.getPackageName());
```   
4.根据资源id加载布局  
```java 
View view = layoutInflater.inflate(layoutId, (ViewGroup) getWindow().getDecorView(), false);
```  
## 效果：
一个普通应用([otherApp.apk](otherApp.apk "OtherApp"))，布局里包括一行文字，和一张图片；其中文字部分使用了自定义View(MyTextView extends TextView),图片对应ImageView。
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="pw.qlm.otherapp.MainActivity">

    <pw.qlm.otherapp.MyTextView
        android:id="@+id/mtv"
        android:padding="10dp"
        android:textSize="18sp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerHorizontal="true"
        android:text="This layout is from other app"/>

    <ImageView
        android:layout_below="@id/mtv"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/cat"/>

</RelativeLayout>
```
![](http://src.qlm.pw/ImgOtherApp.png?imageView2/3/w/500/h/400/q/60|watermark/2/text/cWxtLnB3/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)

---

另一个应用（RemoteInflater App），根据使用步骤中的代码加载OtherAPP中的布局（包括其中的资源，自定义View）

![](http://src.qlm.pw/ImgRemoteInflaterApp.png?imageView2/3/w/500/h/400/q/60|watermark/2/text/cWxtLnB3/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
    
## 原理：
  ```java
  public InflaterContext(Context base, String packageName) {
        super(base);
        //关键1.根据包名创建可以读取对方资源的Context
        contextForResources = ApplicationUtil.createApplicationContext(base, packageName);
        //关键2.使用该ContextWrapper克隆LayoutInflater
        inflater = LayoutInflater.from(base).cloneInContext(this);
        //关键3.若布局中用到了自定义View，需要使用该ClassLoader
        mClassLoader = new DexClassLoader(ApplicationUtil.getApkPath(contextForResources), getDir("dex", 0).getAbsolutePath(), getDir("so", 0).getAbsolutePath(), super.getClassLoader());
    }
  ```
  
  ## 参考RemoteView.apply()方法：
  ```java
     public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);

        View result;
        // RemoteViews may be built by an application installed in another
        // user. So build a context that loads resources from that user but
        // still returns the current users userId so settings like data / time formats
        // are loaded without requiring cross user persmissions.
        final Context contextForResources = getContextForResources(context);
        Context inflationContext = new ContextWrapper(context) {
            @Override
            public Resources getResources() {
                return contextForResources.getResources();
            }
            @Override
            public Resources.Theme getTheme() {
                return contextForResources.getTheme();
            }
            @Override
            public String getPackageName() {
                return contextForResources.getPackageName();
            }
        };

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Clone inflater so we load resources from correct context and
        // we don't add a filter to the static version returned by getSystemService.
        inflater = inflater.cloneInContext(inflationContext);
        inflater.setFilter(this);
        result = inflater.inflate(rvToApply.getLayoutId(), parent, false);

        rvToApply.performApply(result, parent, handler);

        return result;
    }
  ```
