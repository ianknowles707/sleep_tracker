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

package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

//Annotation tells the system this is a database access object
@Dao
interface SleepDatabaseDao {

    //Default actions are already provided - Insert is one of them
    @Insert
    suspend fun insert(night: SleepNight)

    @Update
    suspend fun update(night: SleepNight)

    //For everything else the Query function is required. Note that at first use the
    //table may be empty so this must return a nullable
    @Query("SELECT * FROM daily_sleep_quality_table WHERE nightId = :key")
    suspend fun get(key: Long): SleepNight?

    //Delete is provided which deletes a single row. There are ways to use this to clear
    //the table but we would need to know something about the table itself. WIth no knowledge
    //of the contents we can run a Query action which deletes all rows
    @Query("DELETE FROM daily_sleep_quality_table")
    suspend fun clear()

    //Room can return the results as LiveData. We can then se ta b observer on the data
    //and update the UI when date changes
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights(): LiveData<List<SleepNight>>

    //LIMIT is used to restrict number of rows returned, here we just want the last one. Again
    //needs to be nullable for first use
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    suspend fun getTonight(): SleepNight?
}