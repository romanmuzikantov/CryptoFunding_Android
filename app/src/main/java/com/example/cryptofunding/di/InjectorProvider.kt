package com.example.cryptofunding.di

import android.app.Activity

interface InjectorProvider {
    val component: ApplicationComponent
}

val Activity.injector get() = (application as InjectorProvider).component