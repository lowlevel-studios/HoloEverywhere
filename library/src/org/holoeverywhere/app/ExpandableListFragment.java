
package org.holoeverywhere.app;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.R;
import org.holoeverywhere.widget.ExpandableListView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListFragment extends Fragment implements
		ExpandableListView.OnChildClickListener,
		ExpandableListView.OnGroupClickListener,
		ExpandableListView.OnGroupCollapseListener,
		ExpandableListView.OnGroupExpandListener{
    private ExpandableListAdapter mAdapter;
    private CharSequence mEmptyText;
    private View mEmptyView;
    final private Handler mHandler = new Handler();
    private ExpandableListView mList;
    private View mListContainer;
    private boolean mListShown;
    private CharSequence mLoadingText;
    private TextView mLoadingView;
    private View mProgressContainer;
    final private Runnable mRequestFocus = new Runnable() {
        @Override
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };
    private TextView mStandardEmptyView;

    private void ensureList() {
        if (mList != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof ExpandableListView) {
            mList = (ExpandableListView) root;
        } else {
        	mLoadingView = (TextView) root.findViewById(R.id.internalLoading);
            mStandardEmptyView = (TextView) root
                    .findViewById(R.id.internalEmpty);
            if (mStandardEmptyView == null) {
                mEmptyView = root.findViewById(android.R.id.empty);
            } else {
                mStandardEmptyView.setVisibility(View.GONE);
            }
            mProgressContainer = root.findViewById(R.id.progressContainer);
            mListContainer = root.findViewById(R.id.listContainer);
            View rawListView = root.findViewById(android.R.id.list);
            if (!(rawListView instanceof ExpandableListView)) {
                if (rawListView == null) {
                    throw new RuntimeException(
                            "Your content must have a ExpandableListView whose id attribute is "
                                    + "'android.R.id.list'");
                }
                throw new RuntimeException(
                        "Content has view with id attribute 'android.R.id.list' "
                                + "that is not a ExpandableListView class");
            }
            mList = (ExpandableListView) rawListView;
            if (mEmptyView != null) {
                mList.setEmptyView(mEmptyView);
            } else if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
                mList.setEmptyView(mStandardEmptyView);
            }
            if (mLoadingView != null && mLoadingText != null) {
            	mLoadingView.setText(mLoadingText);
            }
        }
        mListShown = true;
        mList.setOnChildClickListener(this);
        mList.setOnGroupClickListener(this);
        mList.setOnGroupCollapseListener(this);
        mList.setOnGroupExpandListener(this);
        if (mAdapter != null) {
            ExpandableListAdapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        } else {
            if (mProgressContainer != null) {
                setListShown(false, false);
            }
        }
        mHandler.post(mRequestFocus);
    }

    protected View getEmptyView() {
        return mEmptyView;
    }
    
    public ExpandableListAdapter getExpandableListAdapter() {
        return mAdapter;
    }    
    
    public ExpandableListView getExpandableListView() {
        ensureList();
        return mList;
    }

    public long getSelectedItemId() {
        ensureList();
        return mList.getSelectedItemId();
    }

    public int getSelectedItemPosition() {
        ensureList();
        return mList.getSelectedItemPosition();
    }

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		return false;
	}    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.expandable_list_content, container, false);
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mListShown = false;
        mEmptyView = mProgressContainer = mListContainer = null;
        mStandardEmptyView = null;
        super.onDestroyView();
    }

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		return false;
	}    
    
	@Override
	public void onGroupCollapse(int groupPosition) {		
	}	
	
	@Override
	public void onGroupExpand(int groupPosition) {		
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    public void setEmptyText(CharSequence text) {
        ensureList();
        if (mStandardEmptyView == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        mStandardEmptyView.setText(text);
        if (mEmptyText == null) {
            mList.setEmptyView(mStandardEmptyView);
        }
        mEmptyText = text;
    }

    public void setListAdapter(ExpandableListAdapter adapter) {
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mList != null) {
            mList.setAdapter(adapter);
            if (!mListShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter. It is now time to show it.
                setListShown(true, getView().getWindowToken() != null);
            }
        }
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (mProgressContainer == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }

    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }
    
    public void setLoadingText(CharSequence text) {
        ensureList();
        if (mLoadingView == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        mLoadingView.setText(text);
        mLoadingText = text;
    }    
    
    public boolean setSelectedChild(int groupPosition, int childPosition,
            boolean shouldExpandGroup) {
        ensureList();
        return mList.setSelectedChild(groupPosition, childPosition,
                shouldExpandGroup);
    }

    public void setSelectedGroup(int groupPosition) {
        ensureList();
        mList.setSelectedGroup(groupPosition);
    }    
}
