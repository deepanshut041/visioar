package life.plank.visior.di

import life.plank.visior.ui.ArViewViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory {
        ArViewViewModel(get(), get(), get())
    }
}