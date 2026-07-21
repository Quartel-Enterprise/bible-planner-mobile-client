package com.quare.bibleplanner.feature.editprofile.di

import com.quare.bibleplanner.core.image.AvatarImageCropper
import com.quare.bibleplanner.core.image.avatarImageCropper
import com.quare.bibleplanner.feature.editprofile.domain.usecase.RemoveProfilePhoto
import com.quare.bibleplanner.feature.editprofile.domain.usecase.SetProfilePhoto
import com.quare.bibleplanner.feature.editprofile.domain.usecase.UpdateDisplayName
import com.quare.bibleplanner.feature.editprofile.domain.usecase.UseProviderPhoto
import com.quare.bibleplanner.feature.editprofile.domain.usecase.impl.RemoveProfilePhotoUseCase
import com.quare.bibleplanner.feature.editprofile.domain.usecase.impl.SetProfilePhotoUseCase
import com.quare.bibleplanner.feature.editprofile.domain.usecase.impl.UpdateDisplayNameUseCase
import com.quare.bibleplanner.feature.editprofile.domain.usecase.impl.UseProviderPhotoUseCase
import com.quare.bibleplanner.feature.editprofile.presentation.DecodeImageBitmap
import com.quare.bibleplanner.feature.editprofile.presentation.DecodeImageBitmapImpl
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.CropPhotoViewModel
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditNameViewModel
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.EditProfileViewModel
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val editProfileModule = module {
    factoryOf(::UpdateDisplayNameUseCase).bind<UpdateDisplayName>()
    factoryOf(::SetProfilePhotoUseCase).bind<SetProfilePhoto>()
    factoryOf(::RemoveProfilePhotoUseCase).bind<RemoveProfilePhoto>()
    factoryOf(::UseProviderPhotoUseCase).bind<UseProviderPhoto>()
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::EditNameViewModel)
    viewModelOf(::ProfilePhotoViewModel)
    factory<DecodeImageBitmap> { DecodeImageBitmapImpl() }
    factory<AvatarImageCropper> { avatarImageCropper() }
    viewModel { params ->
        CropPhotoViewModel(
            route = params.get(),
            decodeImageBitmap = get(),
            cropImage = get(),
            setProfilePhoto = get(),
            trackEvent = get(),
        )
    }
}
