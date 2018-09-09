package ru.johnlife.lifetools.adapter;

import android.view.View;

import java.util.List;

/**
 * Created by yanyu on 5/23/2016.
 */
public abstract class BaseSelectableAdapter<T> extends BaseAdapter<T> {
    private T selected;

    public abstract class ViewHolderSelectable extends BaseAdapter.ViewHolder<T> {
        public ViewHolderSelectable(View view) {
            super(view);
            view.setOnClickListener(v -> select(getItem()));
        }

        protected boolean isSelected() {
            return getItem() == selected;
        }
    }


    public BaseSelectableAdapter(int itemLayoutId) {
        super(itemLayoutId);
    }

    public BaseSelectableAdapter(int itemLayoutId, List<T> items) {
        super(itemLayoutId, items);
    }

    protected boolean isSelected(T item) {
        return selected == item;
    }

    public T getSelected() {
        return selected;
    }

    public void select(T item) {
        if (null == item) return;
        T oldSelected = selected;
        selected = item;
        if (oldSelected != null) {
            notifyItemChanged(indexOf(oldSelected));
        }
        notifyItemChanged(indexOf(item));
    }

}
