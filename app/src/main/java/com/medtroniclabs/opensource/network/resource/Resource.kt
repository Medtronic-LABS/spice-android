package com.medtroniclabs.opensource.network.resource

/**
 * class to hold currently fetching
 * @state resource state
 * @data if fetching done the actual data
 * @message if any errors occurred then the message
 */
data class Resource<out T> constructor(
    val state: ResourceState,
    val data: T? = null,
    val message: String? = null,
    val optionalData: Boolean? = null
)