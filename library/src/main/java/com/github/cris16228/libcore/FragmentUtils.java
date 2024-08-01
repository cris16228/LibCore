package com.github.cris16228.libcore;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentUtils {

    FragmentActivity fragmentActivity;

    public static FragmentUtils with(FragmentActivity _fragmentActivity) {
        FragmentUtils fragmentUtils = new FragmentUtils();
        fragmentUtils.fragmentActivity = _fragmentActivity;
        return fragmentUtils;
    }

    public void replace(@IdRes int containerViewId, @NonNull Fragment fragment) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        manager.beginTransaction().replace(containerViewId,
                fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
    }

    public void replace(@IdRes int containerViewId, @NonNull Fragment fragment, boolean addToBackStack) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        if (addToBackStack)
            manager.beginTransaction().replace(containerViewId,
                    fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
        else
            manager.beginTransaction().replace(containerViewId,
                    fragment, fragment.getClass().getSimpleName()).commit();
    }

    public void replaceAndAnimate(@IdRes int containerViewId, @NonNull Fragment fragment, boolean addToBackStack) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        if (addToBackStack)
            manager.beginTransaction().setCustomAnimations(R.anim.scroll_up, R.anim.scroll_down).replace(containerViewId,
                    fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
        else
            manager.beginTransaction().setCustomAnimations(R.anim.scroll_up, R.anim.scroll_down).replace(containerViewId,
                    fragment, fragment.getClass().getSimpleName()).commit();
    }

    public void replaceAndAnimate(@IdRes int containerViewId, @NonNull Fragment fragment) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        manager.beginTransaction().setCustomAnimations(R.anim.scroll_up, R.anim.scroll_down).replace(containerViewId,
                fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
    }

    public void replaceAndAnimate(@IdRes int containerViewId, @NonNull Fragment fragment, boolean addToBackStack, @AnimatorRes @AnimRes int enter,
                                  @AnimatorRes @AnimRes int exit) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        if (addToBackStack)
            manager.beginTransaction().setCustomAnimations(enter, exit).replace(containerViewId,
                    fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
        else
            manager.beginTransaction().setCustomAnimations(enter, exit).replace(containerViewId,
                    fragment, fragment.getClass().getSimpleName()).commit();
    }

    public void replaceAndAnimate(@IdRes int containerViewId, @NonNull Fragment fragment, @AnimatorRes @AnimRes int enter,
                                  @AnimatorRes @AnimRes int exit) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        manager.beginTransaction().setCustomAnimations(enter, exit).replace(containerViewId,
                fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
    }

    public void refreshFragment(FragmentActivity fragmentActivity, Fragment fragment) {
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        ft.detach(fragment).attach(fragment).commit();
    }
}
