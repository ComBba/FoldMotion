package com.foldmotion.app.hinge

import kotlinx.coroutines.flow.Flow

interface HingeAngleSource {
    fun observe(): Flow<HingeAvailability>
}
