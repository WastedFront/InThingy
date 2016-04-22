package zemris.fer.hr.inthingy.utils;

import android.content.Context;
import android.view.View;
import android.widget.TabHost;

/**
 * Class which can be used in {@link TabHost} for making empty tab content.
 */
public class EmptyTabFactory implements TabHost.TabContentFactory {

    /** Context of activity which will use this class. */
    private Context mContext;

    /**
     * Constructor.
     *
     * @param mContext
     *         context for view
     */
    public EmptyTabFactory(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public View createTabContent(String tag) {
        return new View(mContext);
    }

}