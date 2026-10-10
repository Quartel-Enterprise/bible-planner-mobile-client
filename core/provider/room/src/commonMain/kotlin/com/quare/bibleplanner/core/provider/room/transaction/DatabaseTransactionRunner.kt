package com.quare.bibleplanner.core.provider.room.transaction

/*
 * Why: writes that span several DAOs need one transaction, so readers never see them half done,
 * and callers outside this module don't get the database itself.
 */
fun interface DatabaseTransactionRunner {
    suspend operator fun invoke(block: suspend () -> Unit)
}
