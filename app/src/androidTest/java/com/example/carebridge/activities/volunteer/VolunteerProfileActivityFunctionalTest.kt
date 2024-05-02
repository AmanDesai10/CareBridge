package com.example.carebridge.activities.volunteer

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carebridge.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VolunteerProfileActivityFunctionalTest {

    private lateinit var scenario: ActivityScenario<VolunteerProfileActivity>

    @Before
    fun setUp() {
        // Launch the activity under test
        scenario = ActivityScenario.launch(VolunteerProfileActivity::class.java)
    }

    @After
    fun tearDown() {
        // Finish the activity after each test
        scenario.close()
    }

    @Test
    fun testUIElementsDisplayed() {
        // Check if the UI elements are displayed correctly
        Espresso.onView(ViewMatchers.withId(R.id.headerTV)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.usernameTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.emailTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.phoneNumberTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.nameTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.ageTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.genderTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.addressTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    // Add more UI test cases for interactions, validations, etc.
}
