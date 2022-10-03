package uz.gita.notes.ui.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.chinalwb.are.AREditText
import com.chinalwb.are.styles.toolbar.ARE_ToolbarDefault
import com.chinalwb.are.styles.toolbar.IARE_Toolbar
import com.chinalwb.are.styles.toolitems.*


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uz.gita.notes.MainActivity
import uz.gita.notes.R
import uz.gita.notes.database.Note
import uz.gita.notes.database.NoteDatabase
import uz.gita.notes.databinding.ActivityNewNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class EditNoteActivity : AppCompatActivity() {
    private lateinit var value: String
    private lateinit var binding: ActivityNewNoteBinding
    private lateinit var noteViewModel: NoteViewModel
    private var editNote: Note? = null
 private lateinit var mToolbar: IARE_Toolbar
    private lateinit var mEditText: AREditText
    private var scrollerAtEnd = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteViewModel = ViewModelProvider(this,  NoteViewModelFactory(NoteDatabase.getDataBase()))[NoteViewModel::class.java]
        val b = intent.extras
        value = "" // or other values

        if (b != null)
            value = b.getString("key").toString()

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_new_note
        )
        binding.lifecycleOwner = this
        Log.i("Tag", value)
        sendNoteSelected(value)



// Handle the cancel button
        binding.btnCancel.setOnClickListener {
            editNote?.let { it1 -> startShowActivity(it1.noteId) }
        }

        binding.btnOK.setOnClickListener {
            // Create a new note
            val newNote = editNote?.let { it1 -> Note(it1.noteId) }

            // Set its properties to match the
            // user's entries on the form
            newNote?.title = binding.editTitle.text.toString()

            newNote?.description = binding.editDescription.text.toString()
//
//            newNote?.idea = binding.checkBoxIdea.isChecked
//            newNote?.todo = binding.checkBoxTodo.isChecked
//            newNote?.important = binding.checkBoxImportant.isChecked

            newNote?.let { it1 ->
                noteViewModel.update(it1)
                // Quit the dialog
                startShowActivity(it1.noteId)
            }




        }
        apply {
            mToolbar = findViewById(R.id.areToolbar)
            val bold: IARE_ToolItem = ARE_ToolItem_Bold()
            val italic: IARE_ToolItem = ARE_ToolItem_Italic()
            val underline: IARE_ToolItem = ARE_ToolItem_Underline()
            val strikethrough: IARE_ToolItem = ARE_ToolItem_Strikethrough()
            val quote: IARE_ToolItem = ARE_ToolItem_Quote()
            val listNumber: IARE_ToolItem = ARE_ToolItem_ListNumber()
            val listBullet: IARE_ToolItem = ARE_ToolItem_ListBullet()
            val hr: IARE_ToolItem = ARE_ToolItem_Hr()
            val link: IARE_ToolItem = ARE_ToolItem_Link()
            val subscript: IARE_ToolItem = ARE_ToolItem_Subscript()
            val superscript: IARE_ToolItem = ARE_ToolItem_Superscript()
            val left: IARE_ToolItem = ARE_ToolItem_AlignmentLeft()
            val center: IARE_ToolItem = ARE_ToolItem_AlignmentCenter()
            val right: IARE_ToolItem = ARE_ToolItem_AlignmentRight()
            val image: IARE_ToolItem = ARE_ToolItem_Image()
            val video: IARE_ToolItem = ARE_ToolItem_Video()
            val at: IARE_ToolItem = ARE_ToolItem_At()
            mToolbar.addToolbarItem(bold)
            mToolbar.addToolbarItem(italic)
            mToolbar.addToolbarItem(underline)
            mToolbar.addToolbarItem(strikethrough)
            mToolbar.addToolbarItem(quote)
            mToolbar.addToolbarItem(listNumber)
            mToolbar.addToolbarItem(listBullet)
            mToolbar.addToolbarItem(hr)
            mToolbar.addToolbarItem(link)
            mToolbar.addToolbarItem(subscript)
            mToolbar.addToolbarItem(superscript)
            mToolbar.addToolbarItem(left)
            mToolbar.addToolbarItem(center)
            mToolbar.addToolbarItem(right)
            mToolbar.addToolbarItem(image)
            mToolbar.addToolbarItem(video)
            mToolbar.addToolbarItem(at)
            mEditText = findViewById(R.id.editDescription)
//        mEditText = findViewById(R.id.editTitle)
            mEditText.setToolbar(binding.areToolbar)
            val imageView: ImageView = findViewById(R.id.arrow)
            if (binding.areToolbar is ARE_ToolbarDefault) {
                (binding.areToolbar as ARE_ToolbarDefault).viewTreeObserver.addOnScrollChangedListener {
                    val scrollX = (binding.areToolbar as ARE_ToolbarDefault).scrollX
                    val scrollWidth = (binding.areToolbar as ARE_ToolbarDefault).width
                    val fullWidth = (binding.areToolbar as ARE_ToolbarDefault).getChildAt(0).width
                    scrollerAtEnd = if (scrollX + scrollWidth < fullWidth) {
                        imageView.setImageResource(R.drawable.ic_baseline_arrow_forward_24)
                        false
                    } else {
                        imageView.setImageResource(R.drawable.ic_baseline_arrow_back_24)
                        true
                    }
                }
            }
            imageView.setOnClickListener {
                scrollerAtEnd = if (scrollerAtEnd) {
                    (binding.areToolbar as ARE_ToolbarDefault?)!!.smoothScrollBy(-Int.MAX_VALUE, 0)
                    false
                } else {
                    val hsWidth = (binding.areToolbar as ARE_ToolbarDefault?)!!.getChildAt(0).width
                    (binding.areToolbar as ARE_ToolbarDefault?)!!.smoothScrollBy(hsWidth, 0)
                    true
                }
            }

        }

    }

    private fun startShowActivity(noteToShow: String) {
        val intent = Intent(this, MainActivity::class.java)
        val b = Bundle()
        b.putString("key", noteToShow) //Your id

        intent.putExtras(b) //Put your id to your next Intent

        startActivity(intent)
//        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }
    // Receive a note from the MainActivity class
    private fun sendNoteSelected(value: String) {
        lifecycleScope.launch(Dispatchers.IO){
            Log.i("Tag","note${noteViewModel.getNoteWithID(value)}")
            noteViewModel.getNoteWithID(value)?.let { setNote(it) }
        }

    }

    private fun setNote(note: Note) {
        editNote = note
        Log.i("Tag", editNote?.description.toString())

        editNote?.noteId.let { it1 ->
            if (it1 != null) {
                val outputDataFormat= SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val calendar: Calendar = Calendar.getInstance()
                calendar.timeInMillis = it1.toLong()
                binding.tvDateTime.text =outputDataFormat.format(calendar.time)
                binding.editTitle.setText(editNote?.title)
                binding.editDescription.setText(editNote?.description)
//                binding.checkBoxIdea.isChecked = editNote?.idea == true
//                binding.checkBoxTodo.isChecked = editNote?.todo == true
//                binding.checkBoxImportant.isChecked = editNote?.important == true

            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
//        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }

}