package co.ltlabs.ltmechanic.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import timber.log.Timber;


public class DimUtil {
    private static final String TAG="DimUtil";
    public static void dimBehind(PopupWindow popupWindow) {
        try {
            View container;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
            if (popupWindow.getBackground() != null) {
                container = (View) container.getParent();
            }
            Context context = popupWindow.getContentView().getContext();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND; // add a flag here instead of clear others
            p.dimAmount = 0.3f;
            wm.updateViewLayout(container, p);
        } catch (Exception e) {
           // Log.e(TAG, "dimBehind: ", e);
            Timber.e("%s", e.getLocalizedMessage());
        }
    }
}
