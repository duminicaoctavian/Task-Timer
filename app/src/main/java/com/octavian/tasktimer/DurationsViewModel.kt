package com.octavian.tasktimer

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "DurationsViewModel"

enum class SortColumns {
    NAME,
    DESCRIPTION,
    START_DATE,
    DURATION
}

class DurationsViewModel(application: Application): AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange: called. uri is $uri")
            loadData()
        }
    }

    private val calendar = GregorianCalendar()

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    var sortOrder = SortColumns.NAME
        set(order) {
            if (field != order) {
                field = order
                loadData()
            }
        }

    private val selection = "${DurationsContract.Columns.START_TIME} Between ? AND ?"
    private var selectionArgs = emptyArray<String>()

    private var _displayWeek = true
    val displayWeek: Boolean
        get() = _displayWeek

    init {
        application.contentResolver.registerContentObserver(TimingsContract.CONTENT_URI, true, contentObserver)
        applyFilter()
    }

    fun toggleDisplayWeek() {
        _displayWeek = !_displayWeek
        applyFilter()
    }

    fun getFilterDate(): Date {
        return calendar.time
    }

    fun setReportDate(year: Int, month: Int, dayOfMonth: Int) {
        // check if the date has changed
        if (calendar.get(GregorianCalendar.YEAR) != year
            || calendar.get(GregorianCalendar.MONTH) != month
            || calendar.get(GregorianCalendar.DAY_OF_MONTH) != dayOfMonth) {

            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            applyFilter()
        }
    }

    private fun applyFilter() {
        Log.d(TAG, "Entering applyFilter")

        val currentCalendarDate = calendar.timeInMillis // store the time, so we can put it back

        if (displayWeek) {
            // show records for the entire week

            // we have a date, so find out which day of the week it is
            val weekStart = calendar.firstDayOfWeek
            Log.d(TAG, "applyFilter: first day of calendar week is $weekStart")
            Log.d(TAG, "applyFilter: dayOfWeek is ${calendar.get(GregorianCalendar.DAY_OF_WEEK)}")
            Log.d(TAG, "applyFilter: date is " + calendar.time)

            // calculate week start and end dates
            calendar.set(GregorianCalendar.DAY_OF_WEEK, weekStart)
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0) // Note: HOUR_OF_DAY, not HOUR
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)
            val startDate = calendar.timeInMillis / 1000

            // move forward 6 days to get to the last day of the week
            calendar.add(GregorianCalendar.DATE, 6)
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 23)
            calendar.set(GregorianCalendar.MINUTE, 59)
            calendar.set(GregorianCalendar.SECOND, 59)
            val endDate = calendar.timeInMillis / 1000

            selectionArgs = arrayOf(startDate.toString(), endDate.toString())
            Log.d(TAG, "In applyFilter(7), Start Date is $startDate, End Date is $endDate")
        } else {
            // re-query for the current day
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)
            val startDate = calendar.timeInMillis / 1000

            calendar.set(GregorianCalendar.HOUR_OF_DAY, 23)
            calendar.set(GregorianCalendar.MINUTE, 59)
            calendar.set(GregorianCalendar.SECOND, 59)
            val endDate = calendar.timeInMillis / 1000

            selectionArgs = arrayOf(startDate.toString(), endDate.toString())
            Log.d(TAG, "In applyFilter(1), Start Date is $startDate, End Date is $endDate")
        }

        // put the calendar back to where it was before we started jumping back and forth
        calendar.timeInMillis = currentCalendarDate

        loadData()
    }

    private fun loadData() {
        val order = when (sortOrder) {
            SortColumns.NAME -> DurationsContract.Columns.NAME
            SortColumns.DESCRIPTION -> DurationsContract.Columns.DESCRIPTION
            SortColumns.START_DATE -> DurationsContract.Columns.START_TIME
            SortColumns.DURATION -> DurationsContract.Columns.DURATION
        }
        Log.d(TAG, "order is $order")

        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                DurationsContract.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                order
            )
            databaseCursor.postValue(cursor)
        }
    }

    fun deleteRecords(timeInMilliseconds: Long) {
        // clear all records from Timings table prior to the date selected
        Log.d(TAG, "Entering deleteRecords")

        val longDate = timeInMilliseconds / 1000
        val selectionArgs = arrayOf(longDate.toString())
        val selection = "${TimingsContract.Columns.TIMING_START_TIME} < ?"

        Log.d(TAG, "Deleting records prior to $longDate")

        GlobalScope.launch {
            getApplication<Application>().contentResolver.delete(TimingsContract.CONTENT_URI, selection, selectionArgs)
        }

        Log.d(TAG, "Exiting deleteRecords")
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: called")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}