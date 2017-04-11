package pw.qlm.remoteinflater;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.UserHandle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ApplicationUtil
 * Created by Administrator on 2017/4/11.
 */
class ApplicationUtil {

    static String getApkPath(Context context) {
        try {
            Method getCodePath = ApplicationInfo.class.getMethod("getCodePath");
            String codePath = (String) getCodePath.invoke(context.getApplicationInfo());
            return codePath + "/base.apk";
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Context createApplicationContext(Context context, String packageName) {
        try {
            Method createApplicationContext = Context.class.getMethod("createApplicationContext", ApplicationInfo.class, int.class);
            return (Context) createApplicationContext.invoke(context, getApplicationInfo(packageName,  myUserId()), Context.CONTEXT_RESTRICTED);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int myUserId() {
        try {
            Method myUserId = UserHandle.class.getMethod("myUserId");
            return (int) myUserId.invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static ApplicationInfo getApplicationInfo(String packageName, int userId) {
        if (packageName == null) {
            return null;
        }

        // Get the application for the passed in package and user.
        Application application = getCurrentApplication();
        if (application == null) {
            throw new IllegalStateException("Cannot create remote views out of an aplication.");
        }
        Context context = createPackageContextAsUser(application.getBaseContext(), packageName, userId);
        return context != null ? context.getApplicationInfo() : null;
    }

    private static Context createPackageContextAsUser(Context baseContext, String packageName, int userId) {
        Parcel userIdParcel = Parcel.obtain();
        userIdParcel.writeInt(userId);
        try {
            Method createPackageContextAsUser = Context.class.getMethod("createPackageContextAsUser", String.class, int.class, UserHandle.class);
            return (Context) createPackageContextAsUser.invoke(baseContext, packageName, 0, new UserHandle(userIdParcel));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            userIdParcel.recycle();
        }
        return null;
    }

    private static Application getCurrentApplication() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentApplication = activityThreadClass.getMethod("currentApplication");
            return (Application) currentApplication.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
