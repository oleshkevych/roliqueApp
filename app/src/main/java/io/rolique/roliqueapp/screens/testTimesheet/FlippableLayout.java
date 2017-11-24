package io.rolique.roliqueapp.screens.testTimesheet;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Volodymyr Oleshkevych on 11/23/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class FlippableLayout extends FrameLayout {

    private FlipEvaluator flipRightInEvaluator;
    private FlipEvaluator flipRightOutEvaluator;
    private FlipEvaluator flipLeftInEvaluator;
    private FlipEvaluator flipLeftOutEvaluator;

    public FlippableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlippableLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setCameraDistance(getCameraDistance() * 10); // reduces perspective skewing
        flipRightInEvaluator = new FlipEvaluator(
                1f, .5f, // pivotX/pivotY
                -1f, 0f, // translationX start/end
                -180, 0, // rotationY start/end
                0f, 1f); // alpha start/end
        flipRightOutEvaluator = new FlipEvaluator(
                0f, .5f,
                0f, 1f,
                0, 180,
                1f, 0f);
        flipLeftInEvaluator = new FlipEvaluator(
                .0f, .5f,
                1f, 0f,
                180, 0,
                0f, 1f);
        flipLeftOutEvaluator = new FlipEvaluator(
                1f, .5f,
                0f, -1f,
                0, -180,
                1f, 0f);
    }

    public void setFlipRightIn(float value) {
        evaluateUsing(flipRightInEvaluator, value);
    }

    public void setFlipRightOut(float value) {
        evaluateUsing(flipRightOutEvaluator, value);
    }

    public void setFlipLeftIn(float value) {
        evaluateUsing(flipLeftInEvaluator, value);
    }

    public void setFlipLeftOut(float value) {
        evaluateUsing(flipLeftOutEvaluator, value);
    }

    private void evaluateUsing(FlipEvaluator evaluator, float value) {
        float cappedValue = Math.min(1f, Math.max(0f, value));
        setPivotX(getWidth() * evaluator.getPivotX());
        setPivotY(getHeight() * evaluator.getPivotY());
        setAlpha(evaluator.getAlpha(cappedValue));
        setTranslationX(getWidth() * evaluator.getTranslationX(cappedValue));
        setRotationY(evaluator.getRotationY(cappedValue));
    }

    private static class FlipEvaluator {
        private final float pivotX;
        private final float pivotY;
        private final float startTranslationX;
        private final float endTranslationY;
        private final float startRotationY;
        private final float endRotationY;
        private final float startAlpha;
        private final float endAlpha;

        /**
         * Simple evaluator holding all the start/end values for a flip animation.
         *
         * @param pivotX value between 0 and 1, where 0 is the left border and 1 is the right border of the target
         * @param pivotY value between 0 and 1, where 0 is the top border and 1 is the bottom border of the target
         * @param startTranslationX value between 0 and 1, where 1 is the width of the target
         * @param endTranslationY value between 0 and 1, where 1 is the width of the target
         * @param startRotationY value between -180 and 180
         * @param endRotationY value between -180 and 180
         * @param startAlpha initial alpha
         * @param endAlpha final alpha
         */
        private FlipEvaluator(float pivotX, float pivotY,
                              float startTranslationX, float endTranslationY,
                              float startRotationY, float endRotationY,
                              float startAlpha, float endAlpha) {
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.startTranslationX = startTranslationX;
            this.endTranslationY = endTranslationY;
            this.startRotationY = startRotationY;
            this.endRotationY = endRotationY;
            this.startAlpha = startAlpha;
            this.endAlpha = endAlpha;
        }

        public float getPivotX() {
            return pivotX;
        }

        public float getPivotY() {
            return pivotY;
        }

        public float getTranslationX(float t) {
            return startTranslationX + (endTranslationY - startTranslationX) * t;
        }

        public float getRotationY(float t) {
            return startRotationY + (endRotationY - startRotationY) * t;
        }

        public float getAlpha(float t) {
            return t < .5f ? startAlpha : endAlpha;
        }
    }
}