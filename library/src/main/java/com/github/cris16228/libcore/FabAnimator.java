package com.github.cris16228.libcore;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabAnimator {

    private static Animation rotate_open_anim;
    private static Animation rotate_close_anim;
    private static Animation from_bottom_anim;
    private static Animation to_bottom_anim;
    private Context context;
    private boolean clicked = false;
    private FloatingActionButton _main_fab;
    private FloatingActionButton[] _fabs;

    public static FabAnimator with(Context _context) {
        FabAnimator fabAnimator = new FabAnimator();
        fabAnimator.context = _context;
        fabAnimator.clicked = false;
        return fabAnimator;
    }

    public static FabAnimator with(Context _context, @NonNull FloatingActionButton main_fab, @NonNull FloatingActionButton... fabs) {
        FabAnimator fabAnimator = new FabAnimator();
        fabAnimator.context = _context;
        fabAnimator._main_fab = main_fab;
        fabAnimator._fabs = fabs;
        fabAnimator.clicked = false;
        return fabAnimator;
    }

    public FabAnimator load() {
        rotate_open_anim = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim);
        rotate_close_anim = AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim);
        from_bottom_anim = AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim);
        to_bottom_anim = AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim);
        return this;
    }

    public FabAnimator onClick(@NonNull FloatingActionButton fab, View.OnClickListener listener) {
        fab.setOnClickListener(listener);
        return this;
    }

    public FabAnimator onClick(@NonNull FloatingActionButton fab, boolean animateClose, View.OnClickListener listener) {
        fab.setOnClickListener(listener);
        if (animateClose)
            animateClose(_main_fab, _fabs);
        return this;
    }

    public FabAnimator animate(@NonNull FloatingActionButton main_fab, @NonNull FloatingActionButton... fabs) {
        main_fab.setOnClickListener(v -> {
            for (FloatingActionButton fab : fabs) {
                setVisibility(fab, clicked);
                setAnimation(main_fab, fab, clicked);
                setClickable(fab, clicked);
            }
            clicked = !clicked;
        });
        return this;
    }

    public void animateClose(@NonNull FloatingActionButton main_fab, @NonNull FloatingActionButton... fabs) {
        clicked = true;
        for (FloatingActionButton fab : fabs) {
            setVisibility(fab, clicked);
            setAnimation(main_fab, fab, clicked);
            setClickable(fab, clicked);
        }
        clicked = false;
    }

    private void setAnimation(FloatingActionButton main_fab, FloatingActionButton fab, boolean clicked) {
        if (!clicked) {
            if (fab != null)
                if (from_bottom_anim != null)
                    fab.startAnimation(from_bottom_anim);
                else
                    Log.e(getClass().getSimpleName(), "\"from_bottom_anim\" animation is null!");
            else
                Log.e(getClass().getSimpleName(), "fab is null!");
            if (main_fab != null)
                if (rotate_open_anim != null)
                    main_fab.startAnimation(rotate_open_anim);
                else
                    Log.e(getClass().getSimpleName(), "\"rotate_open_anim\" animation is null!");
            else
                Log.e(getClass().getSimpleName(), "main_fab is null!");
        } else {
            if (fab != null)
                if (to_bottom_anim != null)
                    fab.startAnimation(to_bottom_anim);
                else
                    Log.e(getClass().getSimpleName(), "\"to_bottom_anim\" animation is null!");
            else
                Log.e(getClass().getSimpleName(), "fab is null!");
            if (main_fab != null)
                if (rotate_close_anim != null)
                    main_fab.startAnimation(rotate_close_anim);
                else
                    Log.e(getClass().getSimpleName(), "\"rotate_close_anim\" animation is null!");
            else
                Log.e(getClass().getSimpleName(), "main_fab is null!");
        }
    }

    private void setVisibility(FloatingActionButton fab, boolean clicked) {
        if (fab != null) {
            if (!clicked)
                fab.setVisibility(View.VISIBLE);
            else
                fab.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(FloatingActionButton fab, boolean clicked) {
        if (fab != null) {
            if (!clicked)
                fab.startAnimation(from_bottom_anim);
            else
                fab.startAnimation(to_bottom_anim);
        }

    }

    private void setClickable(FloatingActionButton fab, boolean clicked) {
        if (fab != null)
            fab.setClickable(!clicked);
    }
}
