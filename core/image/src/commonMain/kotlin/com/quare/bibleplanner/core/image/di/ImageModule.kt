package com.quare.bibleplanner.core.image.di

import com.quare.bibleplanner.core.image.DecodeImageBitmap
import com.quare.bibleplanner.core.image.DecodeImageBitmapImpl
import org.koin.dsl.module

val imageModule = module {
    factory<DecodeImageBitmap> { DecodeImageBitmapImpl() }
}
