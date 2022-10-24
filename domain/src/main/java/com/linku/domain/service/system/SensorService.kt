package com.linku.domain.service.system

import com.linku.domain.bean.Point3D
import kotlinx.coroutines.flow.Flow

interface SensorService {
    fun observe(): Flow<Point3D>
}
