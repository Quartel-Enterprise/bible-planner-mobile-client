package com.quare.bibleplanner.core.model.book

fun BookId.isNewTestament(): Boolean = ordinal >= BookId.MAT.ordinal

fun BookId.isOldTestament(): Boolean = !isNewTestament()
