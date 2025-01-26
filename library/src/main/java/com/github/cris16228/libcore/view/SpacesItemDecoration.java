package com.github.cris16228.libcore.view;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;
    private final int spanCount;

    public SpacesItemDecoration(int space, int spanCount) {
        this.space = space;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        outRect.left = column == 0 ? space : space / 2;
        outRect.right = column == spanCount - 1 ? 0 : space / 2;
        outRect.bottom = space;

        if (position < spanCount)
            outRect.top = space;
    }
}
