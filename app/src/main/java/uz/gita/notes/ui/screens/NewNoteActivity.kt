package uz.gita.notes.ui.screens

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chinalwb.are.AREditText
import com.chinalwb.are.styles.toolbar.ARE_ToolbarDefault
import com.chinalwb.are.styles.toolbar.IARE_Toolbar
import com.chinalwb.are.styles.toolitems.*

import com.google.android.material.snackbar.Snackbar

import uz.gita.notes.MainActivity
import uz.gita.notes.R
import uz.gita.notes.commons.Dialog
import uz.gita.notes.database.Note
import uz.gita.notes.database.NoteDatabase
import uz.gita.notes.databinding.ActivityNewNoteBinding
import java.text.SimpleDateFormat
import java.util.*


class NewNoteActivity : AppCompatActivity() {

  private lateinit var mToolbar: IARE_Toolbar
    private lateinit var mEditText: AREditText
    private var scrollerAtEnd = false
    //    private lateinit var uid: String
//    private lateinit var user: FirebaseUser
//    private lateinit var myRef: DatabaseReference
//    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityNewNoteBinding
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



//            btnBack = findViewById(R.id.btnBackFromAdd)
//            btnSave = findViewById(R.id.btnSave)
//            titleInput = findViewById(R.id.text_input)

        noteViewModel = ViewModelProvider(
            this,
            NoteViewModelFactory(NoteDatabase.getDataBase())
        )[NoteViewModel::class.java]

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_new_note
        )
        binding.lifecycleOwner = this
        val currentTime = System.currentTimeMillis()
        val outputDataFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        binding.tvDateTime.text = outputDataFormat.format(calendar.time)
        // Handle the cancel button
        binding.btnCancel.setOnClickListener {
            startMainActivity()
        }



        binding.btnOK.setOnClickListener {
            // Create a new note
            val newNote = Note(currentTime.toString())

            // Set its properties to match the
            // user's entries on the form
            when {
                TextUtils.isEmpty(binding.editTitle.text.toString()) -> {
                    binding.editTitle.error = getString(R.string.enter_title)
                }
                TextUtils.isEmpty(binding.editDescription.text.toString()) -> {
                    binding.editDescription.error = getString(R.string.enter_description)
                }
//                !binding.checkBoxIdea.isChecked && !binding.checkBoxTodo.isChecked && !binding.checkBoxImportant.isChecked -> {
//                    Snackbar.make(
//                        binding.root,
//                        getString(R.string.select_note_type),
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                }
                else -> {
                    newNote.title = binding.editTitle.text.toString()

                    newNote.description = binding.editDescription.text.toString()

//                    newNote.idea = binding.checkBoxIdea.isChecked
//                    newNote.todo = binding.checkBoxTodo.isChecked
//                    newNote.important = binding.checkBoxImportant.isChecked


                    noteViewModel.insert(newNote, onDoneFunction = {
                        startMainActivity()
                    })
                }
            }
        }
        initObjects()
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
//            mEditText = findViewById(R.id.editTitle)
            mEditText.setToolbar(binding.areToolbar)
            val imageView: ImageView = findViewById(R.id.arrow)
            if (true) {
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


    var progressDialog: AlertDialog? = null
    private fun initObjects() {
        noteViewModel.progress.observe(this) {
            if (it == true) {
                progressDialog = Dialog.progress().cancelable(false).show(this)
            } else {
                progressDialog?.dismiss()
            }
        }

        noteViewModel.errorMessage.observe(this) {
            if (it?.isNotEmpty() == true)
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
//        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }
}