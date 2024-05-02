package com.example.carebridge.activities.seniorcitizen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.carebridge.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SeniorBookVolunteerActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SeniorBookVolunteerActivity::class.java)

    @Test
    fun testSelectDate() {
        // Click on the calendar icon to open the date picker dialog
        onView(withId(R.id.calendarIcon)).perform(click())
    }

    @Test
    fun testViewAvailableVolunteers() {

        onView(withId(R.id.timePeriodSpinner)).perform(click())
        onView(withText("10:00-11:00")).perform(click())

        // Check if the RecyclerView is displayed and contains volunteers
        onView(withId(R.id.seniorvolunteerRecyclerView)).check(matches(isDisplayed()))
    }
}
