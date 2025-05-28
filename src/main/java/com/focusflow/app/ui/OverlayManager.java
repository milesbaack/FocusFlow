package com.focusflow.app.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Manages overlay panels with smooth animations for seamless UX.
 * Provides different animation types and handles overlay lifecycle.
 * 
 * Features:
 * - Multiple animation types (slide, fade, etc.)
 * - Background dim overlay
 * - Smooth transitions with proper easing
 * - Queue management for rapid show/hide calls
 * - Memory management and cleanup
 * 
 * @author FocusFlow Team
 * @version 2.0 - Enhanced Animation System
 */
public class OverlayManager {
    private final StackPane overlayContainer;
    private Node currentOverlay;
    private Rectangle backgroundDim;
    private Timeline currentAnimation;
    private boolean isAnimating = false;

    // Animation settings
    private static final Duration ANIMATION_DURATION = Duration.millis(300);
    private static final Duration FAST_ANIMATION_DURATION = Duration.millis(200);
    private static final Interpolator EASE_OUT = Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0);
    private static final Interpolator EASE_IN = Interpolator.SPLINE(0.42, 0.0, 1.0, 1.0);

    /**
     * Animation types available for overlay transitions
     */
    public enum AnimationType {
        SLIDE_UP, // Panel slides up from bottom
        SLIDE_DOWN, // Panel slides down from top
        SLIDE_LEFT, // Panel slides in from left
        SLIDE_RIGHT, // Panel slides in from right
        FADE_IN, // Panel fades in
        SCALE_IN, // Panel scales in from center
        BOUNCE_IN // Panel bounces in with spring effect
    }

    /**
     * Creates a new OverlayManager for the specified container.
     * 
     * @param container The StackPane that will contain overlays
     */
    public OverlayManager(StackPane container) {
        this.overlayContainer = container;
        initializeBackgroundDim();
    }

    /**
     * Shows an overlay with the specified animation type.
     * If another overlay is currently showing, it will be hidden first.
     * 
     * @param overlay       The overlay node to show
     * @param animationType The animation type to use
     */
    public void showOverlay(Node overlay, AnimationType animationType) {
        if (isAnimating) {
            return; // Prevent rapid-fire calls during animation
        }

        hideCurrentOverlay(() -> {
            currentOverlay = overlay;
            overlayContainer.getChildren().addAll(backgroundDim, overlay);
            animateIn(overlay, animationType);
        });
    }

    /**
     * Hides the current overlay if one is showing.
     */
    public void hideCurrentOverlay() {
        hideCurrentOverlay(null);
    }

    /**
     * Hides the current overlay and executes a callback when complete.
     * 
     * @param onComplete Callback to execute after hiding is complete
     */
    public void hideCurrentOverlay(Runnable onComplete) {
        if (currentOverlay != null && !isAnimating) {
            animateOut(currentOverlay, () -> {
                overlayContainer.getChildren().removeAll(backgroundDim, currentOverlay);
                currentOverlay = null;
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        } else if (onComplete != null) {
            onComplete.run();
        }
    }

    /**
     * Checks if an overlay is currently showing.
     * 
     * @return true if an overlay is visible
     */
    public boolean isOverlayShowing() {
        return currentOverlay != null;
    }

    /**
     * Gets the currently showing overlay node.
     * 
     * @return The current overlay node, or null if none is showing
     */
    public Node getCurrentOverlay() {
        return currentOverlay;
    }

    /**
     * Initializes the background dim overlay that appears behind panels.
     */
    private void initializeBackgroundDim() {
        backgroundDim = new Rectangle();
        backgroundDim.setFill(Color.rgb(0, 0, 0, 0.4)); // Semi-transparent black
        backgroundDim.widthProperty().bind(overlayContainer.widthProperty());
        backgroundDim.heightProperty().bind(overlayContainer.heightProperty());
        backgroundDim.setOnMouseClicked(e -> hideCurrentOverlay()); // Click to dismiss
        backgroundDim.setOpacity(0); // Start invisible
    }

    /**
     * Animates an overlay into view with the specified animation type.
     * 
     * @param node The node to animate
     * @param type The animation type to use
     */
    private void animateIn(Node node, AnimationType type) {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        isAnimating = true;

        // Always fade in the background dim
        Timeline backgroundFade = new Timeline(
                new KeyFrame(FAST_ANIMATION_DURATION,
                        new KeyValue(backgroundDim.opacityProperty(), 1, EASE_OUT)));
        backgroundFade.play();

        // Set initial state and create animation based on type
        switch (type) {
            case SLIDE_UP:
                node.setTranslateY(overlayContainer.getHeight());
                node.setOpacity(1);
                currentAnimation = new Timeline(
                        new KeyFrame(ANIMATION_DURATION,
                                new KeyValue(node.translateYProperty(), 0, EASE_OUT)));
                break;

            case SLIDE_DOWN:
                node.setTranslateY(-overlayContainer.getHeight());
                node.setOpacity(1);
                currentAnimation = new Timeline(
                        new KeyFrame(ANIMATION_DURATION,
                                new KeyValue(node.translateYProperty(), 0, EASE_OUT)));
                break;

            case SLIDE_LEFT:
                node.setTranslateX(overlayContainer.getWidth());
                node.setOpacity(1);
                currentAnimation = new Timeline(
                        new KeyFrame(ANIMATION_DURATION,
                                new KeyValue(node.translateXProperty(), 0, EASE_OUT)));
                break;

            case SLIDE_RIGHT:
                node.setTranslateX(-overlayContainer.getWidth());
                node.setOpacity(1);
                currentAnimation = new Timeline(
                        new KeyFrame(ANIMATION_DURATION,
                                new KeyValue(node.translateXProperty(), 0, EASE_OUT)));
                break;

            case FADE_IN:
                node.setOpacity(0);
                node.setTranslateX(0);
                node.setTranslateY(0);
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.opacityProperty(), 1, EASE_OUT)));
                break;

            case SCALE_IN:
                node.setScaleX(0.8);
                node.setScaleY(0.8);
                node.setOpacity(0);
                node.setTranslateX(0);
                node.setTranslateY(0);
                currentAnimation = new Timeline(
                        new KeyFrame(ANIMATION_DURATION,
                                new KeyValue(node.scaleXProperty(), 1, EASE_OUT),
                                new KeyValue(node.scaleYProperty(), 1, EASE_OUT),
                                new KeyValue(node.opacityProperty(), 1, EASE_OUT)));
                break;

            case BOUNCE_IN:
                node.setScaleX(0.3);
                node.setScaleY(0.3);
                node.setOpacity(0);
                node.setTranslateX(0);
                node.setTranslateY(0);

                // Create bouncy spring effect
                currentAnimation = new Timeline(
                        new KeyFrame(Duration.millis(0),
                                new KeyValue(node.scaleXProperty(), 0.3),
                                new KeyValue(node.scaleYProperty(), 0.3),
                                new KeyValue(node.opacityProperty(), 0)),
                        new KeyFrame(Duration.millis(150),
                                new KeyValue(node.scaleXProperty(), 1.05, Interpolator.EASE_OUT),
                                new KeyValue(node.scaleYProperty(), 1.05, Interpolator.EASE_OUT),
                                new KeyValue(node.opacityProperty(), 1)),
                        new KeyFrame(Duration.millis(300),
                                new KeyValue(node.scaleXProperty(), 1, Interpolator.EASE_OUT),
                                new KeyValue(node.scaleYProperty(), 1, Interpolator.EASE_OUT)));
                break;

            default:
                // Fallback to fade in
                node.setOpacity(0);
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.opacityProperty(), 1, EASE_OUT)));
                break;
        }

        currentAnimation.setOnFinished(e -> {
            isAnimating = false;
            // Ensure final state is clean
            node.setTranslateX(0);
            node.setTranslateY(0);
            node.setScaleX(1);
            node.setScaleY(1);
            node.setOpacity(1);
        });

        currentAnimation.play();
    }

    /**
     * Animates the current overlay out of view.
     * 
     * @param node       The node to animate out
     * @param onComplete Callback to execute when animation completes
     */
    private void animateOut(Node node, Runnable onComplete) {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        isAnimating = true;

        // Fade out background dim
        Timeline backgroundFade = new Timeline(
                new KeyFrame(FAST_ANIMATION_DURATION,
                        new KeyValue(backgroundDim.opacityProperty(), 0, EASE_IN)));
        backgroundFade.play();

        // Determine exit animation based on current position/state
        AnimationType exitType = determineExitAnimation(node);

        switch (exitType) {
            case SLIDE_DOWN:
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.translateYProperty(),
                                        overlayContainer.getHeight(), EASE_IN)));
                break;

            case SLIDE_UP:
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.translateYProperty(),
                                        -overlayContainer.getHeight(), EASE_IN)));
                break;

            case SLIDE_RIGHT:
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.translateXProperty(),
                                        overlayContainer.getWidth(), EASE_IN)));
                break;

            case SLIDE_LEFT:
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.translateXProperty(),
                                        -overlayContainer.getWidth(), EASE_IN)));
                break;

            case SCALE_IN: // Scale out
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.scaleXProperty(), 0.8, EASE_IN),
                                new KeyValue(node.scaleYProperty(), 0.8, EASE_IN),
                                new KeyValue(node.opacityProperty(), 0, EASE_IN)));
                break;

            default: // FADE_IN becomes fade out
                currentAnimation = new Timeline(
                        new KeyFrame(FAST_ANIMATION_DURATION,
                                new KeyValue(node.opacityProperty(), 0, EASE_IN)));
                break;
        }

        currentAnimation.setOnFinished(e -> {
            isAnimating = false;
            if (onComplete != null) {
                onComplete.run();
            }
        });

        currentAnimation.play();
    }

    /**
     * Determines the appropriate exit animation based on the node's current state.
     * This creates intuitive exit animations that reverse the entry animation.
     * 
     * @param node The node to analyze
     * @return The appropriate exit animation type
     */
    private AnimationType determineExitAnimation(Node node) {
        // Check if node was positioned by slide animations
        double translateX = node.getTranslateX();
        double translateY = node.getTranslateY();
        double scaleX = node.getScaleX();
        double scaleY = node.getScaleY();

        // If node has non-standard scale, it was probably scaled in
        if (Math.abs(scaleX - 1.0) > 0.01 || Math.abs(scaleY - 1.0) > 0.01) {
            return AnimationType.SCALE_IN;
        }

        // If node has translation, determine slide direction
        if (Math.abs(translateX) > Math.abs(translateY)) {
            return translateX > 0 ? AnimationType.SLIDE_RIGHT : AnimationType.SLIDE_LEFT;
        } else if (Math.abs(translateY) > 1) {
            return translateY > 0 ? AnimationType.SLIDE_DOWN : AnimationType.SLIDE_UP;
        }

        // Check node's position in container to make educated guess
        Bounds nodeBounds = node.getBoundsInParent();
        Bounds containerBounds = overlayContainer.getBoundsInLocal();

        // If node is in bottom half, slide down; if top half, slide up
        if (nodeBounds.getMinY() > containerBounds.getHeight() / 2) {
            return AnimationType.SLIDE_DOWN;
        } else {
            return AnimationType.FADE_IN; // Fade out
        }
    }

    /**
     * Creates a quick fade-in overlay for simple content.
     * Convenience method for common use case.
     * 
     * @param content The content to show
     */
    public void showQuickOverlay(Node content) {
        showOverlay(content, AnimationType.FADE_IN);
    }

    /**
     * Creates a slide-up overlay for panels that should appear from bottom.
     * Convenience method for common use case.
     * 
     * @param panel The panel to show
     */
    public void showPanel(Node panel) {
        showOverlay(panel, AnimationType.SLIDE_UP);
    }

    /**
     * Shows an overlay with a bounce effect for attention-grabbing content.
     * Convenience method for notifications or important panels.
     * 
     * @param content The content to show with bounce effect
     */
    public void showWithBounce(Node content) {
        showOverlay(content, AnimationType.BOUNCE_IN);
    }

    /**
     * Immediately hides any overlay without animation.
     * Use sparingly - animations provide better UX.
     */
    public void hideImmediately() {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        if (currentOverlay != null) {
            overlayContainer.getChildren().removeAll(backgroundDim, currentOverlay);
            currentOverlay = null;
        }

        isAnimating = false;
    }

    /**
     * Cleanup method to stop any running animations and clear references.
     * Call this when the OverlayManager is no longer needed.
     */
    public void cleanup() {
        hideImmediately();
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        backgroundDim = null;
    }
}