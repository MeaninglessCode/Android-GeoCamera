package com.github.meaninglesscode.geocameraapp.di

import android.content.Context
import com.github.meaninglesscode.geocameraapp.GeoCameraApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * [ApplicationComponent] is a dagger-injected [Singleton] that serves as the hub for other Dagger
 * [Component]s. Thsi interface holds modules for [ApplicationModule],
 * [AndroidSupportInjectionModule], and [MapsModule].
 */
@Singleton
@Component(
    modules=[
        ApplicationModule::class,
        AndroidSupportInjectionModule::class,
        MapsModule::class
    ]
)
interface ApplicationComponent: AndroidInjector<GeoCameraApplication> {
    /**
     * [Factory] is an interface for [Component.Factory] to allow for the creation of an
     * [ApplicationComponent] via the [BindsInstance] passed [Context].
     */
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }
}