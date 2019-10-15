package life.plank.visior.di

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner

interface DependencyProvider {
    fun getContext(): Context
    fun getLifecycleOwner(): LifecycleOwner
    fun getPermissionActivity(): Activity
}