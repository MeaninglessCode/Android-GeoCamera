package com.github.meaninglesscode.geocameraapp

import android.content.Context
import com.github.meaninglesscode.geocameraapp.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

/**
 * Base application that is the entry poitn for Dagger dependency injection. [GeoCameraApplication]
 * implements [DaggerApplication] to this end and returns a [DaggerApplicationComponent] from the
 * [GeoCameraApplication.applicationInjector] method.
 */
class GeoCameraApplication: DaggerApplication() {
    /**
     * Override of the [DaggerApplication] onCreate method in case I need to add something here
     * at a later point.
     */
    override fun onCreate() {
        super.onCreate()
    }

    /**
     * This function creates the [DaggerApplicationComponent] via it's associated factory and
     * passes the current [Context] (applicationContext) into the creation method. The
     * [DaggerApplicationComponent] object is an automatically generated Dagger component.
     */
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }
}