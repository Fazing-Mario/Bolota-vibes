package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.notification.BolotaNotificationHelper
import com.example.notification.DailyReminderWorker
import com.example.notification.ReminderScheduler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager(context)
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bolota", appName)
  }

  @Test
  fun `notification channel is created properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    BolotaNotificationHelper.createNotificationChannel(context)
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = manager.getNotificationChannel(BolotaNotificationHelper.CHANNEL_ID)
    assertNotNull(channel)
    assertEquals(BolotaNotificationHelper.CHANNEL_NAME, channel.name)
  }

  @Test
  fun `reminder scheduler saves preferences and computes time`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    ReminderScheduler.setReminderPreferences(context, enabled = true, hour = 19, minute = 30)
    assertTrue(ReminderScheduler.isRemindersEnabled(context))
    val (hour, minute) = ReminderScheduler.getReminderTime(context)
    assertEquals(19, hour)
    assertEquals(30, minute)
  }

  @Test
  fun `daily reminder worker executes successfully`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val worker = TestListenableWorkerBuilder<DailyReminderWorker>(context).build()
    val result = worker.doWork()
    assertTrue(result is ListenableWorker.Result.Success)
  }
}

