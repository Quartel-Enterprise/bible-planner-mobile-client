package com.quare.bibleplanner.core.profile.domain.model

val AvatarSource.photoUrl: String?
    get() = (this as? AvatarSource.Remote)?.url

val AvatarSource.photoBytes: ByteArray?
    get() = (this as? AvatarSource.Pending)?.bytes
