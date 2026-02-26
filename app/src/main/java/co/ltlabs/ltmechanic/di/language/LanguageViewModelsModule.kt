package co.ltlabs.ltmechanic.di.language

import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.di.ViewModelKey
import co.ltlabs.ltmechanic.viewmodels.auth.AuthViewModel
import co.ltlabs.ltmechanic.viewmodels.language.LanguageSettingViewModel
import co.ltlabs.ltmechanic.viewmodels.setup.SetupViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LanguageViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(LanguageSettingViewModel::class)
    abstract fun bindLanguageSettingViewModel(viewModel: LanguageSettingViewModel): ViewModel
}