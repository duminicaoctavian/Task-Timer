package com.octavian.tasktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(),
    AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit {

    //Whether or not the activity is in 2-pane mode
    // i.e running in landscape, or on tablet
    private var mTwoPane = false

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG, "onCreate: twoPane is $mTwoPane")

        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            // There was an existing fragment to edit a task, make sure the panes are set correctly
            showEditPane()
        } else {
            task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

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
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        // Set the visibility of the right hand pane
        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditPane(fragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up addedit_save, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
//            R.id.menumain_settings -> true
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
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

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        // Create a new fragment to edit the task
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .replace(R.id.task_details_container, newFragment)
            .commit()

        showEditPane()

        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
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