package com.medtroniclabs.opensource.ui.screening.listener

import com.medtroniclabs.opensource.db.tables.SiteEntity

interface SiteChangeListener {
    fun onSiteChange(siteEntity: SiteEntity)
}