package com.example.carebridge.activities.familyfriendsview

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import com.example.carebridge.R
import com.example.carebridge.activities.familyandfriends.ExistingSeniorCitizenView
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExistingSeniorCitizenActivityFunctionalTest {
    private lateinit var scenario: ActivityScenario<ExistingSeniorCitizenView>

    @Before
    fun setUp() {
        // Launch the activity under test
        scenario = ActivityScenario.launch(ExistingSeniorCitizenView::class.java)
    }

    @After
    fun tearDown() {
        // Finish the activity after each test
        scenario.close()
    }

    @Test
    fun testSearchViewClick() {
        // Find the SearchView within a LinearLayout and click it
        onView(
            allOf(
                withId(R.id.search_bar),
                withParent(withId(R.id.existingSeniorCitizen))
            )
        ) // Replace with your LinearLayout's ID
            .check(ViewAssertions.matches(isDisplayed()))
            .perform(click())
    }
}