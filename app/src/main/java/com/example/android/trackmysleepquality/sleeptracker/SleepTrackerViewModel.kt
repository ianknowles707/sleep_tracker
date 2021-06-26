/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private var tonight = MutableLiveData<SleepNight?>()

    private val nights = database.getAllNights()

    val nightString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    //Variable to be used for navigation. The value is set by the viewmodel and
    //observed in teh Fragment. It is of type SleepNight so we can use the current
    //value to pass in the key of the current night, which can then be passed
    //using safe args to the quality fragment where it can be updated
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    init {
        initializeTonight()
    }

    private fun initializeTonight() {

        viewModelScope.launch {
            tonight.value = getTonightFromDatabase()

        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {

        var night = database.getTonight()
        if (night?.startTimeMilli != night?.endTimeMillie) {
            night = null
        }
        return night
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight()
            insertNight(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insertNight(newNight: SleepNight) {
        database.insert(newNight)
    }

    fun onStopTracking() {
        viewModelScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMillie = System.currentTimeMillis()
            updateNight(oldNight)
            //Set the navigation variable to be the valuie of tha last night
            //so we can use the key in the safe args call
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun updateNight(oldNight: SleepNight) {
        database.update(oldNight)
    }

    fun onClear() {
        viewModelScope.launch {
            clearNights()
        }
    }

    private suspend fun clearNights() {
        database.clear()
    }

    //Set value to null to indicate we are finished with the navigation
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }
}

