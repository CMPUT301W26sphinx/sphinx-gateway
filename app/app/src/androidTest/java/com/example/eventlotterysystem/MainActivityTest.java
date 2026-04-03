package com.example.eventlotterysystem;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.action.ViewActions;
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
    public void addCommentTest() {
        // click the organizer tab
        onView(withId(R.id.organizer)).perform(click());
        // click the comments tab
        onView(withId(R.id.commentsTab)).perform(click());
        // add a comment
        onView(withId(R.id.write_comment_box)).perform(ViewActions.typeText("ui test add comment"));
        // click send comment button
        onView(withId(R.id.add_comment_button)).perform(click());
        //


    }

}
