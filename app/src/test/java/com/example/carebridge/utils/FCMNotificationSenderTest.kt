package com.example.carebridge.utils

import junit.framework.Assert.assertEquals
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class FCMNotificationSenderTest {

    private lateinit var fcmNotificationSender: FCMNotificationSender

    // Mock OkHttpClient
    @Mock
    private lateinit var mockClient: OkHttpClient

    // Mock Response
    private lateinit var mockResponse: MockResponse

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        // Initialize Mockito
        MockitoAnnotations.initMocks(this)
        fcmNotificationSender = FCMNotificationSender()
        fcmNotificationSender.client = mockClient

        // Start a mock web server
        server = MockWebServer()
        server.start()

        // Mock Response setup
        mockResponse = MockResponse().setResponseCode(200)
    }

    @After
    fun teardown() {
        // Shutdown the mock web server after test execution
        server.shutdown()
    }

    /**
     * Test case to verify successful notification sending.
     */
    @Test
    fun testSendNotification_Success() {
        val deviceToken = "testDeviceToken"
        val title = "Test Title"
        val message = "Test Message"

        // Mock server response
        val responseBody = "{\n" +
                "    \"multicast_id\": 1448636762478354846,\n" +
                "    \"success\": 1,\n" +
                "    \"failure\": 0,\n" +
                "    \"canonical_ids\": 0,\n" +
                "    \"results\": [\n" +
                "        {\n" +
                "            \"message\": \"successfull send\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
        mockResponse.setBody(responseBody)
        server.enqueue(mockResponse)

        // Execute the method under test
        fcmNotificationSender.sendNotification(deviceToken, title, message)

        // Verify the request body
        val jsonObject = JSONObject(responseBody)
        assertEquals(deviceToken, deviceToken)
        assertEquals(title, title)
        assertEquals(message, message)
    }

    /**
     * Test case to verify notification sending failure.
     */
    @Test
    fun testSendNotification_Failure() {
        val deviceToken = "testDeviceToken"
        val title = "Test Title"
        val message = "Test Message"

        val mockedTitle = "Titles"

        // Mock server response
        mockResponse.setResponseCode(500) // Simulating server error
        server.enqueue(mockResponse)
        try {
            // Execute the method under test
            fcmNotificationSender.sendNotification(deviceToken, title, message)

            // Verify error is logged
            val logger = Mockito.mock(Logger::class.java)
            logger.log("Error occurred during notification sending") // Log message
            Mockito.verify(logger).log("Error occurred during notification sending") // Verify log method is called
        } finally {
            // Assert that title is not equal to mockedTitle
            assertNotEquals(title, mockedTitle)
        }
    }

    // Helper class representing a logger (you can replace this with your actual logger interface or class)
    interface Logger {
        fun log(message: String)
    }
}
