package com.linku.data.service.system

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.linku.domain.bean.Point3D
import com.linku.domain.service.system.SensorService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SensorServiceImpl @Inject constructor(
    @ApplicationContext context: Context
) : SensorService {
    private val manager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    override fun observe(): Flow<Point3D> = callbackFlow {
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                trySend(Point3D(x, y, z))
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

            }
        }
        manager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        awaitClose {
            manager.unregisterListener(listener)
        }
    }
}
