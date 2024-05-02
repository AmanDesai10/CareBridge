package com.example.carebridge.activities.seniorcitizen

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.carebridge.R
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

@RunWith(AndroidJUnit4::class)
@LargeTest
class SeniorItemsListActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SeniorItemsListActivity::class.java)

    @Test
    fun testAddItem() {
        // Click on the add button to open the dialog
        onView(withId(R.id.addButton)).perform(click())

        // Type a new item name
        onView(withId(R.id.itemNameEditText)).perform(typeText("New Item"))

        // Click on the submit button to add the item
        onView(withId(R.id.submitButton)).perform(click())

        // Check if the new item is displayed in the RecyclerView
        onView(withText("New Item")).check(matches(isDisplayed()))
    }

    @Test
    fun testEditItem() {
        // Assuming the first item is "Old Item"
        onView(withText("Old Item")).perform(click())

        // Type a new item name
        onView(withId(R.id.itemNameEditText)).perform(typeText("Updated Item"))

        // Click on the submit button to edit the item
        onView(withId(R.id.submitButton)).perform(click())

        // Check if the item is updated in the RecyclerView
        onView(withText("Updated Item")).check(matches(isDisplayed()))
    }

    @Test
    fun testDeleteItem() {
        // Assuming the first item is "Old Item"
        onView(withText("Old Item")).perform(click())

        // Click on the delete button to delete the item
        onView(withId(R.id.deleteButton)).perform(click())

        // Check if the item is no longer displayed in the RecyclerView
        onView(withText("Old Item")).check(matches(isDisplayed()))
    }
}