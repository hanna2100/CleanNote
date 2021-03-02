package com.hanna2100.cleannote.framework.datasource.data

import android.app.Application
import android.content.res.AssetManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDataFactory
@Inject
constructor(
    private val application: Application,
    private val noteFactory: NoteFactory
) {

    fun produceListOfNotes(): List<Note> {
        val notes: List<Note> = Gson().fromJson(
                readJsonFromAsset("note_list.json"),
                object : TypeToken<List<Note>>(){}.type
        )
        return notes
    }

    fun produceEmptyListOfNotes(): List<Note> {
        return emptyList()
    }

    private fun readJsonFromAsset(filename: String): String? {
        var json: String? = null
        json = try {
            val inputStream: InputStream = (application.assets as AssetManager).open(filename)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return json
    }

    fun createSingleNote(
        id: String? = null,
        title: String,
        body: String? = null
    ) = noteFactory.createSingleNote(id, title, body)

    fun createNoteList(numNotes: Int) = noteFactory.createNoteList(numNotes)
}