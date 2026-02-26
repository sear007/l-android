package co.ltlabs.ltmechanic.di.auth

import co.ltlabs.ltmechanic.network.ApiCompanyLogin
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.auth.AuthApi
import co.ltlabs.ltmechanic.network.auth.Reference2Api
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
abstract class AuthModule {

    companion object {

        @AuthScope
        @Provides
        fun provideAuthApi(@Named("auth")retrofit: Retrofit): AuthApi =
            retrofit.create(AuthApi::class.java)

        @AuthScope
        @Provides
        fun provideReference2Api(@Named("reference2")retrofit: Retrofit): Reference2Api =
            retrofit.create(Reference2Api::class.java)

        @AuthScope
        @Provides
        @Named("globalapi")
        fun provideGlobalApi(@Named("global")retrofit: Retrofit): ApiGlobal =
            retrofit.create(ApiGlobal::class.java)

        @AuthScope
        @Provides
        @Named("companyLogin")
        fun provideCompanyLogin(@Named("companyLogin")retrofit: Retrofit): ApiCompanyLogin =
            retrofit.create(ApiCompanyLogin::class.java)
    }
}