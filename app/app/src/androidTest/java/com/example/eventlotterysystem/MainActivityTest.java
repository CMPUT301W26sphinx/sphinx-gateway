package com.example.eventlotterysystem;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.lang.Thread.sleep;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest

public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void loadEventsTest() throws InterruptedException {
        sleep(5000);
        // check events
        onView(withId(R.id.eventsRecyclerView)).perform(RecyclerViewActions.scrollTo(hasDescendant(withText("Coding Class"))));
        onView(withText("Coding Class")).check(matches(isDisplayed()));
    }

    @Test
    public void addCommentTest() throws InterruptedException {
        String TEST_COMMENT = "ui test add comment" + System.currentTimeMillis();
        // click event
        sleep(5000);
        onView(withId(R.id.eventsRecyclerView)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Coding Class")), click()));
        // click the comments tab
        sleep(5000);
        onView(withId(R.id.seeCommentsButton)).perform(click());
        // add a comment
        sleep(5000);
        onView(withId(R.id.write_comment_box)).perform(typeText(TEST_COMMENT));
        // click send comment button
        sleep(5000);
        onView(withId(R.id.add_comment_button)).perform(click());
        // verify added
        sleep(3000);
        onView(withId(R.id.comment_recycler_view)).perform(RecyclerViewActions.scrollTo(hasDescendant(withText(TEST_COMMENT))));
        onView(withText(TEST_COMMENT)).check(matches(isDisplayed()));


    }

    @Test
    public void addEventTest() throws InterruptedException {
        // click the organizer tab
        sleep(5000);
        onView(withId(R.id.organizer)).perform(click());

        onView(withId(R.id.createEventButton)).perform(click());

        onView(withId(R.id.eventName)).perform(scrollTo(), typeText("TEST_EVENT"), closeSoftKeyboard());

        onView(withId(R.id.eventDescription)).perform(scrollTo(), typeText("TEST_DESCRIPTION"), closeSoftKeyboard());

        onView(withId(R.id.eventPlace)).perform(scrollTo(), typeText("EDMONTON"), closeSoftKeyboard());

        onView(withId(R.id.eventTime)).perform(scrollTo(), replaceText("05/05/2026 12:30"), closeSoftKeyboard());

        onView(withId(R.id.saveEventButton)).perform(scrollTo(), click());
        


    }

}
