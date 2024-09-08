package com.velvit.roadvision

object Constants {
    const val MODEL_PATH = "best_float16.tflite"//"yolov8n_int8.tflite"
    val LABELS_PATH: String? = "yolo_road.txt"//null // provide your labels.txt file if the metadata not present in the model
}