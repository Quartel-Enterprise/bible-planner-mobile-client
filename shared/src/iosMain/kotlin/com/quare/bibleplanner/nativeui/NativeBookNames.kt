package com.quare.bibleplanner.nativeui

import com.quare.bibleplanner.core.books.util.getBookNames

/*
 * Why: book names stay in the shared Compose resources until step 4 of the native UI migration gives them one native
 * home, so the SwiftUI screens load them from there, keyed by the BookId name, in the language Compose uses.
 */
object NativeBookNames {
    // Why: Swift calls this from the main actor; without @Throws a resource read failure would terminate the app.
    @Throws(Exception::class)
    suspend fun load(): Map<String, String> = getBookNames().mapKeys { (bookId, _) -> bookId.name }
}
