package com.hanna2100.cleannote.business.di

import com.hanna2100.cleannote.business.data.cache.FakeNoteCacheDataSourceImpl
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.FakeNoteNetworkDataSourceImpl
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*

class DependencyContainer {
    private val dataFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.KOREA)
    val dateUtil = DateUtil(dataFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory

    init {
        isUnitTest =  true
    }

    fun build() {
        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = HashMap(),
            deletedNotesData = HashMap()
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = HashMap(),
            dateUtil = dateUtil
        )
    }
}