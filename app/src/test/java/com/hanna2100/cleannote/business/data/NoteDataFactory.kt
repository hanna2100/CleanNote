package com.hanna2100.cleannote.business.data

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.hanna2100.cleannote.business.domain.model.Note
import java.lang.reflect.Type

class NoteDataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceListOfNotes(): List<Note>{
        val typeObj: Type = object : TypeToken<List<Note>>() {}.type
        val notes: List<Note> = Gson().fromJson(
                getNotesFromFile("note_list.json"),
                typeObj
            )
        return notes
    }

    fun produceHashMapOfNotes(noteList: List<Note>): HashMap<String, Note> {
        val map = HashMap<String, Note>()
        for(note in noteList) {
            map.put(note.id, note)
        }
        return map
    }

    fun produceEmptyListOfNotes(): List<Note>{
        return ArrayList()
    }

    fun getNotesFromFile(fileName: String): String {
        return testClassLoader.getResource(fileName).readText()
    }

}
