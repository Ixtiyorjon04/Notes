package uz.gita.notes

import android.app.Application
import uz.gita.notes.database.DeletedNoteDatabase
import uz.gita.notes.database.NoteDatabase

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        NoteDatabase.initDatabase(context = this)
        DeletedNoteDatabase.initDatabase(context = this)
    }
}