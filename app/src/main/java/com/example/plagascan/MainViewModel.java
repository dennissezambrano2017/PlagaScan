package com.example.plagascan;

import androidx.lifecycle.ViewModel;

import com.google.mediapipe.tasks.core.Delegate;

public class MainViewModel extends ViewModel {

        private Delegate _delegate = ObjectDetectorHelper.DELEGATE_GPU;
        private float _threshold = ObjectDetectorHelper.THRESHOLD_DEFAULT;
        private int _maxResults = ObjectDetectorHelper.MAX_RESULTS_DEFAULT;
        private String _model = ObjectDetectorHelper.MODEL;

        public Delegate getCurrentDelegate() {
            return _delegate;
        }

        public float getCurrentThreshold() {
            return _threshold;
        }

        public int getCurrentMaxResults() {
            return _maxResults;
        }

        public String getCurrentModel() {
            return _model;
        }

        public void setDelegate(Delegate delegate) {
            _delegate = delegate;
        }

        public void setThreshold(float threshold) {
            _threshold = threshold;
        }

        public void setMaxResults(int maxResults) {
            _maxResults = maxResults;
        }

        public void setModel(String model) {
            _model = model;
        }

}
