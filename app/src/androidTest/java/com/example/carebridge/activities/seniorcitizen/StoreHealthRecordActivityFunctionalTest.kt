package com.example.carebridge.activities.seniorcitizen

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.carebridge.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 * This test demonstrates a user interface test for the StoreHealthRecordActivity.
 * The test checks if the UI elements are displayed correctly and if the pick PDF button works as expected.
 * The test also checks if the delete button inside the first item works as expected.
 * The test assumes that the user will select a PDF file when the pick PDF button is clicked.
 * The test also assumes that the logged-in user has at least one health record and the role of the user is a senior citizen.
 */
@RunWith(AndroidJUnit4::class)
class StoreHealthRecordActivityFunctionalTest {

    private lateinit var scenario: ActivityScenario<StoreHealthRecordActivity>

    /**
     * Set up the test environment.
     * Launch the StoreHealthRecordActivity before each test.
     * This method is annotated with @Before, so it will be executed before each test.
     */
    @Before
    fun setUp() {
        // Launch the activity under test
        scenario = ActivityScenario.launch(StoreHealthRecordActivity::class.java)
    }

    /**
     * Tear down the test environment.
     * Finish the StoreHealthRecordActivity after each test.
     * This method is annotated with @After, so it will be executed after each test.
     */
    @After
    fun tearDown() {
        // Finish the activity after each test
        scenario.close()
    }

    /**
     * Test if the UI elements are displayed correctly.
     * This test checks if the UI elements are displayed correctly.
     */
    @Test
    fun testUIElementsDisplayed() {
        // Check if the UI elements are displayed correctly
        Espresso.onView(withId(R.id.pdfHealthRecordRecyclerView))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.pickPdfHealthRecordButton))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    /**
     * Test the delete button inside the first item.
     * This test checks if the delete button inside the first item works as expected.
     * The test will ask the user to select a PDF file if the health record list is empty.
     *  The test will then click the delete button inside the first item.
     */
    @Test
    fun testDeleteButton() {
        Thread.sleep(4000)
        // Scroll to the position of the item you want to test (assuming it's at position 0)
        Espresso.onView(withId(R.id.pdfHealthRecordRecyclerView))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))

        // get the item count
        var itemCount = 0
        Espresso.onView(withId(R.id.pdfHealthRecordRecyclerView)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(RecyclerView::class.java)
            }

            override fun getDescription(): String {
                return "Get the item count"
            }

            override fun perform(uiController: UiController?, view: View?) {
                val recyclerView = view as RecyclerView
                itemCount = recyclerView.adapter?.itemCount ?: 0
            }
        })

        if (itemCount == 0) {
            // Perform click on the pick PDF button
            Espresso.onView(withId(R.id.pickPdfHealthRecordButton)).perform(ViewActions.click())

            // Check if the health record list is displayed
            Espresso.onView(withId(R.id.pdfHealthRecordRecyclerView))
                .check(ViewAssertions.matches(isDisplayed()))

            // Give some time to the user to select a PDF file
            Thread.sleep(4000)
        }

        // Click the delete button inside the first item
        Espresso.onView(withId(R.id.pdfHealthRecordRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    MyViewAction.clickChildViewWithId(R.id.deleteIconImageView)
                )
            )
    }

    /**
     * Test the pick PDF button.
     * This test checks if the pick PDF button works as expected.
     * The test will ask the user to select a PDF file if the health record list is empty.
     */
    @Test
    fun testPickPdfButtonAndSelectPdfFile() {
        // Perform click on the pick PDF button
        Espresso.onView(withId(R.id.pickPdfHealthRecordButton)).perform(ViewActions.click())

        // Check if the health record list is displayed
        Espresso.onView(withId(R.id.pdfHealthRecordRecyclerView))
            .check(ViewAssertions.matches(isDisplayed()))

        // Give some time to the user to select a PDF file
        Thread.sleep(4000)

        // Check if the health record list has at least 1 item
        Espresso.onView(withId(R.id.pdfHealthRecordRecyclerView))
            .check(ViewAssertions.matches(hasMinimumChildCount(1)))
    }

}

object MyViewAction {
    fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController?, view: View?) {
                val v = view?.findViewById<View>(id)
                v?.performClick()
            }
        }
    }
}
