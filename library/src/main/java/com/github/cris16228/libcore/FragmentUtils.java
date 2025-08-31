package com.github.cris16228.libcore;

import android.os.Bundle;
import android.view.View;

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
    FragmentTransaction transaction;
    FragmentManager fragmentManager;

    public static FragmentUtils with(FragmentActivity _fragmentActivity) {
        FragmentUtils fragmentUtils = new FragmentUtils();
        fragmentUtils.fragmentManager = _fragmentActivity.getSupportFragmentManager();
        fragmentUtils.fragmentActivity = _fragmentActivity;
        fragmentUtils.transaction = fragmentUtils.fragmentManager.beginTransaction();
        return fragmentUtils;
    }

    public FragmentUtils show(int showFragment, View view) {
        Fragment fragment = fragmentManager.findFragmentById(showFragment);
        if (fragment != null) {
            transaction.show(fragment);
        }
        if (view != null) {
            view.findViewById(showFragment).setVisibility(View.VISIBLE);
        }
        return this;
    }

    public FragmentUtils show(int showFragment) {
        Fragment fragment = fragmentManager.findFragmentById(showFragment);
        if (fragment != null) {
            transaction.show(fragment);
        }
        return this;
    }

    public FragmentUtils hide(int hideFragment) {
        Fragment fragment = fragmentManager.findFragmentById(hideFragment);
        if (fragment != null) {
            transaction.hide(fragment);
        }
        return this;
    }

    public FragmentUtils add(int fragmentID, Fragment addFragment, Bundle bundle) {
        if (bundle != null) {
            addFragment.setArguments(bundle);
        }
        transaction.add(fragmentID, addFragment);
        transaction.addToBackStack(addFragment.getClass().getSimpleName().toLowerCase());
        return this;
    }

    public FragmentUtils replace(int fragmentID, Fragment addFragment, Bundle bundle) {
        if (bundle != null) {
            addFragment.setArguments(bundle);
        }
        transaction.replace(fragmentID, addFragment);
        transaction.addToBackStack(addFragment.getClass().getSimpleName().toLowerCase());
        return this;
    }

    public FragmentUtils replace(int fragmentID, Fragment replaceFragment) {
        replace(fragmentID, replaceFragment, null);
        return this;
    }

    public FragmentUtils add(int fragmentID, Fragment replaceFragment) {
        add(fragmentID, replaceFragment, null);
        return this;
    }

    public void build() {
        transaction.commit();
    }

   /* public void replace(@IdRes int containerViewId, @NonNull Fragment fragment) {
        FragmentManager manager = fragmentActivity.getSupportFragmentManager();
        manager.beginTransaction().replace(containerViewId,
                fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
    }*/

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
