package com.hanna2100.cleannote.business.di

import com.hanna2100.cleannote.business.data.NoteDataFactory
import com.hanna2100.cleannote.business.data.cache.FakeNoteCacheDataSourceImpl
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.FakeNoteNetworkDataSourceImpl
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependencyContainer {
    private val dataFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil = DateUtil(dataFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory
    lateinit var noteDataFactory: NoteDataFactory

    init {
        isUnitTest =  true
    }

    fun build() {
        this.javaClass.classLoader?.let { classLoader ->
            noteDataFactory = NoteDataFactory(classLoader)
        }
        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
                notesData = noteDataFactory.produceHashMapOfNotes(
                        noteDataFactory.produceListOfNotes()
                ),
                deletedNotesData = HashMap(),
                dateUtil = dateUtil
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
                notesData = noteDataFactory.produceHashMapOfNotes(
                        noteDataFactory.produceListOfNotes()
                ),
                dateUtil = dateUtil
        )
    }
}