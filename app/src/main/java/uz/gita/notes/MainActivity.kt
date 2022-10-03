

package uz.gita.notes


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

import uz.gita.notes.commons.Constants
import uz.gita.notes.database.DeletedNoteDatabase
import uz.gita.notes.database.Note
import uz.gita.notes.database.NoteDatabase
import uz.gita.notes.databinding.ActivityMainBinding

import uz.gita.notes.ui.DeletedNotesActivity
import uz.gita.notes.ui.DeletedNotesViewModel
import uz.gita.notes.ui.DeletedNotesViewModelFactory
import uz.gita.notes.ui.screens.*
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NoteAdapter.ItemListener {

    private val adapter: NoteAdapter by lazy {
        NoteAdapter(this)
    }
    private lateinit var preferencesPrivate: SharedPreferences
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var deletedNoteViewModel: DeletedNotesViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.appBarMain.toolbar)
        preferencesPrivate = this.getSharedPreferences(
            this.packageName + "_private_preferences",
            Context.MODE_PRIVATE
        )


        val header = binding.navView.getHeaderView(0)

        noteViewModel = ViewModelProvider(this,  NoteViewModelFactory(NoteDatabase.getDataBase()))[NoteViewModel::class.java]
        deletedNoteViewModel = ViewModelProvider(this,
            DeletedNotesViewModelFactory(DeletedNoteDatabase.getDataBase())
        )[DeletedNotesViewModel::class.java]



        binding.appBarMain.fab.setOnClickListener {
            val intent =  Intent(this, NewNoteActivity::class.java)
            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }


        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBarMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        AppRater.appLaunched(this);

        binding.appBarMain.recyclerViewLayout.recyclerView.adapter = adapter
        if(firstRun()&&adapter.itemCount>0){
            Snackbar.make(binding.appBarMain.coordinatorLayout,getString(R.string.swipe_to_delete),Snackbar.LENGTH_LONG).show()
            finishFirstRun()
        }
        when {
            isNewFirst() -> {
                noteViewModel.new()
            }
            isOldFirst()->{
                noteViewModel.old()

            }
            isTitleFirst()->{
                noteViewModel.title()
            }
        }

        noteViewModel.mNew.observe(this){it->
            if(it == true){

                preferencesPrivate.edit().putBoolean(Constants.new, true).apply()
                notOldFirst()
                notTitleFirst()

                noteViewModel.getAllNotesByIdNew().observe(this) { lisOfNotes ->
                    lisOfNotes?.let {
                        adapter.setNote(it)
                    }
                }
                binding.appBarMain.recyclerViewLayout.recyclerView.smoothSnapToPosition(0)
            }
        }
        noteViewModel.mOld.observe(this){it->
            if(it == true){

                preferencesPrivate.edit().putBoolean(Constants.old, true).apply()
                notNewFirst()
                notTitleFirst()

                noteViewModel.getAllNotesByIdOld().observe(this) { lisOfNotes ->
                    lisOfNotes?.let {
                        adapter.setNote(it)
                    }
                }
                binding.appBarMain.recyclerViewLayout.recyclerView.smoothSnapToPosition(0)

            }
        }
        noteViewModel.mTitle.observe(this){it->
            if(it == true){

                preferencesPrivate.edit().putBoolean(Constants.title, true).apply()
                notOldFirst()
                notNewFirst()

                noteViewModel.getAllNotesByTitle().observe(this) { lisOfNotes ->
                    lisOfNotes?.let {
                        adapter.setNote(it)
                    }
                }
                binding.appBarMain.recyclerViewLayout.recyclerView.smoothSnapToPosition(0)
            }
        }
        adapter.registerAdapterDataObserver( object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                binding.appBarMain.recyclerViewLayout.recyclerView.smoothSnapToPosition(0)
            }
        })


        val swipeGesture = object : SwipeGesture(this) {

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        val position = viewHolder.adapterPosition
                        val deletedItem = adapter.getItem(position)
                        deleteFromDatabase(deletedItem)
                        binding.appBarMain.recyclerViewLayout.recyclerView.clearOnScrollListeners()

                        Snackbar.make(
                            binding.appBarMain.coordinatorLayout,
                            deletedItem.title.toString() + getString(R.string.is_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                // adding on click listener to our action of snack bar.
                                // below line is to add our item to array list with a position.
                                insertToDatabase(deletedItem)

                            }.addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    super.onDismissed(transientBottomBar, event)
                                    if (event != DISMISS_EVENT_ACTION) {
                                        // Snackbar closed on its own
                                        //deleteFromDatabase(deletedItem)
                                        createDeletedNotesDatabase(deletedItem)

                                    }
                                }

                                override fun onShown(sb: Snackbar?) {
                                    super.onShown(sb)

                                }
                            }).show()


                    }
                }

            }

        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.appBarMain.recyclerViewLayout.recyclerView)

        binding.appBarMain.recyclerViewLayout.recyclerView


        displayScreen(-1)

    }

    private fun insertToDatabase(deletedNote: Note) {
        noteViewModel.insert(deletedNote)
    }

    private fun deleteFromDatabase(deletedNote: Note) {
        noteViewModel.deleteById(deletedNote.noteId)
    }

    private fun createDeletedNotesDatabase(deletedNote: Note) {
        deletedNoteViewModel.insert(deletedNote)
    }


    private fun showNote(noteToShow: Int) {
        Log.i("Tag","$noteToShow")
        val intent = Intent(this, EditNoteActivity::class.java)
        val b = Bundle()
        b.putString("key", adapter.getItem(noteToShow).noteId) //Your id
        adapter.getItem(noteToShow).title?.let { Log.i("Tag", it) }
        intent.putExtras(b) //Put your id to your next Intent

        startActivity(intent)
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val menuItem = menu.findItem(R.id.search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.type_ur_search)
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                val tempArr = ArrayList<Note>()

                noteViewModel.getAllNotesByIdNew().observe(this@MainActivity){ listOfNotes ->
                    for (arr in listOfNotes) {
                        if (arr.title!!.toLowerCase(Locale.getDefault()).contains(newText.toString())||arr.description!!.toLowerCase(Locale.getDefault()).contains(newText.toString())) {
                            tempArr.add(arr)
                            adapter.setNote(tempArr)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                return true
            }

        })

        MenuItemCompat.setOnActionExpandListener(
            menuItem,
            object : MenuItemCompat.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    binding.appBarMain.fab.isVisible = false
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    binding.appBarMain.fab.isVisible = true
                    return true
                }
            })

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        when {
            isNewFirst() -> {
                if (menu != null) {
                    menu.findItem(R.id.mNewFirst).isChecked = true
                }

            }
            isOldFirst()->{
                if (menu != null) {
                    menu.findItem(R.id.mOldFirst).isChecked = true
                }

            }
            isTitleFirst()->{
                if (menu != null) {
                    menu.findItem(R.id.mTitle).isChecked = true
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {

            R.id.mNewFirst -> {
                item.isChecked = !item.isChecked

                noteViewModel.new()
                preferencesPrivate.edit().putBoolean(Constants.new, true).apply()
                notOldFirst()
                notTitleFirst()
                return true
            }
            R.id.mOldFirst -> {
                item.isChecked = !item.isChecked

                noteViewModel.old()
                preferencesPrivate.edit().putBoolean(Constants.old, true).apply()
                notNewFirst()
                notTitleFirst()

                return true
            }
            R.id.mTitle -> {
                item.isChecked = !item.isChecked

                noteViewModel.title()
                preferencesPrivate.edit().putBoolean(Constants.title, true).apply()
                notOldFirst()
                notNewFirst()
                return true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun displayScreen(id: Int) {

        // val fragment =  when (id){

        when (id) {
            R.id.deleted_notes -> {
                //supportFragmentManager.beginTransaction().replace(R.id.fragmentHolder, DeletedNotes(deletedNotesList)).commit()
                startActivity(Intent(this, DeletedNotesActivity::class.java))
            }

            R.id.settings ->{
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, "Hey Check out this Great app:")
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, "Share To:"))
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        displayScreen(item.itemId)

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    override fun onClick(view: View, itemPosition: Int) {
        showNote(itemPosition)
    }


    private fun firstRun() = preferencesPrivate.getBoolean(Constants.FIRST_RUN, true)

    private fun finishFirstRun() =
        preferencesPrivate.edit().putBoolean(Constants.FIRST_RUN, false).apply()
    private fun isTitleFirst() = preferencesPrivate.getBoolean(Constants.title,false)
    private fun isOldFirst() = preferencesPrivate.getBoolean(Constants.old,true)
    private fun isNewFirst() = preferencesPrivate.getBoolean(Constants.new,false)

    private fun notTitleFirst() = preferencesPrivate.edit().putBoolean(Constants.title,false).apply()
    private fun notOldFirst() = preferencesPrivate.edit().putBoolean(Constants.old,false).apply()
    private fun notNewFirst() = preferencesPrivate.edit().putBoolean(Constants.new,false).apply()

}