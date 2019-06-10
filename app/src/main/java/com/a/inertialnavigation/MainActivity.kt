package com.a.inertialnavigation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity(), SensorEventListener {
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)

        _gravityTextView = findViewById(R.id.gravityText)
        _accelerationTextView = findViewById(R.id.accelerationText)
        _updateFrequencyTextView = findViewById(R.id.updateFrequencyTextView)

        setupAccelerometer()
    }

    fun setupAccelerometer() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            printErrorMessage("Failed to get Sensor instance for TYPE_ACCELEROMETER")
            return
        }

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        val alpha: Float = 0.8f

        if (_previousEventTimeStamp == 0L) {
            _previousEventTimeStamp = event.timestamp
            return
        }

        val dt = (event.timestamp - _previousEventTimeStamp) * 1e-9f

        _updateFrequencyTextView!!.setText("" + 1.0f/dt + " Hz")
        _previousEventTimeStamp = event.timestamp

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        val linearAcceleration = FloatArray(3)
        // Remove the gravity contribution with the high-pass filter.
        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]

        for (i in 0..2)
            _speed[i] += linearAcceleration[i] * dt

        _gravityTextView!!.setText("Gravity: " + gravity[0] + ", " + gravity[1] + ", " + gravity[2])
        _accelerationTextView!!.setText("Gravity-corrected acceleration: " + _speed[0] + ", " + _speed[1] + ", " + _speed[2])
    }


    fun printErrorMessage(text: String) {
        Toast.makeText(this, "Error: " + text, Toast.LENGTH_LONG)
    }

    private var _gravityTextView: TextView? = null
    private var _accelerationTextView: TextView? = null
    private var _updateFrequencyTextView: TextView? = null

    private var _previousEventTimeStamp: Long = 0

    private val gravity = floatArrayOf(0.0f, 0.0f, 0.0f)

    private val _speed = floatArrayOf(0.0f, 0.0f, 0.0f)
}
