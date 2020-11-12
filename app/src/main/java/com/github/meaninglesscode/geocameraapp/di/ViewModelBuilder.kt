package com.github.meaninglesscode.geocameraapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

/**
 * [ViewModelFactory] utilizing Dagger to create [ViewModel] instances. Implements
 * [ViewModelProvider.Factory].
 *
 * @param [creators] [JvmSuppressWildcards] [Map] of [ViewModel] classes to their [Provider]
 */
class ViewModelFactory @Inject constructor(
    private val creators: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>
): ViewModelProvider.Factory {
    /**
     * Override method to create a new [ViewModel]. If there is no [Provider] for the desired
     * [ViewModel], then an [IllegalArgumentException] is thrown.
     *
     * @return [T] [ViewModel] type to return from the associated [Provider]
     */
    @Throws(IllegalArgumentException::class, RuntimeException::class)
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        // Attempt to get the ViewModel creator for the given modelClass
        var creator: Provider<out ViewModel>? = creators[modelClass]

        // If no creator, check if ViewModel can be assigned from any Provider
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }

        // If still null after checking all Providers, then the model class is not implemented
        if (creator == null)
            throw IllegalArgumentException("Unknown model class: $modelClass")

        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        }
        catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

/**
 * Dagger [Module] for binding a [ViewModelFactory]
 */
@Module
internal abstract class ViewModelBuilder {
    /**
     * [Binds] method to bind a [ViewModelFactory] for Dagger injection.
     *
     * @param [factory] [ViewModelFactory] to bind
     * @return The bound [ViewModelProvider.Factory] bound from [ViewModelFactory]
     */
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

/**
 * [Target] annotation class used as a [MapKey] for accessing [ViewModel]s. Additionally, this class
 * is annotated with [AnnotationRetention.RUNTIME].
 *
 * @param [value] [KClass] to get [ViewModel] for
 */
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)