package com.github.meaninglesscode.geocameraapp.di

import androidx.lifecycle.ViewModel
import com.github.meaninglesscode.geocameraapp.maps.MapsFragment
import com.github.meaninglesscode.geocameraapp.maps.MapsViewModel
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Dagger [Module] used for injection purposes with the maps section of the UI.
 */
@Module
abstract class MapsModule {
    /**
     * [ContributesAndroidInjector] method to generate an [AndroidInjector] implemented within the
     * [ViewModelBuilder] subclass component.
     *
     * @return The resultant [MapsFragment]
     */
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun mapsFragment(): MapsFragment

    /**
     * [Binds] the [MapsViewModel] [IntoMap] for Dagger injection with the [ViewModelKey] being
     * [MapsViewModel]::class.
     *
     * @return The bound [ViewModel]
     */
    @Binds
    @IntoMap
    @ViewModelKey(MapsViewModel::class)
    abstract fun bindViewModel(viewModel: MapsViewModel): ViewModel
}