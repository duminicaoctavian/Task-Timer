package com.octavian.tasktimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
//data class Task(val name: String, val description: String, val sortOrder: Int) : Parcelable {
class Task(val name: String, val description: String, val sortOrder: Int) : Parcelable {
    var id: Long = 0
}