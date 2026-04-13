package com.example.movilexplora.core.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ResourceProvider {
    fun getString(id: Int, vararg formatArgs: Any): String
}

class ResourceProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourceProvider {
    override fun getString(id: Int, vararg formatArgs: Any): String {
        return context.getString(id, *formatArgs)
    }
}
