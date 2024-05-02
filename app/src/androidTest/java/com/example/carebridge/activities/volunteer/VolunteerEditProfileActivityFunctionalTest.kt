package com.example.carebridge.activities.volunteer

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carebridge.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VolunteerEditProfileActivityFunctionalTest {

    private lateinit var scenario: ActivityScenario<VolunteerEditProfileActivity>

    @Before
    fun setUp() {
        // Launch the activity under test
        scenario = ActivityScenario.launch(VolunteerEditProfileActivity::class.java)
    }

    @After
    fun tearDown() {
        // Finish the activity after each test
        scenario.close()
    }

    @Test
    fun testEditTextFieldsDisplayed() {
        // Check if EditText fields are displayed
        onView(withId(R.id.usernameEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.emailEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.phoneNumberEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.nameEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.ageEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.genderEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.addressEditText)).check(matches(isDisplayed()))
    }

    @Test
    fun testSaveButtonClicked() {
        // Type text into EditText fields
        onView(withId(R.id.usernameEditText)).perform(replaceText("NewUsername"))
        onView(withId(R.id.emailEditText)).perform(replaceText("newemail@example.com"))
        onView(withId(R.id.phoneNumberEditText)).perform(replaceText("1234567890"))
        onView(withId(R.id.nameEditText)).perform(replaceText("NewName"))
        onView(withId(R.id.ageEditText)).perform(replaceText("30"))
        onView(withId(R.id.genderEditText)).perform(replaceText("Male"))
        onView(withId(R.id.addressEditText)).perform(replaceText("New Address"))

        // Close soft keyboard to prevent issues with animations
        closeSoftKeyboard()
        Thread.sleep(1000)
        // Click the save button
        onView(withId(R.id.saveButton)).perform(scrollTo(),click())

    }
}
