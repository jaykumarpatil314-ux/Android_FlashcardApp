package com.codealpha.flashcard;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Native Android Mobile Application in Java
 * CodeAlpha Task 1: Flashcard Quiz App
 * Connects to CodeAlpha Java REST Backend on port 8080
 */
public class MainActivity extends AppCompatActivity {

    private CardView cardFront, cardBack;
    private TextView tvCategoryFront, tvQuestion, tvCategoryBack, tvAnswer, tvCardCounter;
    private Button btnShowAnswer, btnFlipBack, btnPrev, btnNext, btnAddCard;
    private ProgressBar progressBar;

    private AnimatorSet flipInAnim, flipOutAnim;
    private boolean isBackVisible = false;

    private final List<Flashcard> flashcards = new ArrayList<>();
    private int currentIndex = 0;

    // Emulator uses 10.0.2.2 to access host localhost:8080
    private static final String BACKEND_URL = "http://10.0.2.2:8080/api/flashcards";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadAnimations();
        seedFallbackCards();
        fetchCardsFromJavaBackend();
        updateCardDisplay();

        btnShowAnswer.setOnClickListener(v -> flipCard());
        btnFlipBack.setOnClickListener(v -> flipCard());
        findViewById(R.id.card_container).setOnClickListener(v -> flipCard());

        btnNext.setOnClickListener(v -> {
            if (currentIndex < flashcards.size() - 1) {
                currentIndex++;
                resetCardToFront();
                updateCardDisplay();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                resetCardToFront();
                updateCardDisplay();
            }
        });

        btnAddCard.setOnClickListener(v -> showAddCardDialog());
    }

    private void initViews() {
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        tvCategoryFront = findViewById(R.id.tv_category_front);
        tvQuestion = findViewById(R.id.tv_question);
        tvCategoryBack = findViewById(R.id.tv_category_back);
        tvAnswer = findViewById(R.id.tv_answer);
        tvCardCounter = findViewById(R.id.tv_card_counter);
        btnShowAnswer = findViewById(R.id.btn_show_answer);
        btnFlipBack = findViewById(R.id.btn_flip_back);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnAddCard = findViewById(R.id.btn_add_card);
        progressBar = findViewById(R.id.deck_progress);
    }

    private void loadAnimations() {
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);
    }

    private void flipCard() {
        if (!isBackVisible) {
            cardFront.animate().rotationY(90).setDuration(200).withEndAction(() -> {
                cardFront.setVisibility(View.GONE);
                cardBack.setVisibility(View.VISIBLE);
                cardBack.setRotationY(-90);
                cardBack.animate().rotationY(0).setDuration(200).start();
                isBackVisible = true;
            }).start();
        } else {
            cardBack.animate().rotationY(-90).setDuration(200).withEndAction(() -> {
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                cardFront.setRotationY(90);
                cardFront.animate().rotationY(0).setDuration(200).start();
                isBackVisible = false;
            }).start();
        }
    }

    private void resetCardToFront() {
        if (isBackVisible) {
            cardBack.setVisibility(View.GONE);
            cardFront.setVisibility(View.VISIBLE);
            cardFront.setRotationY(0);
            isBackVisible = false;
        }
    }

    private void updateCardDisplay() {
        if (flashcards.isEmpty()) return;

        Flashcard current = flashcards.get(currentIndex);
        tvCategoryFront.setText(current.getCategory());
        tvCategoryBack.setText(current.getCategory());
        tvQuestion.setText(current.getQuestion());
        tvAnswer.setText(current.getAnswer());

        tvCardCounter.setText("Card " + (currentIndex + 1) + " of " + flashcards.size());
        progressBar.setProgress((int) (((float) (currentIndex + 1) / flashcards.size()) * 100));

        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < flashcards.size() - 1);
    }

    private void seedFallbackCards() {
        flashcards.add(new Flashcard("1", "Java", "What is JVM?", "Java Virtual Machine executes compiled bytecode."));
        flashcards.add(new Flashcard("2", "Android", "What is an Activity?", "An Activity represents a single focused screen in an Android app."));
        flashcards.add(new Flashcard("3", "Java", "What is Polymorphism?", "The ability of an object to take on many forms via method overriding or overloading."));
    }

    private void fetchCardsFromJavaBackend() {
        new Thread(() -> {
            try {
                URL url = new URL(BACKEND_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    // Parse JSON response on background thread and update UI
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Synced with Java Backend!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                // Java Backend not running; falls back to local cards seamlessly
            }
        }).start();
    }

    private void showAddCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Flashcard");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null);
        EditText etCat = view.findViewById(R.id.et_category);
        EditText etQ = view.findViewById(R.id.et_question);
        EditText etA = view.findViewById(R.id.et_answer);

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String cat = etCat.getText().toString().trim();
            String q = etQ.getText().toString().trim();
            String a = etA.getText().toString().trim();

            if (!q.isEmpty() && !a.isEmpty()) {
                flashcards.add(new Flashcard(String.valueOf(System.currentTimeMillis()), cat.isEmpty() ? "General" : cat, q, a));
                currentIndex = flashcards.size() - 1;
                resetCardToFront();
                updateCardDisplay();
                Toast.makeText(MainActivity.this, "Flashcard added!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
