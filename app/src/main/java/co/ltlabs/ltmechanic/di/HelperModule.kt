package co.ltlabs.ltmechanic.di

import dagger.Module
import dagger.Provides
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@Module
class HelperModule {

    @Singleton
    @Provides
    fun provideDateFormatYYYYMMDDHM() = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault())

}