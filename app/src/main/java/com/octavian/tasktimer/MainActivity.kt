package com.octavian.tasktimer

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.octavian.tasktimer.debug.TestData
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(),
    AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit,
    AppDialog.DialogEvents {

    // Whether or not the activity is in 2-pane mode
    // i.e running in landscape, or on tablet
    private var mTwoPane = false

    // module scope because we need to dismiss it in onStop (e.g. when orientation changes) to avoid memory leaks.
    private var aboutDialog: AlertDialog? = null

    private val viewModel by lazy { ViewModelProviders.of(this).get(TaskTimerViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG, "onCreate: twoPane is $mTwoPane")

        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            // There was an existing fragment to edit a task, make sure the panes are set correctly
            showEditPane()
        } else {
            task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

        viewModel.timing.observe(this, Observer<String> { timing ->
            current_task.text = if (timing != null) {
                getString(R.string.timing_message, timing)
            } else {
                getString(R.string.no_task_message)
            }
        })
        Log.d(TAG, "onCreate: finished")
    }

    private fun showEditPane() {
        task_details_container.visibility = View.VISIBLE
        // hide the left hand pane, if in single pane view
        mainFragment.view?.visibility = if(mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane: called")
        if (fragment != null) {
//            supportFragmentManager.beginTransaction()
//                .remove(fragment)
//                .commit()
            removeFragment(fragment)
        }

        // Set the visibility of the right hand pane
        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        removeEditPane(findFragmentById(R.id.task_details_container))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (BuildConfig.DEBUG) {
            val generate = menu.findItem(R.id.menumain_generate)
            generate.isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up addedit_save, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
            R.id.menumain_showDurations -> startActivity(Intent(this, DurationsReport::class.java))
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            R.id.menumain_showAbout -> showAboutDialog()
            R.id.menumain_generate -> TestData.generateTestData(contentResolver)
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = findFragmentById(R.id.task_details_container)
//                removeEditPane(fragment)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                            getString(R.string.cancelEditDiag_message),
                            R.string.cancelEditDiag_positive_caption,
                            R.string.cancelEditDiag_negative_caption)
                } else {
                    removeEditPane(fragment)
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            Log.d(TAG, "onClick: Entering messageView:onClick")
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)

        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME

        // Use a nullable type: the TextView won't exist on API 21 and higher
        val aboutUrl: TextView? = messageView.findViewById(R.id.about_url)
        aboutUrl?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val s = (it as TextView).text.toString()
            intent.data = Uri.parse(s)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@MainActivity, R.string.about_url_error, Toast.LENGTH_LONG).show()
            }
        }

        aboutDialog?.show()
    }

//    private fun showAboutDialog() {
//        val messageView = layoutInflater.inflate(R.layout.about, null, false)
//        val builder = AlertDialog.Builder(this)
//
//        builder.setTitle(R.string.app_name)
//        builder.setIcon(R.mipmap.ic_launcher)
//
//        aboutDialog = builder.setView(messageView).create()
//        aboutDialog?.setCanceledOnTouchOutside(true)
//
//        messageView.setOnClickListener {
//            Log.d(TAG, "Entering messageView.onClick")
//            if (aboutDialog != null && aboutDialog?.isShowing == true) {
//                aboutDialog?.dismiss()
//            }
//        }
//
//        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
//        aboutVersion.text = BuildConfig.VERSION_NAME
//
//        aboutDialog?.show()
//    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        // Create a new fragment to edit the task
        val newFragment = AddEditFragment.newInstance(task)
        replaceFragment(newFragment, R.id.task_details_container)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.task_details_container, newFragment)
//            .commit()

        showEditPane()

        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onBackPressed() {
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
//            removeEditPane(fragment)
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negative_caption)
            } else {
                removeEditPane(fragment)
            }
        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with dialogId $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            val fragment = findFragmentById(R.id.task_details_container)
            removeEditPane(fragment)
        }
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState: called")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onResume() {
        Log.d(TAG, "onResume: called")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: called")
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState: called")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()

        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }
}

//    //        testInsert()
////        testUpdate()
////        testUpdateTwo()
////        testDelete()
////        testDeleteTwo()
//
////        val appDatabase = AppDatabase.getInstance(this)
////        val db = appDatabase.readableDatabase
////        val cursor = db.rawQuery("SELECT * FROM Tasks", null)
//
//    val projection = arrayOf(TasksContract.Columns.TASK_NAME, TasksContract.Columns.TASK_SORT_ORDER)
//    val sortColumn = TasksContract.Columns.TASK_SORT_ORDER
//
//    //        val cursor = contentResolver.query(TasksContract.buildUriFromId(2), projection, null, null, sortColumn)
//    val cursor = contentResolver.query(TasksContract.CONTENT_URI, null, null, null, sortColumn)
//    Log.d(TAG, "*****************************")
//    cursor?.use {
//        while(it.moveToNext()) {
//            with(it) {
//                val id = getLong(0)
//                val name = getString(1)
//                val description = getString(2)
//                val sortOrder = getString(3)
//                val result = "ID: $id. Name: $name. Description: $description. Sort order: $sortOrder"
//                android.util.Log.d(TAG, "onCreate: reading data $result")
//            }
//        }
//    }
//
//    Log.d(TAG, "*****************************")
//}
//
//private fun testUpdateTwo() {
//    val values = ContentValues().apply {
//        put(TasksContract.Columns.TASK_SORT_ORDER, 99)
//        put(TasksContract.Columns.TASK_DESCRIPTION, "For deletion")
//    }
//
//    val selection = TasksContract.Columns.TASK_SORT_ORDER + " = ?"
//    val selectionArgs = arrayOf("99")
//
////        val taskUri = TasksContract.buildUriFromId(4)
//    val rowsAffected = contentResolver.update(TasksContract.CONTENT_URI, values, selection, selectionArgs)
//    Log.d(TAG, "Number of rows updated is $rowsAffected")
//}
//
//private fun testDeleteTwo() {
//
//    val selection = TasksContract.Columns.TASK_DESCRIPTION + " = ?"
//    val selectionArgs = arrayOf("For deletion")
//
//    val rowsAffected = contentResolver.delete(TasksContract.CONTENT_URI, selection, selectionArgs)
//    Log.d(TAG, "Number of rows deleted is $rowsAffected")
//}
//
//private fun testDelete() {
//
//    val taskUri = TasksContract.buildUriFromId(3)
//    val rowsAffected = contentResolver.delete(taskUri,null, null)
//    Log.d(TAG, "Number of rows deleted is $rowsAffected")
//}
//
//private fun testUpdate() {
//    val values = ContentValues().apply {
//        put(TasksContract.Columns.TASK_NAME, "Content Provider")
//        put(TasksContract.Columns.TASK_DESCRIPTION, "Record content providers videos")
//    }
//
//    val taskUri = TasksContract.buildUriFromId(4)
//    val rowsAffected = contentResolver.update(taskUri, values, null, null)
//    Log.d(TAG, "Number of rows updated is $rowsAffected")
//}
//
//private fun testInsert() {
//    val values = ContentValues().apply {
//        put(TasksContract.Columns.TASK_NAME, "New Task 1")
//        put(TasksContract.Columns.TASK_DESCRIPTION, "Description 1")
//        put(TasksContract.Columns.TASK_SORT_ORDER, 2)
//    }
//
//    val uri = contentResolver.insert(TasksContract.CONTENT_URI, values) ?: throw NullPointerException("testInsert: Uri cannot be null!")
//    Log.d(TAG, "New row id (in uri) is $uri")
//    Log.d(TAG, "id (in uri) is ${TasksContract.getId(uri)}")
//}