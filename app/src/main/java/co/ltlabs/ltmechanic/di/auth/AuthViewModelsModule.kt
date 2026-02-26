package co.ltlabs.ltmechanic.di.auth

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.di.ViewModelKey
import co.ltlabs.ltmechanic.viewmodels.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(viewModel: AuthViewModel): ViewModel
}