package com.example.carebridge.activities.volunteer

import android.widget.CalendarView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carebridge.R
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VolunteerSlotAvailabilityActivityFunctionalTest {

    private lateinit var activityScenario: ActivityScenario<VolunteerSlotAvailabilityActivity>

    @Before
    fun setUp() {
        // Launch the activity under test
        activityScenario = ActivityScenario.launch(VolunteerSlotAvailabilityActivity::class.java)

    }

    private fun getActivity(): VolunteerSlotAvailabilityActivity? {
        var activity: VolunteerSlotAvailabilityActivity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


    @Test
    fun testSelectDateAndSlots() {
        // Click the CalendarView to select a date
        onView(allOf(withId(R.id.calendarView), isAssignableFrom(CalendarView::class.java)))
            .perform(click())

        // Select time slots
        onView(withId(R.id.timeSlotTextView1)).perform(click())
        onView(withId(R.id.timeSlotTextView3)).perform(click())
        onView(withId(R.id.timeSlotTextView5)).perform(click())

        // Click the save button
        onView(withId(R.id.button)).perform(click())
    }

    // Add more test cases as needed to cover other interactions and edge cases
}
