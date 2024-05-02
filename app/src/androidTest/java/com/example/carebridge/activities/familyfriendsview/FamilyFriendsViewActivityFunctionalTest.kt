package com.example.carebridge.activities.familyfriendsview

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carebridge.R
import com.example.carebridge.activities.familyandfriends.FamilyFriendsView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FamilyFriendsViewActivityFunctionalTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(FamilyFriendsView::class.java)

    @Test
    fun checkAllUIElementsDisplayed() {
        onView(withId(R.id.textView)).check(matches(isDisplayed()))
        onView(withId(R.id.familyMemberRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.addAccBtn)).check(matches(isDisplayed()))
        onView(withId(R.id.linkAccBtn)).check(matches(isDisplayed()))
    }
}
