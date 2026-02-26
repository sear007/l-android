package co.ltlabs.ltmechanic.di.setup

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.di.ViewModelKey
import co.ltlabs.ltmechanic.viewmodels.auth.AuthViewModel
import co.ltlabs.ltmechanic.viewmodels.setup.SetupViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TranslationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SetupViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(SetupViewModel::class)
    abstract fun bindSetupViewModel(viewModel: SetupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TranslationViewModel::class)
    abstract fun bindTranslationViewModel(viewModel: TranslationViewModel): ViewModel

}