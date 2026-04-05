package com.example.eventlotterysystem;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.database.UserCommentManager;
import com.example.eventlotterysystem.model.UserComment;
import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class UserCommentManagerTest {

    private static final String EVENT_ID = "Gh3IHAczlecQ4oMOj8t2";
    private static final String TEST_COMMENT = "TEST_comment_" + System.currentTimeMillis();


    @Before
    public void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth.signInAnonymously().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                latch.countDown();
                fail("signInAnonymously failed: " + task.getException());
                return;
            }

            assertNotNull(mAuth.getCurrentUser());
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).get().addOnSuccessListener(document -> {
                if (document.exists()) {
                    latch.countDown();
                    return;
                }

                UserProfile userProfile = new UserProfile();
                userProfile.setUserID(uid);
                userProfile.setFirstName("TestUser");


                db.collection("users").document(uid).set(userProfile).addOnSuccessListener(unused -> latch.countDown()).addOnFailureListener(e -> {
                    latch.countDown();
                    fail("Profile creation failed: " + e.getMessage());
                });
            }).addOnFailureListener(e -> {
                latch.countDown();
                fail("Profile lookup failed: " + e.getMessage());
            });
        });

        assertTrue("Timed out during setup", latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void testAddCommentToEvent() throws InterruptedException {
        UserCommentManager manager = UserCommentManager.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        manager.addCommentToEvent(EVENT_ID, TEST_COMMENT, false, new UserCommentManager.OnCommentAddedListener() {
            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                fail("Add comment failed: " + e.getMessage());
            }

            @Override
            public void onSuccess(Void unused) {
                // verify it was added
                manager.getCommentsFromEvent(EVENT_ID, new UserCommentManager.UserCommentCallback() {
                    @Override
                    public void onCommentLoaded(List<UserComment> comments) {
                        boolean found = false;

                        for (UserComment comment : comments) {
                            if (TEST_COMMENT.equals(comment.getText())) {
                                found = true;
                                break;
                            }
                        }

                        latch.countDown();
                        assertTrue("Comment not found in database", found);
                    }

                    @Override
                    public void onError(Exception e) {
                        latch.countDown();
                        fail("Fetching comments failed: " + e.getMessage());
                    }
                });


            }
        });


        assertTrue("Timed out waiting for comment test", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testDeleteCommentFromEvent() throws InterruptedException {
        UserCommentManager manager = UserCommentManager.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        manager.addCommentToEvent(EVENT_ID, TEST_COMMENT, false, new UserCommentManager.OnCommentAddedListener() {
            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                fail("Failed to add comment: " + e.getMessage());
            }

            @Override
            public void onSuccess(Void unused) {
                manager.getCommentsFromEvent(EVENT_ID, new UserCommentManager.UserCommentCallback() {
                    @Override
                    public void onCommentLoaded(List<UserComment> comments) {
                        String commentId = null;

                        for (UserComment comment : comments) {
                            if (TEST_COMMENT.equals(comment.getText())) {
                                commentId = comment.getCommentID();
                                break;
                            }
                        }

                        if (commentId == null) {
                            latch.countDown();
                            fail("Comment not found after add");
                            return;
                        }

                        manager.deleteComment(EVENT_ID, commentId, new UserCommentManager.OnCommentDeletedListener() {
                            @Override
                            public void onFailure(Exception e) {
                                latch.countDown();
                                fail("Could not delete comment: " + e.getMessage());
                            }

                            @Override
                            public void onSuccess(Void unused) {
                                manager.getCommentsFromEvent(EVENT_ID, new UserCommentManager.UserCommentCallback() {
                                    @Override
                                    public void onCommentLoaded(List<UserComment> commentsAfterDelete) {
                                        for (UserComment comment : commentsAfterDelete) {
                                            if (TEST_COMMENT.equals(comment.getText())) {
                                                latch.countDown();
                                                fail("Comment was still found after deletion");
                                                return;
                                            }
                                        }

                                        latch.countDown();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        latch.countDown();
                                        fail("Fetching comments failed: " + e.getMessage());
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        latch.countDown();
                        fail("Fetching comments failed: " + e.getMessage());
                    }
                });
            }
        });

        assertTrue("Timed out waiting for delete test", latch.await(10, TimeUnit.SECONDS));
    }
}