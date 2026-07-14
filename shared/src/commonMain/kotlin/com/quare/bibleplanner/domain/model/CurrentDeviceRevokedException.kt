package com.quare.bibleplanner.domain.model

internal class CurrentDeviceRevokedException :
    Exception("Current device row disappeared from the synced list; ending the local session")
