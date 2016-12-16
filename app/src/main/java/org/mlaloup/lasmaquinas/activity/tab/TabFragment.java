package org.mlaloup.lasmaquinas.activity.tab;

import android.support.v4.app.Fragment;


public abstract class TabFragment<T> extends Fragment {

    public abstract void updateTabContent(T contentData);
}
