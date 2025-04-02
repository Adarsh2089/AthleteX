package com.example.athletex.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.athletex.data.database.AppRepository
import com.example.athletex.data.plan.Plan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository = AppRepository(application)

    /** Insert a plan */
    suspend fun insert(plan: Plan) = withContext(Dispatchers.IO) {
        repository.insertPlan(plan)
    }

    /** Update a plan */
    suspend fun update(plan: Plan) = withContext(Dispatchers.IO) {
        repository.updatePlan(plan)
    }

    /** Update only complete state and timeComplete of a plan */
    suspend fun updateComplete(completedState: Boolean, timeComplete: Long?, planId: Int) =
        withContext(Dispatchers.IO) {
            repository.updateCompleted(completedState, timeComplete, planId)
        }

    /** Delete a plan */
    suspend fun deletePlan(planId: Int) = withContext(Dispatchers.IO) {
        repository.deletePlan(planId)
    }
}